package com.fun90.idea.util;

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.notification.*;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatcherUtil {
    private static final String PLUGIN_NAME = "Patcher";
    private static final String NOTIFICATION_TITLE = "Patcher";
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(PLUGIN_NAME + " log",
                    NotificationDisplayType.BALLOON, true);
    private static final Pattern webPathPattern = Pattern.compile("(.+)/(webapp|WebRoot)/(.+)");

    public static PathResult getPathResult(CompileContext compileContext, Module module, ListModel<VirtualFile> selectedFiles, String pathPreffix) {
        // 编译输出目录
        VirtualFile compilerOutputPath = compileContext.getModuleOutputDirectory(module);
        String compilerOutputUrl = Objects.requireNonNull(compilerOutputPath).getPath();
        // 源码目录
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();
        List<String> sourceRootPathList = new ArrayList<>(sourceRoots.length);
        for (VirtualFile sourceRoot : sourceRoots) {
            sourceRootPathList.add(sourceRoot.getPath());
        }
        PathResult pathResult = new PathResult();
        Project project = compileContext.getProject();
        for (int i = 0; i < selectedFiles.getSize(); i++) {
            VirtualFile element = selectedFiles.getElementAt(i);
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
                        Path to = Paths.get(pathPreffix + "WEB-INF" + File.separator + "classes" + toName);
                        pathResult.put(from, to);
                    }
                } else {
                    Path from = Paths.get(compilerOutputUrl + outName);
                    Path to = Paths.get(pathPreffix + "WEB-INF" + File.separator + "classes" + outName);
                    pathResult.put(from, to);
                }
                continue;
            }
            Matcher webPathMatcher = webPathPattern.matcher(elementPath);
            if (webPathMatcher.find()) {
                Path from = Paths.get(elementPath);
                Path to = Paths.get(pathPreffix + webPathMatcher.group(3));
                pathResult.put(from, to);
            } else {
                pathResult.addUnsettled(elementPath);
            }
        }
        return pathResult;
    }

    private static String getSourceRootPath(List<String> sourceRootPathList, String elementPath) {
        for (String s : sourceRootPathList) {
            if (elementPath.contains(s)) {
                return s;
            }
        }
        return null;
    }

    public static void showInfo(String content, Project project) {
        showNotification(content, NotificationType.INFORMATION, project);
    }

    public static void showError(String content, Project project) {
        showNotification(content, NotificationType.ERROR, project);
    }

    private static void showNotification(String content, NotificationType type, Project project) {
        Notifications.Bus.notify(PatcherUtil.NOTIFICATION_GROUP.createNotification(
                PatcherUtil.NOTIFICATION_TITLE, content, type,
                NotificationListener.URL_OPENING_LISTENER), project);
    }
}
