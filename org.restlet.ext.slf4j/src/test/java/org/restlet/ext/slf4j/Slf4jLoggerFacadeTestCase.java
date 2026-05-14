/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Slf4jLoggerFacade}. */
class Slf4jLoggerFacadeTestCase {

    @Test
    void testGetAnonymousLogger() {
        Slf4jLoggerFacade facade = new Slf4jLoggerFacade();
        Logger logger = facade.getAnonymousLogger();

        assertNotNull(logger);
        assertInstanceOf(Slf4jLogger.class, logger);
        assertEquals("", logger.getName());
    }

    @Test
    void testGetLogger() {
        Slf4jLoggerFacade facade = new Slf4jLoggerFacade();
        String loggerName = "org.restlet.test";
        Logger logger = facade.getLogger(loggerName);

        assertNotNull(logger);
        assertInstanceOf(Slf4jLogger.class, logger);
        assertEquals(loggerName, logger.getName());
    }
}
