/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;

/** Unit tests for {@link Service}. */
class ServiceTestCase {

    /** Minimal concrete fixture, since {@link Service} is abstract. */
    private static class TestService extends Service {
        TestService() {
            super();
        }

        TestService(boolean enabled) {
            super(enabled);
        }
    }

    @Test
    void defaultConstructor_isEnabledAndNotStarted() {
        TestService service = new TestService();

        assertTrue(service.isEnabled());
        assertFalse(service.isStarted());
        assertTrue(service.isStopped());
        assertNull(service.getContext());
    }

    @Test
    void constructorWithFlag_setsEnabled() {
        TestService disabled = new TestService(false);
        assertFalse(disabled.isEnabled());
    }

    @Test
    void setContext_roundTrip() {
        TestService service = new TestService();
        Context context = new Context();

        service.setContext(context);

        assertSame(context, service.getContext());
    }

    @Test
    void setEnabled_roundTrip() {
        TestService service = new TestService();

        service.setEnabled(false);

        assertFalse(service.isEnabled());
    }

    @Test
    void start_whenEnabled_setsStarted() throws Exception {
        TestService service = new TestService();

        service.start();

        assertTrue(service.isStarted());
        assertFalse(service.isStopped());
    }

    @Test
    void start_whenDisabled_staysStopped() throws Exception {
        TestService service = new TestService(false);

        service.start();

        assertFalse(service.isStarted());
        assertTrue(service.isStopped());
    }

    @Test
    void stop_afterStart_setsStopped() throws Exception {
        TestService service = new TestService();
        service.start();

        service.stop();

        assertFalse(service.isStarted());
        assertTrue(service.isStopped());
    }

    @Test
    void createInboundFilter_returnsNullByDefault() {
        TestService service = new TestService();

        assertNull(service.createInboundFilter(new Context()));
    }

    @Test
    void createOutboundFilter_returnsNullByDefault() {
        TestService service = new TestService();

        assertNull(service.createOutboundFilter(new Context()));
    }

    @Test
    void concreteSubclass_logService_isAlsoAService() {
        LogService logService = new LogService();
        assertTrue(logService.isEnabled());
    }

    @Test
    void concreteSubclass_encoderService_isAlsoAService() {
        EncoderService encoderService = new EncoderService();
        assertTrue(encoderService.isEnabled());
    }
}
