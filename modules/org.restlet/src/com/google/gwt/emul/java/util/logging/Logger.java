/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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

    private Level level;

    private String name;

    private Logger parent;

    private boolean useParentHandlers;

    @SuppressWarnings("unused")
    private String resourceBundleName;

    public Logger() {
        super();
        this.level = Level.INFO;
        this.useParentHandlers = false;
    }

    protected Logger(String name, String resourceBundleName) {
        this();
        this.name = name;
        this.resourceBundleName = resourceBundleName;
    }

    public Level getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    public Logger getParent() {
        return this.parent;
    }

    public boolean getUseParentHandlers() {
        return useParentHandlers;
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

    public void log(LogRecord record) {
        if(record != null && record.getLevel()!=null){
            log(record.getLevel(), record.getMessage());
        }
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

    public void setLevel(Level newLevel) {
        this.level = newLevel;
    }

    public void setParent(Logger newParent) {
        this.parent = newParent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUseParentHandlers(boolean newUseParentHandlers) {
        this.useParentHandlers = newUseParentHandlers;
    }

    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

}
