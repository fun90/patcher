package com.fun90.idea.patcher;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

    public static List<File> matchFiles(String glob, String location) throws IOException {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        final List<File> fileList = new ArrayList<>();
        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    fileList.add(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
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

        List<String> notExports = new ArrayList<>();
        try {
            CompilerModuleExtension instance = CompilerModuleExtension.getInstance(module);
            // 编译目录
            VirtualFile compilerOutputPath = instance.getCompilerOutputPath();
            if (compilerOutputPath == null) {
                Messages.showErrorDialog(this, "Please Compile First!", "Error");
                return;
            }
            String compilerOutputUrl = compilerOutputPath.getPath();
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
            for (int i = 0; i < model.getSize(); i++) {
                VirtualFile element = model.getElementAt(i);
                String elementName = element.getName();
                String elementPath = element.getPath();
                String fileType = element.getFileType().getName();
                String sourceRootPath = getSourceRootPath(sourceRootPathList, elementPath);
                if (sourceRootPath != null) {
                    String outName = elementPath.split(sourceRootPath)[1];
                    if ("java".equalsIgnoreCase(fileType)) {
                        outName = outName.replace("java", "");
                        String className = elementName.replace(".java", "");
                        String packageDir = outName.substring(0, outName.lastIndexOf("/") + 1);
                        String classLocation = compilerOutputUrl + packageDir;
                        // 针对一个Java文件编译出多个class文件的情况，如:Test$1.class
                        List<File> fileList = matchFiles("glob:**" + File.separator + className + "$*.class", classLocation);
                        // 添加本身class文件
                        fileList.add(new File(classLocation + className + ".class"));
                        for (File from : fileList) {
//                            File from = new File(compilerOutputUrl + outName);
                            String toName = packageDir + from.getName();
                            File to = new File(exportPath + "WEB-INF" + File.separator + "classes" + toName);
                            FileUtil.copy(from, to);
                        }
                    } else {
                        File from = new File(compilerOutputUrl + outName);
                        File to = new File(exportPath + "WEB-INF" + File.separator + "classes" + outName);
                        FileUtil.copy(from, to);
                    }
                } else if (elementPath.contains(webPath)) {
                    File from = new File(elementPath);
                    File to = new File(exportPath + elementPath.split(webPath)[1]);
                    FileUtil.copy(from, to);
                } else {
                    notExports.add(elementPath);
                }
            }
            int size = notExports.size();
            if (size > 0) {
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    message.append(notExports.get(i));
                    if (i < size-1 ) {
                        message.append(", ");
                    }
                }
                message.append(" is not included in the web path!");
                Messages.showErrorDialog(this, message.toString(), "Error");
            }
        } catch (Exception e) {
//            Messages.showErrorDialog(this, "Create Patcher Error!", "Error");
//            e.printStackTrace();
            throw new RuntimeException(e);
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
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        fieldList = new JBList(data);
        fieldList.setEmptyText("No File Selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }

    public static void main(String[] args) {
        StringBuilder mess = new StringBuilder("dddd,");
        mess.deleteCharAt(mess.length() - 1);
        System.out.println(mess);
    }
}
