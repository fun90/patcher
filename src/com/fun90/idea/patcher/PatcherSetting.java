package com.fun90.idea.patcher;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;

public class PatcherSetting {
    public static final String PLUGIN_NAME = "Patcher";
    public static final String NOTIFACTION_TITLE = "Create Patcher";
    public static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(PLUGIN_NAME + " log",
                    NotificationDisplayType.BALLOON, true);
}
