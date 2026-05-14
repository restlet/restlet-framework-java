/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;

class ShutdownHookTestCase {
    private static final Logger LOGGER = Logger.getLogger("ShutdownHookTest");
    private static boolean shouldDebug = false;

    @BeforeEach
    void setUp() {
        LOGGER.setLevel(Level.FINE);
        Engine.clearThreadLocalVariables();
        Engine.register(true);
    }

    @AfterEach
    void tearDown() {
        Engine.clearThreadLocalVariables();
        Engine.register(true);
    }

    /** Validates that the server stops immediately when no requests are currently handled. */
    @Test
    void whenServerIsNotHandlingRequestThenItStopsImmediately() throws Exception {
        // Given a server resource that takes 1 min to send a response
        final Restlet hangingRestlet = newHangingRestlet(Duration.ofMinutes(1));
        // Given a server with a 3-seconds graceful shutdown
        final Server server =
                startServerWithGracefulShutdown(Duration.ofSeconds(3), hangingRestlet);

        // When the server stops
        final Instant serverAskedToStopInstant = stopServer(server);

        // Then the server stops immediately (no pending request)
        assertIntervalBetweenDatesEquals(Duration.ZERO, serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests are aborted immediately when graceful shutdown is OFF.
     *
     * <p>This is done by making a request froze, then stopping the server, and checking that it
     * didn't wait.
     */
    @Test
    void whenServerIsHandlingBlockingRequestThenItStopsImmediately() throws Exception {
        // Given a server resource that takes 1 min to send a response
        final Lock lock = new Lock("Server");
        final Restlet hangingRestlet = newHangingAndLockedRestlet(Duration.ofMinutes(1), lock);
        // Given a server without graceful shutdown
        Server server = startServerWithoutGracefulShutdown(hangingRestlet);

        // Given a client that sends a request
        final TestClient testClient = new TestClient(server);
        new Thread(testClient).start();

        // When we stop the server while there is a pending request
        log("before resource unlock");
        final boolean isResourceUnlocked = lock.awaitForUnlockingFor(Duration.ofSeconds(3));
        log("after resource unlock");
        log("before stopping server while request is pending");
        final Instant serverAskedToStopInstant = stopServer(server);
        log("before client unlock");
        final boolean isClientResourceUnlocked =
                testClient.lock.awaitForUnlockingFor(Duration.ofSeconds(4));
        log("after client unlock");

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieve the request");
        assertTrue(testClient.cr.getStatus().isError(), "The request should have ended in error");
        assertIntervalBetweenDatesEquals(
                Duration.ZERO, serverAskedToStopInstant, testClient.stoppedAt);
        assertIntervalBetweenDatesEquals(Duration.ZERO, serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests are aborted after the server has waited for the timeout when
     * graceful shutdown is ON.
     *
     * <p>This is done by making a request froze, then stopping the server, and checking that it
     * waited the expected amount of time before shutting down.
     */
    @Test
    void whenServerIsHandlingBlockingRequestThenItGracefullyWaitsFor1SecondBeforeStopping()
            throws Exception {
        // Given a server resource that takes 1 min to send a response
        final Lock serverLock = new Lock("Server");
        final Restlet hangingRestlet =
                newHangingAndLockedRestlet(Duration.ofMinutes(1), serverLock);

        // Given a server with a 1-second graceful shutdown
        final Duration shutdownTimeout = Duration.ofSeconds(1);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        // Given a client that sends a request
        final TestClient hangingClient = new TestClient(server);
        new Thread(hangingClient).start();

        // When we stop the server while there is a pending request
        final boolean isResourceUnlocked =
                serverLock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        log("Before ask server to stop");
        final Instant serverAskedToStopInstant = stopServer(server);
        log("After ask server to stop");
        final boolean isClientResourceUnlocked =
                hangingClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieved the request");
        assertTrue(
                hangingClient.cr.getStatus().isError(), "The request should have ended in error");

        assertIntervalBetweenDatesEquals(
                shutdownTimeout, serverAskedToStopInstant, hangingClient.stoppedAt);
        assertIntervalBetweenDatesEquals(
                toJettyEffectiveTimeout(shutdownTimeout), serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that incoming requests are refused after the server is stopping when graceful
     * shutdown is ON.
     *
     * <p>This is done by making a request froze, then stopping the server, and checking that a new
     * request is not taken into account.
     */
    @Test
    void whenServerIsHandlingBlockingRequestThenItRefusesNewRequest() throws Exception {
        // Given a server resource that takes 1 min to send a response
        final Lock lock = new Lock("Server");
        final Restlet hangingRestlet = newHangingAndLockedRestlet(Duration.ofMinutes(1), lock);

        // Given a server with a 1-second graceful shutdown
        final Duration shutdownTimeout = Duration.ofSeconds(1);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        // Given a client that sends a request
        final TestClient firstTestClient = new TestClient(server);
        new Thread(firstTestClient).start();

        // When
        final boolean isResourceUnlocked =
                lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        final TestClient blockedTestClient = new TestClient(server);
        blockedTestClient.run();
        final boolean isFirstClientResourceUnlocked =
                firstTestClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final boolean isBlockedClientResourceUnlocked =
                blockedTestClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isFirstClientResourceUnlocked, "The first client didn't achieved the request");
        assertTrue(
                isBlockedClientResourceUnlocked,
                "The \"blocked\" client didn't achieved the request");
        assertTrue(
                firstTestClient.cr.getStatus().isError(), "The request should have ended in error");
        assertIntervalBetweenDatesEquals(
                shutdownTimeout, serverAskedToStopInstant, firstTestClient.stoppedAt);
        assertTrue(
                blockedTestClient.cr.getStatus().isConnectorError(),
                "Any new client is blocked and fails with a connection error");
        assertIntervalBetweenDatesEquals(
                toJettyEffectiveTimeout(shutdownTimeout), serverAskedToStopInstant, Instant.now());
    }

    /**
     * Validates that hanging requests for a short amount of time are handled before the server has
     * waited for the timeout when graceful shutdown is ON.
     *
     * <p>This is done by making a request froze for a short amount of time, then stopping the
     * server, and checking that the request has been handled.
     */
    @Test
    void whenServerIsHandlingLongRequestThenRequestIsHandledCorrectlyBeforeStopping()
            throws Exception {
        // Given a server resource that takes 1 sec to send a response
        final Lock lock = new Lock("Server");
        Duration requestHangingTime = Duration.ofSeconds(1);
        final Restlet hangingRestlet = newHangingAndLockedRestlet(requestHangingTime, lock);

        // Given a server with a 20-seconds graceful shutdown
        final Duration shutdownTimeout = Duration.ofSeconds(20);
        final Server server = startServerWithGracefulShutdown(shutdownTimeout, hangingRestlet);

        // Given a client that sends a request
        final TestClient testClient = new TestClient(server);
        new Thread(testClient).start();

        // When
        final boolean isResourceUnlocked =
                lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        final Instant serverAskedToStopInstant = stopServer(server);
        log("Client resource wait lock");
        final boolean isClientResourceUnlocked =
                testClient.lock.awaitForUnlockingFor(shutdownTimeout.multipliedBy(2));
        log("Client resource unlocked");

        // Then
        assertTrue(isResourceUnlocked, "The resource didn't receive the request");
        assertTrue(isClientResourceUnlocked, "The client didn't achieve the request");
        assertEquals(Status.SUCCESS_OK, testClient.cr.getStatus());
        assertIntervalBetweenDatesIsLessThan(
                requestHangingTime, serverAskedToStopInstant, Instant.now());
        assertEquals("hello, world", testClient.responseText);
    }

    private static void assertIntervalBetweenDatesEquals(
            final Duration expectedDuration,
            final Instant firstInstant,
            final Instant secondInstant) {
        final Duration dateDifference = Duration.between(firstInstant, secondInstant).abs();

        final Duration tolerance = Duration.ofMillis(200); // let's consider that +/- 200ms is fine
        final boolean isDateDifferenceNearlyEqualToExpectedDuration =
                dateDifference.minus(expectedDuration).abs().minus(tolerance).isNegative();
        assertTrue(
                isDateDifferenceNearlyEqualToExpectedDuration,
                String.format(
                        "Expected delay: %d second(s) versus %d second(s)%n",
                        expectedDuration.toMillis(), dateDifference.toMillis()));
    }

    private static void assertIntervalBetweenDatesIsLessThan(
            final Duration expectedDuration,
            final Instant firstInstant,
            final Instant secondInstant) {
        final Duration dateDifference = Duration.between(firstInstant, secondInstant).abs();

        final Duration tolerance = Duration.ofMillis(200); // let's consider that +/- 200ms is fine
        final boolean dateDifferenceIsLessThanExpectedDuration =
                dateDifference.minus(expectedDuration).abs().minus(tolerance).isNegative();
        assertTrue(
                dateDifferenceIsLessThanExpectedDuration,
                String.format(
                        "difference between dates [%d second(s)] is higher than expected:  %d second(s)%n",
                        dateDifference.getSeconds(), expectedDuration.getSeconds()));
    }

    private Server startServerWithoutGracefulShutdown(final Restlet restlet) throws Exception {
        return startServer(false, Duration.ZERO, restlet);
    }

    private Server startServerWithGracefulShutdown(final Duration timeout, final Restlet restlet)
            throws Exception {
        return startServer(true, timeout, restlet);
    }

    private Server startServer(
            final boolean graceful, final Duration timeout, final Restlet restlet)
            throws Exception {

        Engine.getInstance()
                .getRegisteredServers()
                .addFirst(new HttpServerHelper(null)); // Creates a Jetty server helper manually
        // 0 port means it will be computed when the server starts
        Server server =
                new Server(
                        new Context(),
                        singletonList(Protocol.HTTP),
                        null,
                        0,
                        restlet,
                        HttpServerHelper.class.getCanonicalName());

        if (shouldDebug) {
            server.getContext().getParameters().add("tracing", "true");
            System.setProperty("org.eclipse.jetty.LEVEL", "TRACE");
            Engine.setLogLevel(Level.FINE);
        }

        if (graceful) {
            // Don't let the lowResource monitor mess with the current test
            server.getContext()
                    .getParameters()
                    .add("lowResource.idleTimeout", Long.toString(timeout.toMillis() * 10));
        }
        server.getContext().getParameters().add("shutdown.gracefully", Boolean.toString(graceful));
        server.getContext()
                .getParameters()
                .add("shutdown.timeout", Long.toString(timeout.toMillis()));

        server.start();
        log("Server started on port " + server.getEphemeralPort());
        return server;
    }

    /**
     * Creates a resource that hangs for a specific amount of time before answering and acknowledges
     * incoming requests by unlocking the given lock.
     */
    private static Restlet newHangingAndLockedRestlet(
            final Duration requestHangingTime, final Lock lock) {
        return new Restlet() {
            @Override
            public void handle(final Request request, final Response response) {
                log("Restlet opens lock");
                lock.unlock();
                log("Restlet starts sleeping");
                try {
                    Thread.sleep(requestHangingTime.toMillis());
                } catch (Exception e) {
                    // silently stops, especially when Jetty server will abruptly quit after time
                    // out
                    LOGGER.log(Level.FINE, "Restlet error", e);
                }
                log("Restlet woke up, answering");
                response.setEntity("hello, world", MediaType.TEXT_ALL);
            }
        };
    }

    /** Creates a resource that hangs for a specific amount of time before answering. */
    private static Restlet newHangingRestlet(final Duration requestHangingTime) {
        return new Restlet() {
            @Override
            public void handle(final Request request, final Response response) {
                try {
                    Thread.sleep(requestHangingTime.toMillis());
                } catch (Exception e) {
                    // silently stops, especially when Jetty server will abruptly quit after time
                    // out
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
            this.lock = new Lock("TestClient");
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
                LOGGER.log(Level.FINE, "Client state: {0}", cr.getStatus());
            }
        }
    }

    private synchronized Instant stopServer(final Server server) {
        log("Server stopping");
        Instant serverAskedToStopInstant = Instant.now();
        try {
            final HttpServerHelper serverHelper =
                    (HttpServerHelper)
                            server.getContext().getAttributes().get("org.restlet.engine.helper");
            serverHelper.getWrappedServer().stop();
            log("Server stopped");
        } catch (Exception e) {
            // silently ignore errors
            log("Server stopped", e);
        }
        return serverAskedToStopInstant;
    }

    /**
     * Returns the effective Jetty timeout since there is an extra half-timeout in the {@link
     * org.eclipse.jetty.util.thread.QueuedThreadPool}.
     */
    private Duration toJettyEffectiveTimeout(final Duration timeout) {
        return timeout.plusMillis(500); // FIXME: needs improvements
    }

    private static void log(final String message) {
        LOGGER.info(Instant.now().toString() + " " + message);
    }

    private static void log(final String message, final Exception exception) {
        LOGGER.log(Level.INFO, exception, () -> Instant.now().toString() + " " + message);
    }
}
