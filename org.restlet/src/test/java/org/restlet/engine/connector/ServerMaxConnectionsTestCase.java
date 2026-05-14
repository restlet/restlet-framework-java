/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.util.Series;

/** This tests the ability of the server to accept a fixed number of incoming connections. */
public class ServerMaxConnectionsTestCase extends BaseConnectorsTestCase {
    private static final Logger LOGGER =
            Logger.getLogger(ServerMaxConnectionsTestCase.class.getCanonicalName());
    private static final int CONNECTIONS_NUMBER = 1;
    private static final int CONCURRENT_REQUESTS = 2;
    private static final Duration SERVER_RESOURCE_FREEZE_DURATION = Duration.ofMillis(100);

    @Override
    protected void configureServer(final Server server) {
        super.configureServer(server);

        final Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("server.maxConnections", Integer.toString(CONNECTIONS_NUMBER));
        parameters.add(
                "connector.acceptors",
                Integer.toString(CONCURRENT_REQUESTS)); // server can accept all requests
    }

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        final CountDownLatch countDownLatch = new CountDownLatch(CONCURRENT_REQUESTS);

        final List<Boolean> testResults = new ArrayList<>();

        final Executor executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        final Runnable runnable =
                () -> {
                    testResults.add(sendGet(uri).isSuccess());
                    countDownLatch.countDown();
                    log("client countDownLatch.countDown done " + Thread.currentThread().getName());
                };

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executor.execute(runnable);
        }

        log("test before countDownLatch.await()");
        countDownLatch.await();
        log("test after countDownLatch.await()");

        assertEquals(CONCURRENT_REQUESTS, testResults.size());
        assertTrue(testResults.contains(true)); // One has succeeded
        assertTrue(testResults.contains(false)); // The other has failed
    }

    private static void log(final String message) {
        LOGGER.fine(message + " " + Thread.currentThread());
    }

    private Status sendGet(final String uri) {
        final Status result;

        log("client send get " + Thread.currentThread().getName());
        try {
            final Request request =
                    new Request(Method.GET, uri + "/" + Thread.currentThread().getName());
            final Client client = new Client(new Context(), Protocol.HTTP);
            final Response response = client.handle(request);
            log("client get sent " + Thread.currentThread().getName());
            result = response.getStatus();
            log("client status " + result + " " + Thread.currentThread().getName());
            response.release();
            client.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log("client done " + Thread.currentThread().getName());
        return result;
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                return new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        try {
                            log("server resource wait");
                            Thread.sleep(SERVER_RESOURCE_FREEZE_DURATION.toMillis());
                            log("server resource wait ended");
                            response.setStatus(Status.SUCCESS_NO_CONTENT);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
}
