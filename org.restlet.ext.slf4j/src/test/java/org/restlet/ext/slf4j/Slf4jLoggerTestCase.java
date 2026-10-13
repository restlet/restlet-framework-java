/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.slf4j;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/** Unit tests for {@link Slf4jLogger}. */
class Slf4jLoggerTestCase {

    private Slf4jLogger logger;

    @BeforeEach
    void setUp() {
        logger = new Slf4jLogger(LoggerFactory.getLogger(Slf4jLoggerTestCase.class.getName()));
    }

    @Test
    void testConstructor() {
        assertNotNull(logger.getSlf4jLogger());
        assertEquals(Slf4jLoggerTestCase.class.getName(), logger.getName());
    }

    @Test
    void testSetSlf4jLogger() {
        var newLogger = LoggerFactory.getLogger("other");
        logger.setSlf4jLogger(newLogger);
        assertEquals(newLogger, logger.getSlf4jLogger());
    }

    @Test
    void testLoggingMethods() {
        assertDoesNotThrow(
                () -> {
                    logger.config("config message");
                    logger.fine("fine message");
                    logger.finer("finer message");
                    logger.finest("finest message");
                    logger.info("info message");
                    logger.severe("severe message");
                    logger.warning("warning message");
                });
    }

    @Test
    void testIsLoggableFixedLevels() {
        assertTrue(logger.isLoggable(Level.ALL));
        assertFalse(logger.isLoggable(Level.OFF));
    }

    @Test
    void testIsLoggableAllLevels() {
        assertDoesNotThrow(
                () -> {
                    logger.isLoggable(Level.CONFIG);
                    logger.isLoggable(Level.FINE);
                    logger.isLoggable(Level.FINER);
                    logger.isLoggable(Level.FINEST);
                    logger.isLoggable(Level.INFO);
                    logger.isLoggable(Level.SEVERE);
                    logger.isLoggable(Level.WARNING);
                });
    }

    @Test
    void testIsLoggableUnknownLevel() {
        Level unknownLevel = new Level("CUSTOM", 450) {};
        assertFalse(logger.isLoggable(unknownLevel));
    }

    @Test
    void testLogLevelMessage() {
        assertDoesNotThrow(
                () -> {
                    logger.log(Level.CONFIG, "config");
                    logger.log(Level.FINE, "fine");
                    logger.log(Level.FINER, "finer");
                    logger.log(Level.FINEST, "finest");
                    logger.log(Level.INFO, "info");
                    logger.log(Level.SEVERE, "severe");
                    logger.log(Level.WARNING, "warning");
                    logger.log(Level.ALL, "unmapped");
                });
    }

    @Test
    void testLogLevelMessageObject() {
        assertDoesNotThrow(
                () -> {
                    logger.log(Level.CONFIG, "msg {}", "p");
                    logger.log(Level.FINE, "msg {}", "p");
                    logger.log(Level.FINER, "msg {}", "p");
                    logger.log(Level.FINEST, "msg {}", "p");
                    logger.log(Level.INFO, "msg {}", "p");
                    logger.log(Level.SEVERE, "msg {}", "p");
                    logger.log(Level.WARNING, "msg {}", "p");
                    logger.log(Level.ALL, "msg {}", "p");
                });
    }

    @Test
    void testLogLevelMessageObjects() {
        Object[] params = {"p1", "p2"};
        assertDoesNotThrow(
                () -> {
                    logger.log(Level.CONFIG, "msg {} {}", params);
                    logger.log(Level.FINE, "msg {} {}", params);
                    logger.log(Level.FINER, "msg {} {}", params);
                    logger.log(Level.FINEST, "msg {} {}", params);
                    logger.log(Level.INFO, "msg {} {}", params);
                    logger.log(Level.SEVERE, "msg {} {}", params);
                    logger.log(Level.WARNING, "msg {} {}", params);
                    logger.log(Level.ALL, "msg {} {}", params);
                });
    }

    @Test
    void testLogLevelMessageThrowable() {
        Throwable t = new RuntimeException("test");
        assertDoesNotThrow(
                () -> {
                    logger.log(Level.CONFIG, "msg", t);
                    logger.log(Level.FINE, "msg", t);
                    logger.log(Level.FINER, "msg", t);
                    logger.log(Level.FINEST, "msg", t);
                    logger.log(Level.INFO, "msg", t);
                    logger.log(Level.SEVERE, "msg", t);
                    logger.log(Level.WARNING, "msg", t);
                    logger.log(Level.ALL, "msg", t);
                });
    }

    @Test
    void testLogRecord_withThrowable() {
        LogRecord logRecord = new LogRecord(Level.SEVERE, "test");
        logRecord.setThrown(new RuntimeException("test"));
        assertDoesNotThrow(() -> logger.log(logRecord));
    }

    @Test
    void testLogRecord_withParams() {
        LogRecord logRecord = new LogRecord(Level.INFO, "test {} {}");
        logRecord.setParameters(new Object[] {"p1", "p2"});
        assertDoesNotThrow(() -> logger.log(logRecord));
    }

    @Test
    void testLogRecord_simple() {
        LogRecord logRecord = new LogRecord(Level.WARNING, "simple message");
        assertDoesNotThrow(() -> logger.log(logRecord));
    }

    @Test
    void testLocationAwareLoggerReceivesCallerBoundary() {
        List<Object[]> calls = new ArrayList<>();
        LocationAwareLogger fake =
                (LocationAwareLogger)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class<?>[] {LocationAwareLogger.class},
                                (proxy, method, args) -> {
                                    if ("log".equals(method.getName()) && args.length == 6) {
                                        calls.add(args);
                                    } else if ("getName".equals(method.getName())) {
                                        return "fake";
                                    }
                                    return null;
                                });
        Slf4jLogger wrapper = new Slf4jLogger(fake);
        Throwable t = new RuntimeException("test");

        wrapper.info("info");
        wrapper.log(Level.WARNING, "warn {}", "p");
        wrapper.log(Level.SEVERE, "error", t);
        wrapper.log(Level.FINEST, "ignored level mapping still trace");
        wrapper.log(Level.ALL, "unmapped");

        assertEquals(4, calls.size());
        for (Object[] call : calls) {
            assertEquals(Slf4jLogger.class.getName(), call[1]);
        }
        assertEquals(LocationAwareLogger.INFO_INT, calls.get(0)[2]);
        assertEquals(LocationAwareLogger.WARN_INT, calls.get(1)[2]);
        assertEquals(LocationAwareLogger.ERROR_INT, calls.get(2)[2]);
        assertEquals(t, calls.get(2)[5]);
        assertEquals(LocationAwareLogger.TRACE_INT, calls.get(3)[2]);
    }

    @Test
    void testPlainSlf4jLoggerFallback() {
        List<String> calls = new ArrayList<>();
        org.slf4j.Logger fake =
                (org.slf4j.Logger)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class<?>[] {org.slf4j.Logger.class},
                                (proxy, method, args) -> {
                                    String name = method.getName();
                                    if ("getName".equals(name)) {
                                        return "fake";
                                    } else if (!name.startsWith("is")) {
                                        calls.add(name + "/" + args.length);
                                    }
                                    return null;
                                });
        Slf4jLogger wrapper = new Slf4jLogger(fake);
        Throwable t = new RuntimeException("test");
        Level[] levels = {
            Level.FINEST, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE, Level.ALL
        };
        String[] expected = {"trace", "debug", "info", "warn", "error"};

        for (Level level : levels) {
            wrapper.log(level, "msg");
            wrapper.log(level, "msg {}", "p");
            wrapper.log(level, "msg {} {}", new Object[] {"p1", "p2"});
            wrapper.log(level, "msg", t);
        }

        assertEquals(20, calls.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i] + "/1", calls.get(i * 4));
            assertEquals(expected[i] + "/2", calls.get(i * 4 + 1));
            assertEquals(expected[i] + "/2", calls.get(i * 4 + 2));
            assertEquals(expected[i] + "/2", calls.get(i * 4 + 3));
        }
    }
}
