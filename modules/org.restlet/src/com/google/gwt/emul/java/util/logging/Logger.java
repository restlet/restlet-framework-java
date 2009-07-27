package com.google.gwt.emul.java.util.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Emulate the Logger class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 * 
 */
public class Logger {

    public static final Logger global = new Logger("global", null);

    public static synchronized Logger getAnonymousLogger() {
        return new Logger("", null);
    }

    public static synchronized Logger getAnonymousLogger(
            String resourceBundleName) {
        return new Logger("", resourceBundleName);
    }

    public static synchronized Logger getLogger(String name) {
        return new Logger(name, null);
    }

    public static synchronized Logger getLogger(String name,
            String resourceBundleName) {
        return new Logger(name, resourceBundleName);
    }

    private String name;

    @SuppressWarnings("unused")
    private String resourceBundleName;

    public Logger() {
        super();
    }

    protected Logger(String name, String resourceBundleName) {
        this();
        this.name = name;
        this.resourceBundleName = resourceBundleName;
    }

    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public boolean isLoggable(Level level) {
        return true;
    }

    public void log(Level level, String msg) {
        log(level, msg, null);
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (level.hashCode() > Level.WARNING.hashCode()) {
            log(System.err, level, msg, thrown);
        } else {
            log(System.out, level, msg, thrown);
        }
    }

    private void log(OutputStream o, Level level, String msg, Throwable thrown) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(new Date());
            builder.append(" ").append(name);
            builder.append(" [").append(level.getName()).append("] ");
            builder.append(" ").append(msg);
            if (thrown != null) {
                builder.append(" ").append(thrown.getMessage());
            }
            o.write(builder.toString().getBytes());
        } catch (IOException e) {
        }
    }

    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

}
