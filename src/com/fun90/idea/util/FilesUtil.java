package com.fun90.idea.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilesUtil {

    public static List<Path> matchFiles(String glob, String location) {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
//        final List<File> fileList = new ArrayList<>();
        final List<Path> pathList = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (pathMatcher.matches(path)) {
//                        fileList.add(path.toFile());
                        pathList.add(path);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        return fileList;
        return pathList;
    }

//    public static void copy(String sourcePath, String targetPath) {
//        Path from = Paths.get(sourcePath);
//        Path to = Paths.get(targetPath);
//        copy(from, to);
//    }

    public static void copy(Path from, Path to) {
        try {
            if (!Files.exists(to)) {
                Files.createDirectories(to);
            }
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
