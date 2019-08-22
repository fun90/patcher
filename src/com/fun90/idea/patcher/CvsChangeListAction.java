package com.fun90.idea.patcher;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CvsChangeListAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        CvsChangeListDialog dialog = new CvsChangeListDialog(e);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
