/**
 * Copyright 2005-2014 Restlet
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 *
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jetty;

import org.junit.jupiter.api.Test;
import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShutdownHookTestCase {
    private static final Logger LOGGER = Logger.getLogger("ShutdownHookTest");

    static {
         LOGGER.setLevel(Level.INFO);
    }

    /**
     * Validates that the server stops immediately when no requests are currently handled.
     */
    @Test
    public void whenServerIsNotHandlingRequestThenItStopsImmediately() throws Exception {
        // Given
        final Duration requestHangingTime = Duration.ofMinutes(1); // Test ALWAYS fails before that
        final Restlet hangingRestlet = newHangingRestlet(requestHangingTime);
        final Duration shutdownTimeout = Duration.ofSeconds(2);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        // When
        final Instant serverAskedToStopInstant = stopServer(server);

        // Then
        assertIntervalBetweenDatesEquals(Duration.ZERO, serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests are aborted immediately when graceful shutdown is OFF.
     *
     * This is done by making a request froze, then stopping the server, and checking that it didn't wait.
     */
    @Test
    public void whenServerIsHandlingBlockingRequestThenItStopsImmediately() throws Exception {
        // Given
        final Lock lock = new Lock();
        final Duration requestHangingTime = Duration.ofMinutes(1); // Test ALWAYS fails before that
        final Restlet hangingRestlet = newHangingAndLockedRestlet(requestHangingTime, lock);
        Server server = startServerWithoutGracefulShutdown(hangingRestlet);

        final TestClient testClient = new TestClient(server);
        new Thread(testClient).start();

        // When
        final boolean isResourceUnlocked = lock.awaitForUnlockingFor(Duration.ofSeconds(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        final boolean isClientResourceUnlocked = testClient.lock.awaitForUnlockingFor(Duration.ofSeconds(2));

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieved the request");
        assertTrue(testClient.cr.getStatus().isError(), "The request should have ended in error");
        assertIntervalBetweenDatesEquals(Duration.ZERO, serverAskedToStopInstant, testClient.stoppedAt);
        assertIntervalBetweenDatesEquals(Duration.ZERO, serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests are aborted after the server has waited for the timeout when graceful shutdown is ON.
     *
     * This is done by making a request froze, then stopping the server, and checking that it waited the expected amount of time before shutting down.
     */
    @Test
    public void whenServerIsHandlingBlockingRequestThenItGracefullyWaitsFor2SecondsBeforeStopping() throws Exception {
        // Given
        final Lock lock = new Lock();
        final Duration requestHangingTime = Duration.ofMinutes(1); // Test ALWAYS fails before that
        final Restlet hangingRestlet = newHangingAndLockedRestlet(requestHangingTime, lock);

        final Duration shutdownTimeout = Duration.ofSeconds(2);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        final TestClient testClient = new TestClient(server);
        new Thread(testClient).start();

        // When
        final boolean isResourceUnlocked = lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        final boolean isClientResourceUnlocked = testClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieved the request");
        assertTrue(testClient.cr.getStatus().isError(), "The request should have ended in error");

        Thread.sleep(100000);
        assertIntervalBetweenDatesEquals(shutdownTimeout, serverAskedToStopInstant, testClient.stoppedAt);
        assertIntervalBetweenDatesEquals(toJettyEffectiveTimeout(shutdownTimeout), serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that incoming requests are refused after the server is stopping when graceful shutdown is ON.
     *
     * This is done by making a request froze, then stopping the server, and checking that a new request is not taken into account.
     */
    @Test
    public void whenServerIsHandlingBlockingRequestThenItRefusesNewRequest() throws Exception {
        // Given
        final Lock lock = new Lock();
        final Duration requestHangingTime = Duration.ofMinutes(1); // Test ALWAYS fails before that
        final Restlet hangingRestlet = newHangingAndLockedRestlet(requestHangingTime, lock);

        final Duration shutdownTimeout = Duration.ofSeconds(2);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        final TestClient firstTestClient = new TestClient(server);
        new Thread(firstTestClient).start();

        // When
        final boolean isResourceUnlocked = lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        final TestClient blockedTestClient = new TestClient(server);
        blockedTestClient.run();
        final boolean isFirstClientResourceUnlocked = firstTestClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final boolean isBlockedClientResourceUnlocked = blockedTestClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isFirstClientResourceUnlocked, "The first client didn't achieved the request");
        assertTrue(isBlockedClientResourceUnlocked, "The \"blocked\" client didn't achieved the request");
        assertTrue(firstTestClient.cr.getStatus().isError(), "The request should have ended in error");
        assertIntervalBetweenDatesEquals(shutdownTimeout, serverAskedToStopInstant, firstTestClient.stoppedAt);
        assertTrue(blockedTestClient.cr.getStatus().isConnectorError(), "Any new client is blocked and fails with a connection error");
        assertIntervalBetweenDatesEquals(toJettyEffectiveTimeout(shutdownTimeout), serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests for a short amount of time are handled before the server has waited for the timeout when graceful shutdown is ON.
     *
     * This is done by making a request froze for a short amount of time, then stopping the server, and checking that the request has been handled.
     */
    @Test
    public void whenServerIsHandlingLongRequestThenRequestIsHandledCorrectlyBeforeStopping() throws Exception {
        // Given
        final Lock lock = new Lock();
        final Duration requestHangingTime = Duration.ofSeconds(2);
        final Restlet hangingRestlet = newHangingAndLockedRestlet(requestHangingTime, lock);

        final Duration shutdownTimeout = Duration.ofSeconds(20);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        final TestClient testClient = new TestClient(server);
        new Thread(testClient).start();

        // When
        final boolean isResourceUnlocked = lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        LOGGER.fine("Client resource wait lock");
        final boolean isClientResourceUnlocked = testClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        LOGGER.fine("Client resource unlocked");

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieve the request");
        assertEquals(Status.SUCCESS_OK, testClient.cr.getStatus());
        assertIntervalBetweenDatesIsLessThan(requestHangingTime, serverAskedToStopInstant, Instant.now());
        assertEquals("hello, world", testClient.responseText);
    }

    private static void assertIntervalBetweenDatesEquals(final Duration expectedDuration, final Instant firstInstant, final Instant secondInstant) {
        final Duration dateDifference = Duration.between(firstInstant, secondInstant)
                .abs();

        final Duration tolerance = Duration.ofMillis(200); // let's consider that +/- 200ms is fine
        final boolean isDateDifferenceNearlyEqualToExpectedDuration = dateDifference.minus(expectedDuration)
                .abs()
                .minus(tolerance)
                .isNegative();
        assertTrue(isDateDifferenceNearlyEqualToExpectedDuration, String.format("Expected delay: %d second(s) versus %d second(s)\n", expectedDuration.getSeconds(), dateDifference.getSeconds()));
    }

    private static void assertIntervalBetweenDatesIsLessThan(final Duration expectedDuration, final Instant firstInstant, final Instant secondInstant) {
        final Duration dateDifference = Duration.between(firstInstant, secondInstant).abs();

        final Duration tolerance = Duration.ofMillis(200); // let's consider that +/- 200ms is fine
        final boolean dateDifferenceIsLessThanExpectedDuration = dateDifference.minus(expectedDuration)
                .abs()
                .minus(tolerance)
                .isNegative();
        assertTrue(dateDifferenceIsLessThanExpectedDuration, String.format("difference between dates [%d second(s)] is higher than expected:  %d second(s)\n", dateDifference.getSeconds(), expectedDuration.getSeconds()));
    }

    private Server startServerWithoutGracefulShutdown(final Restlet restlet) throws Exception {
        return startServer(false, Duration.ZERO, restlet);
    }

    private Server startServerWithGracefulShutdown(final Duration timeout, final Restlet restlet) throws Exception {
        return startServer(true, timeout, restlet);
    }

    private Server startServer(final boolean graceful, final Duration timeout, final Restlet restlet) throws Exception {

        Engine.getInstance().getRegisteredServers().add(0, new HttpServerHelper(null)); // Creates a Jetty server helper manually
        // 0 port means it will be computed when the server starts
        Server server = new Server(new Context(), singletonList(Protocol.HTTP), null, 0, restlet, HttpServerHelper.class.getCanonicalName());

        if (graceful) {
            server.getContext().getParameters().add("lowResource.idleTimeout", Long.toString(timeout.toMillis() * 10));
        }
        server.getContext().getParameters().add("shutdown.gracefully", Boolean.toString(graceful));
        server.getContext().getParameters().add("shutdown.timeout", Long.toString(timeout.toMillis()));

        server.start();
        LOGGER.fine( "Server started on port " + server.getEphemeralPort());
        return server;
    }

    /**
     * Creates a resource that hangs for a specific amount of time before answering and acknowledges incoming requests
     * by unlocking the given lock.
     */
    private static Restlet newHangingAndLockedRestlet(final Duration requestHangingTime, final Lock lock) {
        return new Restlet() {
            @Override
            public void handle(final Request request, final Response response) {
                LOGGER.fine("Restlet opens lock");
                lock.unlock();
                LOGGER.fine("Restlet starts sleeping");
                try {
                    Thread.sleep(requestHangingTime.toMillis());
                } catch (Exception e) {
                    // silently stops, especially when Jetty server will abruptly quit after time out
                    LOGGER.log(Level.FINE, "Restlet error", e);
                }
                LOGGER.fine("Restlet woke up, answering");
                response.setEntity("hello, world", MediaType.TEXT_ALL);
            }
        };
    }

    /**
     * Creates a resource that hangs for a specific amount of time before answering.
     */
    private static Restlet newHangingRestlet(final Duration requestHangingTime) {
        return new Restlet() {
            @Override
            public void handle(final Request request, final Response response) {
                try {
                    Thread.sleep(requestHangingTime.toMillis());
                } catch (Exception e) {
                    // silently stops, especially when Jetty server will abruptly quit after time out
                    LOGGER.log(Level.FINE, "Restlet error", e);
                }
                LOGGER.log(Level.FINE, "Restlet woke up, answering");
                response.setEntity("hello, world", MediaType.TEXT_ALL);
            }
        };
    }

    private static class TestClient implements Runnable {
        private final ClientResource cr;
        private Instant stoppedAt = null;
        private final Lock lock; // ensures that the client has totally run
        private String responseText;

        public TestClient(final Server server) {
            cr = new ClientResource("http://localhost:" + server.getEphemeralPort());
            cr.setRetryOnError(false);
            this.lock = new Lock();
        }

        @Override
        public void run() {
            try {
                LOGGER.log(Level.FINE, "Client running");
                responseText = cr.get().getText();
            } catch (Exception e) { // silently ignore errors
                LOGGER.log(Level.FINE, "Client error", e);
            } finally {
                stoppedAt = Instant.now();
                lock.unlock();
                LOGGER.log(Level.FINE, "Client state: " + cr.getStatus());
            }
        }
    }

    private synchronized Instant stopServer(final Server server) {
        LOGGER.log(Level.FINE, "Server stopping");
        Instant serverAskedToStopInstant = Instant.now();
        try {
            final HttpServerHelper serverHelper = (HttpServerHelper) server.getContext().getAttributes().get("org.restlet.engine.helper");
            serverHelper.getWrappedServer().stop();
            LOGGER.log(Level.FINE, "Server stopped");
        } catch (Exception e) {
            // silently ignore errors
            LOGGER.log(Level.FINE, "Server stopped", e);
        }
        return serverAskedToStopInstant;
    }

    /**
     * Returns the effective Jetty timeout since there is an extra half-timeout in the {@link org.eclipse.jetty.util.thread.QueuedThreadPool}.
     */
    private Duration toJettyEffectiveTimeout(final Duration timeout) {
        return timeout.multipliedBy(3).dividedBy(2); // FIXME: needs improvements
    }

}
