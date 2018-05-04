package com.fun90.idea.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class PatcherUtil {
    private static final String PLUGIN_NAME = "Patcher";
    private static final String NOTIFICATION_TITLE = "Create Patcher";
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(PLUGIN_NAME + " log",
                    NotificationDisplayType.BALLOON, true);

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
