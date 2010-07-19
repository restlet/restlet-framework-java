/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.google.gwt.emul.java.util.logging;

import java.io.PrintStream;
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

    private void log(PrintStream o, Level level, String msg, Throwable thrown) {
        StringBuilder builder = new StringBuilder();
        builder.append(new Date());
        builder.append(" ").append(name);
        builder.append(" [").append(level.getName()).append("] ");
        builder.append(" ").append(msg);
        if (thrown != null) {
            builder.append(" ").append(thrown.getMessage());
        }
        o.println(builder.toString());
    }

    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

}
