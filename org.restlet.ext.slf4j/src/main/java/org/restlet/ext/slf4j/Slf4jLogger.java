/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.slf4j;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.slf4j.spi.LocationAwareLogger;

/**
 * JULI logger that efficiently wraps a SLF4J logger. It prevents the creation of intermediary
 * {@link LogRecord} objects in favor of direct calls to the SLF4J API.
 *
 * @author Jerome Louvel
 */
public class Slf4jLogger extends Logger {

    /** The wrapped SLF4J logger. */
    private org.slf4j.Logger wrappedSlf4jLogger;

    /**
     * Constructor.
     *
     * @param wrappedSlf4jLogger The SLF4J logger to wrap.
     */
    public Slf4jLogger(org.slf4j.Logger wrappedSlf4jLogger) {
        super(wrappedSlf4jLogger.getName(), null);
        this.wrappedSlf4jLogger = wrappedSlf4jLogger;
    }

    /**
     * Constructor.
     *
     * @param name The logger name.
     * @param resourceBundleName The optional resource bundle name.
     */
    protected Slf4jLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    /**
     * Logs a configuration message. By default, it invokes {@link org.slf4j.Logger#debug(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void config(String msg) {
        doLog(Level.CONFIG, msg, null, null);
    }

    /**
     * Logs a fine trace. By default, it invokes {@link org.slf4j.Logger#debug(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void fine(String msg) {
        doLog(Level.FINE, msg, null, null);
    }

    /**
     * Logs a finer trace. By default, it invokes {@link org.slf4j.Logger#trace(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void finer(String msg) {
        doLog(Level.FINER, msg, null, null);
    }

    /**
     * Logs a finest trace. By default, it invokes {@link org.slf4j.Logger#trace(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void finest(String msg) {
        doLog(Level.FINEST, msg, null, null);
    }

    /**
     * Returns the wrapped SLF4J logger.
     *
     * @return The wrapped SLF4J logger.
     */
    public org.slf4j.Logger getSlf4jLogger() {
        return wrappedSlf4jLogger;
    }

    /**
     * Logs an info message. By default, it invokes {@link org.slf4j.Logger#info(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void info(String msg) {
        doLog(Level.INFO, msg, null, null);
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
        doLog(level, msg, null, null);
    }

    @Override
    public void log(Level level, String msg, Object param) {
        doLog(level, msg, new Object[] {param}, null);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        doLog(level, msg, params, null);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        doLog(level, msg, null, thrown);
    }

    /**
     * Maps a JULI level to the matching SLF4J level.
     *
     * @param level The JULI level.
     * @return The {@link LocationAwareLogger} level constant, or -1 if the level isn't mapped.
     */
    private static int toSlf4jLevel(Level level) {
        if (Level.CONFIG == level || Level.FINE == level) {
            return LocationAwareLogger.DEBUG_INT;
        } else if (Level.FINER == level || Level.FINEST == level) {
            return LocationAwareLogger.TRACE_INT;
        } else if (Level.INFO == level) {
            return LocationAwareLogger.INFO_INT;
        } else if (Level.SEVERE == level) {
            return LocationAwareLogger.ERROR_INT;
        } else if (Level.WARNING == level) {
            return LocationAwareLogger.WARN_INT;
        }
        return -1;
    }

    /**
     * Forwards a message to the wrapped SLF4J logger. When the latter is location aware, this class
     * name is given as the caller boundary so that the actual caller class, method and line number
     * are reported by the logging backend instead of this wrapper.
     *
     * @param level The JULI level.
     * @param msg The message to log.
     * @param params The optional message parameters.
     * @param thrown The optional throwable.
     */
    private void doLog(Level level, String msg, Object[] params, Throwable thrown) {
        int slf4jLevel = toSlf4jLevel(level);

        if (slf4jLevel < 0) {
            return;
        }

        org.slf4j.Logger slf4j = getSlf4jLogger();

        if (slf4j instanceof LocationAwareLogger locationAware) {
            locationAware.log(null, getCallerBoundary(), slf4jLevel, msg, params, thrown);
        } else if (thrown != null) {
            switch (slf4jLevel) {
                case LocationAwareLogger.TRACE_INT -> slf4j.trace(msg, thrown);
                case LocationAwareLogger.DEBUG_INT -> slf4j.debug(msg, thrown);
                case LocationAwareLogger.INFO_INT -> slf4j.info(msg, thrown);
                case LocationAwareLogger.WARN_INT -> slf4j.warn(msg, thrown);
                default -> slf4j.error(msg, thrown);
            }
        } else if (params != null) {
            switch (slf4jLevel) {
                case LocationAwareLogger.TRACE_INT -> slf4j.trace(msg, params);
                case LocationAwareLogger.DEBUG_INT -> slf4j.debug(msg, params);
                case LocationAwareLogger.INFO_INT -> slf4j.info(msg, params);
                case LocationAwareLogger.WARN_INT -> slf4j.warn(msg, params);
                default -> slf4j.error(msg, params);
            }
        } else {
            switch (slf4jLevel) {
                case LocationAwareLogger.TRACE_INT -> slf4j.trace(msg);
                case LocationAwareLogger.DEBUG_INT -> slf4j.debug(msg);
                case LocationAwareLogger.INFO_INT -> slf4j.info(msg);
                case LocationAwareLogger.WARN_INT -> slf4j.warn(msg);
                default -> slf4j.error(msg);
            }
        }
    }

    /**
     * Returns the fully qualified name of the class after which the caller is searched in the stack
     * trace. Subclasses that add their own logging methods should override it.
     *
     * @return The caller boundary class name.
     */
    protected String getCallerBoundary() {
        return Slf4jLogger.class.getName();
    }

    @Override
    public void log(LogRecord logRecord) {
        Level level = logRecord.getLevel();
        String msg = logRecord.getMessage();
        Object[] params = logRecord.getParameters();
        Throwable thrown = logRecord.getThrown();

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
     * @param wrappedSlf4jLogger The wrapped SLF4J logger.
     */
    public void setSlf4jLogger(org.slf4j.Logger wrappedSlf4jLogger) {
        this.wrappedSlf4jLogger = wrappedSlf4jLogger;
    }

    /**
     * Logs a severe message. By default, it invokes {@link org.slf4j.Logger#error(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void severe(String msg) {
        doLog(Level.SEVERE, msg, null, null);
    }

    /**
     * Logs a warning message. By default, it invokes {@link org.slf4j.Logger#warn(String)}.
     *
     * @param msg The message to log.
     */
    @Override
    public void warning(String msg) {
        doLog(Level.WARNING, msg, null, null);
    }
}
