package com.fun90.idea.patcher;

import com.fun90.idea.util.PatcherUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class ExportPatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile[] selectedFiles = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            PatcherUtil.showError("Please select at least one file!", event.getProject());
        } else if (PatcherUtil.isNotSameModule(selectedFiles)) {
            PatcherUtil.showWarning("Please select the module manually!", event.getProject());
        }
        PatcherDialog dialog = new PatcherDialog(event);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
