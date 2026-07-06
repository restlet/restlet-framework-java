/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContextualRunnableTestCase {

    private static class TestContextualRunnable extends ContextualRunnable {
        volatile boolean ran;

        @Override
        public void run() {
            ran = true;
        }
    }

    @Test
    void constructor_capturesCurrentThreadContextClassLoader() {
        TestContextualRunnable runnable = new TestContextualRunnable();
        assertEquals(
                Thread.currentThread().getContextClassLoader(), runnable.getContextClassLoader());
    }

    @Test
    void setContextClassLoader_updatesValue() {
        TestContextualRunnable runnable = new TestContextualRunnable();
        ClassLoader custom = ContextualRunnableTestCase.class.getClassLoader();
        runnable.setContextClassLoader(custom);
        assertEquals(custom, runnable.getContextClassLoader());
    }

    @Test
    void run_invokesSubclassImplementation() {
        TestContextualRunnable runnable = new TestContextualRunnable();
        runnable.run();
        assertTrue(runnable.ran);
    }
}
