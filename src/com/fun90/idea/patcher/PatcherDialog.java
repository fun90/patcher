package com.fun90.idea.patcher;

import com.fun90.idea.util.ExceptionUtils;
import com.fun90.idea.util.FilesUtil;
import com.fun90.idea.util.PatcherUtil;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PatcherDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JTextField textField;
    private JButton fileChooseBtn;
    private JPanel filePanel;
    private JTextField webTextField;
    private JComboBox moduleComboBox;
    private AnActionEvent event;
    private JBList fieldList;
    private Module module;

    PatcherDialog(final AnActionEvent event) {
        this.event = event;
        setTitle("Create Patcher Dialog");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        final String userDir = System.getProperty("user.home");
        textField.setText(userDir + File.separator + "Desktop");
        // 保存路径按钮事件
        fileChooseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(userDir);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                int flag = fileChooser.showOpenDialog(null);
                if (flag == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        final ModuleManager moduleManager = ModuleManager.getInstance(event.getProject());
        Module[] modules = moduleManager.getModules();
        // 增加空选项，防止第一项无法选中
        moduleComboBox.addItem("");
        for (Module module : modules) {
            moduleComboBox.addItem(module.getName());
        }

        // 模块对象
        module = modules.length == 1 ? modules[0] : event.getData(DataKeys.MODULE);
        if (module != null) {
            moduleComboBox.setSelectedItem(module.getName());
        }

        moduleComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                module = moduleManager.findModuleByName((String) e.getItem());
            }
        });
    }

    private void onOK() {
        // 条件校验
        if (null == textField.getText() || "".equals(textField.getText())) {
            Messages.showErrorDialog(this, "Please Select Save Path!", "Error");
            return;
        }

        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog(this, "Please Select Export File!", "Error");
            return;
        }

        if (module == null) {
            Messages.showErrorDialog(this, "Please Select Module!", "Error");
            return;
        }

        try {
            // 先编译
            CompilerManager compilerManager = CompilerManager.getInstance(event.getProject());
            compilerManager.make(module, new CompileStatusNotification() {
                @Override
                public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                    if (aborted) {
                        PatcherUtil.showInfo("Code compilation has been aborted.", compileContext.getProject());
                        return;
                    }
                    if (errors != 0) {
                        PatcherUtil.showError("Errors occurred while compiling code!", compileContext.getProject());
                        return;
                    }
                    try {
                        execute(compileContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                        PatcherUtil.showError(ExceptionUtils.getStructuredErrorString(e), compileContext.getProject());
                    }
                }
            });
            PatcherUtil.showInfo("Code is compiling.", event.getProject());
        } catch (Exception e) {
            e.printStackTrace();
            PatcherUtil.showError(ExceptionUtils.getStructuredErrorString(e), event.getProject());
        } finally {
            dispose();
        }
    }

    private String getSourceRootPath(List<String> sourceRootPathList, String elementPath) {
        for (String s : sourceRootPathList) {
            if (elementPath.contains(s)) {
                return s;
            }
        }
        return null;
    }

    private void onCancel() {
        dispose();
    }


    private void createUIComponents() {
        VirtualFile[] data = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        fieldList = new JBList(data);
        fieldList.setEmptyText("No File Selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }

    private void execute(CompileContext compileContext) {
        // 编译输出目录
        VirtualFile compilerOutputPath = compileContext.getModuleOutputDirectory(module);
        String compilerOutputUrl = compilerOutputPath.getPath();
        // 源码目录
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();
        List<String> sourceRootPathList = new ArrayList<>(sourceRoots.length);
        for (VirtualFile sourceRoot : sourceRoots) {
            sourceRootPathList.add(sourceRoot.getPath());
        }
        // JavaWeb项目的WebRoot目录
        String webPath = "/" + webTextField.getText() + "/";
        // 导出目录
        String exportPath = textField.getText() + File.separator + module.getName() + File.separator;
        ListModel<VirtualFile> model = fieldList.getModel();
        // 未导出的文件记录
        List<String> notExports = new ArrayList<>();
        // 循环文件列表
        Project project = compileContext.getProject();
        for (int i = 0; i < model.getSize(); i++) {
            VirtualFile element = model.getElementAt(i);
            String elementName = element.getName();
            String elementPath = element.getPath();
            String fileType = element.getFileType().getName();

            String sourceRootPath = getSourceRootPath(sourceRootPathList, elementPath);
            if (sourceRootPath != null && !ProjectRootsUtil.isInTestSource(element, project)) {
                String outName = elementPath.split(sourceRootPath)[1];
                if ("java".equalsIgnoreCase(fileType)) {
                    outName = outName.replace("java", "");
                    String className = elementName.replace(".java", "");
                    String packageDir = outName.substring(0, outName.lastIndexOf("/") + 1);
                    String classLocation = compilerOutputUrl + packageDir;
                    // 针对一个Java文件编译出多个class文件的情况，如:Test$1.class
                    List<Path> fileList = FilesUtil.matchFiles("glob:**" + File.separator + className + "$*.class", classLocation);
                    // 添加本身class文件
                    fileList.add(Paths.get(classLocation + className + ".class"));
                    for (Path from : fileList) {
                        String toName = packageDir + from.getFileName().toString();
                        Path to = Paths.get(exportPath + "WEB-INF" + File.separator + "classes" + toName);
                        FilesUtil.copy(from, to);
                    }
                } else {
                    Path from = Paths.get(compilerOutputUrl + outName);
                    Path to = Paths.get(exportPath + "WEB-INF" + File.separator + "classes" + outName);
                    FilesUtil.copy(from, to);
                }
            } else if (elementPath.contains(webPath)) {
                Path from = Paths.get(elementPath);
                Path to = Paths.get(exportPath + elementPath.split(webPath)[1]);
                FilesUtil.copy(from, to);
            } else {
                notExports.add(elementPath);
            }
        }

        StringBuilder message = new StringBuilder();
        int notExportSize = notExports.size();
        int fileCount = model.getSize() - notExportSize;
        message.append("Export ").append(fileCount).append(" files. ");
        if (fileCount != 0) {
            message.append("(<a href=\"file://").append(exportPath).append("\" target=\"blank\">open</a>)<br>");
        }
        if (notExportSize > 0) {
            message.append("<b>Warning:</b>");
            for (int i = 0; i < notExportSize; i++) {
                message.append(notExports.get(i));
                if (i < notExportSize - 1) {
                    message.append(",<br>");
                }
            }
            message.append(" <b>is not exported!</b><br><b>Please make sure web path is right and these files are not tests.</b>");
//            PatcherUtil.showError(message.toString(), project);
        }
        PatcherUtil.showInfo(message.toString(), project);
    }
}
