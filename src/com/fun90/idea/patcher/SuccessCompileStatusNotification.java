package com.fun90.idea.patcher;

import com.fun90.idea.util.ExceptionUtils;
import com.fun90.idea.util.PatcherUtil;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SuccessCompileStatusNotification implements CompileStatusNotification {
    private Consumer<CompileContext> consumer;

    public SuccessCompileStatusNotification(Consumer<CompileContext> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        if (aborted) {
            PatcherUtil.showInfo("Code compilation has been aborted.", compileContext.getProject());
            return;
        }
        if (errors != 0) {
            PatcherUtil.showError("Errors occurred while compiling code!", compileContext.getProject());
            return;
        }
        try {
            consumer.accept(compileContext);
        } catch (Exception e) {
            e.printStackTrace();
            PatcherUtil.showError(ExceptionUtils.getStructuredErrorString(e), compileContext.getProject());
        }
    }
}
