package com.microsoft.alm.plugin.external.exceptions;

public class ToolStderrException extends ToolException {
    public ToolStderrException(String stdErr) {
        super(stdErr);
    }
}