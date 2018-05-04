package com.fun90.idea.patcher;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreatePatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
//        VirtualFile[] data = e.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        PatcherDialog dialog = new PatcherDialog(e);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
