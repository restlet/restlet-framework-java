/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.logging.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/** Unit tests for {@link DefaultSaxHandler}. */
class DefaultSaxHandlerTestCase {

    private Level previousLevel;

    @BeforeEach
    void setUp() {
        previousLevel = Context.getCurrentLogger().getLevel();
        Context.getCurrentLogger().setLevel(Level.CONFIG);
    }

    @AfterEach
    void tearDown() {
        Context.getCurrentLogger().setLevel(previousLevel);
    }

    private SAXParseException newException() {
        return new SAXParseException("boom", "public-id", "system-id", 1, 2);
    }

    @Test
    void error_logsAndDoesNotThrow() throws Exception {
        new DefaultSaxHandler().error(newException());
    }

    @Test
    void fatalError_logs() {
        new DefaultSaxHandler().fatalError(newException());
    }

    @Test
    void warning_logs() throws Exception {
        new DefaultSaxHandler().warning(newException());
    }

    @Test
    void resolveEntity_delegatesToSuperAndLogs() throws Exception {
        InputSource result = new DefaultSaxHandler().resolveEntity("public-id", "system-id");
        assertNull(result);
    }

    @Test
    void resolveResource_alwaysReturnsNull() {
        var result =
                new DefaultSaxHandler()
                        .resolveResource(
                                "type", "namespace-uri", "public-id", "system-id", "base-uri");
        assertNull(result);
    }

    @Test
    void skippedEntity_logs() throws Exception {
        new DefaultSaxHandler().skippedEntity("entity-name");
    }
}
