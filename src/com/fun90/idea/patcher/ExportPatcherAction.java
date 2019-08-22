package com.fun90.idea.patcher;

import com.fun90.idea.util.PatcherUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class ExportPatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile[] selectedFiles = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            Messages.showErrorDialog("Please select at least one file!", "Error");
            return;
        }
        if (PatcherUtil.isNotSameModule(selectedFiles)) {
            Messages.showErrorDialog("Please select file of same module!", "Error");
            return;
        }
        PatcherDialog dialog = new PatcherDialog(event);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
