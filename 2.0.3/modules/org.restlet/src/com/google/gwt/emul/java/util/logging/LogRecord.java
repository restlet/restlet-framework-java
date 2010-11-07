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

import java.io.Serializable;
import java.util.Date;

/**
 * Emulate the LogRecord class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 * 
 */
public class LogRecord implements Serializable {
    /** */
    private static final long serialVersionUID = 1L;

    private Level level;

    private String loggerName = "";

    private String msg;

    private Throwable thrown = null;

    private long millis;

    public LogRecord(Level level, String msg) {
        this.level = level;
        this.msg = msg;
        millis = new Date().getTime();
    }

    protected LogRecord() {
    }

    public Level getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getMessage() {
        return msg;
    }

    public long getMillis() {
        return millis;
    }

    public Throwable getThrown() {
        return thrown;
    }

    public void setLevel(Level newLevel) {
        level = newLevel;
    }

    public void setLoggerName(String newName) {
        loggerName = newName;
    }

    public void setMessage(String newMessage) {
        msg = newMessage;
    }

    public void setMillis(long newMillis) {
        millis = newMillis;
    }

    public void setThrown(Throwable newThrown) {
        thrown = newThrown;
    }
}
