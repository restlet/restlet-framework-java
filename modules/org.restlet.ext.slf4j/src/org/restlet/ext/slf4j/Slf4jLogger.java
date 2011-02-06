/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.slf4j;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JULI logger that efficiently wraps a SLF4J logger. It prevents the creation
 * of intermediary {@link LogRecord} objects in favor of direct calls to the
 * SLF4J API.
 * 
 * @author Jerome Louvel
 */
public class Slf4jLogger extends Logger {

    /** The wrapped SLF4J logger. */
    private org.slf4j.Logger slf4jLogger;

    /**
     * Constructor.
     * 
     * @param slf4jLogger
     *            The SLF4J logger to wrap.
     */
    public Slf4jLogger(org.slf4j.Logger slf4jLogger) {
        super(slf4jLogger.getName(), null);
        this.slf4jLogger = slf4jLogger;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The logger name.
     * @param resourceBundleName
     *            The optional resource bundle name.
     */
    protected Slf4jLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    /**
     * Logs a configuration message. By default, it invokes
     * {@link org.slf4j.Logger#debug(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void config(String msg) {
        getSlf4jLogger().debug(msg);
    }

    /**
     * Logs a fine trace. By default, it invokes
     * {@link org.slf4j.Logger#debug(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void fine(String msg) {
        getSlf4jLogger().debug(msg);
    }

    /**
     * Logs a finer trace. By default, it invokes
     * {@link org.slf4j.Logger#trace(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void finer(String msg) {
        getSlf4jLogger().trace(msg);
    }

    /**
     * Logs a finest trace. By default, it invokes
     * {@link org.slf4j.Logger#trace(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void finest(String msg) {
        getSlf4jLogger().trace(msg);
    }

    /**
     * Returns the wrapped SLF4J logger.
     * 
     * @return The wrapped SLF4J logger.
     */
    public org.slf4j.Logger getSlf4jLogger() {
        return slf4jLogger;
    }

    /**
     * Logs an info message. By default, it invokes
     * {@link org.slf4j.Logger#info(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void info(String msg) {
        getSlf4jLogger().info(msg);
    }

    @Override
    public boolean isLoggable(Level level) {
        if (Level.ALL == level) {
            return true;
        } else if (Level.CONFIG == level) {
            return getSlf4jLogger().isDebugEnabled();
        } else if (Level.FINE == level) {
            return getSlf4jLogger().isDebugEnabled();
        } else if (Level.FINER == level) {
            return getSlf4jLogger().isTraceEnabled();
        } else if (Level.FINEST == level) {
            return getSlf4jLogger().isTraceEnabled();
        } else if (Level.INFO == level) {
            return getSlf4jLogger().isInfoEnabled();
        } else if (Level.OFF == level) {
            return false;
        } else if (Level.SEVERE == level) {
            return getSlf4jLogger().isErrorEnabled();
        } else if (Level.WARNING == level) {
            return getSlf4jLogger().isWarnEnabled();
        } else {
            return false;
        }
    }

    @Override
    public void log(Level level, String msg) {
        if (Level.CONFIG == level) {
            getSlf4jLogger().debug(msg);
        } else if (Level.FINE == level) {
            getSlf4jLogger().debug(msg);
        } else if (Level.FINER == level) {
            getSlf4jLogger().trace(msg);
        } else if (Level.FINEST == level) {
            getSlf4jLogger().trace(msg);
        } else if (Level.INFO == level) {
            getSlf4jLogger().info(msg);
        } else if (Level.SEVERE == level) {
            getSlf4jLogger().error(msg);
        } else if (Level.WARNING == level) {
            getSlf4jLogger().warn(msg);
        }
    }

    @Override
    public void log(Level level, String msg, Object param) {
        if (Level.CONFIG == level) {
            getSlf4jLogger().debug(msg, param);
        } else if (Level.FINE == level) {
            getSlf4jLogger().debug(msg, param);
        } else if (Level.FINER == level) {
            getSlf4jLogger().trace(msg, param);
        } else if (Level.FINEST == level) {
            getSlf4jLogger().trace(msg, param);
        } else if (Level.INFO == level) {
            getSlf4jLogger().info(msg, param);
        } else if (Level.SEVERE == level) {
            getSlf4jLogger().error(msg, param);
        } else if (Level.WARNING == level) {
            getSlf4jLogger().warn(msg, param);
        }
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        if (Level.CONFIG == level) {
            getSlf4jLogger().debug(msg, params);
        } else if (Level.FINE == level) {
            getSlf4jLogger().debug(msg, params);
        } else if (Level.FINER == level) {
            getSlf4jLogger().trace(msg, params);
        } else if (Level.FINEST == level) {
            getSlf4jLogger().trace(msg, params);
        } else if (Level.INFO == level) {
            getSlf4jLogger().info(msg, params);
        } else if (Level.SEVERE == level) {
            getSlf4jLogger().error(msg, params);
        } else if (Level.WARNING == level) {
            getSlf4jLogger().warn(msg, params);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        if (Level.CONFIG == level) {
            getSlf4jLogger().debug(msg, thrown);
        } else if (Level.FINE == level) {
            getSlf4jLogger().debug(msg, thrown);
        } else if (Level.FINER == level) {
            getSlf4jLogger().trace(msg, thrown);
        } else if (Level.FINEST == level) {
            getSlf4jLogger().trace(msg, thrown);
        } else if (Level.INFO == level) {
            getSlf4jLogger().info(msg, thrown);
        } else if (Level.SEVERE == level) {
            getSlf4jLogger().error(msg, thrown);
        } else if (Level.WARNING == level) {
            getSlf4jLogger().warn(msg, thrown);
        }
    }

    @Override
    public void log(LogRecord record) {
        Level level = record.getLevel();
        String msg = record.getMessage();
        Object[] params = record.getParameters();
        Throwable thrown = record.getThrown();

        if (thrown != null) {
            log(level, msg, thrown);
        } else if (params != null) {
            log(level, msg, params);
        } else {
            log(level, msg);
        }
    }

    /**
     * Sets the wrapped SLF4J logger.
     * 
     * @param slf4jLogger
     *            The wrapped SLF4J logger.
     */
    public void setSlf4jLogger(org.slf4j.Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    /**
     * Logs a severe message. By default, it invokes
     * {@link org.slf4j.Logger#error(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void severe(String msg) {
        getSlf4jLogger().error(msg);
    }

    /**
     * Logs a warning message. By default, it invokes
     * {@link org.slf4j.Logger#warn(String)}.
     * 
     * @param msg
     *            The message to log.
     */
    @Override
    public void warning(String msg) {
        getSlf4jLogger().warn(msg);
    }

}
