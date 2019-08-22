package com.fun90.idea.patcher;

import com.fun90.idea.util.FilesUtil;
import com.fun90.idea.util.PatcherUtil;
import com.fun90.idea.util.PathResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.TextTransferable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class CvsChangeListDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pathPrefix;
    private JComboBox<String> moduleComboBox;
    private JPanel listPanel;
    private JBList<VirtualFile> fileList;
    private AnActionEvent event;
    private Module module;
    private final PatcherConfig config;

    CvsChangeListDialog(final AnActionEvent event) {
        this.event = event;
        this.config = PatcherConfig.getInstance(event.getProject());
        setTitle("Copy Change List Dialog");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        final ModuleManager moduleManager = ModuleManager.getInstance(Objects.requireNonNull(event.getProject()));
        Module[] modules = moduleManager.getModules();
        // 增加空选项，防止第一项无法选中
        moduleComboBox.addItem("");
        for (Module module : modules) {
            moduleComboBox.addItem(module.getName());
        }
        // 模块对象
        module = modules.length == 1 ? modules[0] : event.getData(LangDataKeys.MODULE);
        if (module != null) {
            moduleComboBox.setSelectedItem(module.getName());
        }
        moduleComboBox.addItemListener(e -> module = moduleManager.findModuleByName((String) e.getItem()));
        if (config != null) {
            pathPrefix.setText(config.getModulePathMap().get(module.getName()));
        }
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (data != null) {
            fileList = new JBList<>(data);
            fileList.setEmptyText("No File Selected!");
            ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fileList);
            listPanel = decorator.createPanel();
        }
    }

    private void onOK() {
        // 条件校验
        ListModel<VirtualFile> model = fileList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog(this, "Please select at least one file!", "Error");
            return;
        }
        if (module == null) {
            Messages.showErrorDialog(this, "Please select module!", "Error");
            return;
        }
        CompileExecutor compileExecutor = new CompileExecutor(module, event);
        compileExecutor.run(this::execute, this::dispose);
    }

    private void onCancel() {
        dispose();
    }

    private void execute(CompileContext compileContext) {
        Map<String, String> modulePathMap = config.getModulePathMap();
        modulePathMap.put(module.getName(), pathPrefix.getText());

        ListModel<VirtualFile> selectedFiles = fileList.getModel();
        String pathPrefixText = pathPrefix.getText() + File.separator;
        PathResult result = PatcherUtil.getPathResult(compileContext, module, selectedFiles, pathPrefixText);
        StringBuilder builder = new StringBuilder();
        result.getFromTo().values().forEach(path -> {
            builder.append(StringUtil.replace(path.toString(), File.separator, FilesUtil.FILE_SEPARATOR)).append(System.lineSeparator());
        });
        CopyPasteManager.getInstance().setContents(new TextTransferable(builder.toString()));
        PatcherUtil.showInfo("Change list was successfully copied.", event.getProject());
    }
}
