package com.noelios.restlet.ext.shell.controller.commands.usage;

public class UsageException extends RuntimeException {

    public UsageException() {
    }

    public UsageException(String msg) {
        super(msg);
    }
    
    public UsageException(String format, Object ... args) {
        super(String.format(format, args));
    }
}
