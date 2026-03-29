/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.restlet.Server;
import org.restlet.engine.adapter.JettyServerCall;

/**
 * Abstract Jetty web server connector. Here is the list of parameters that are supported. They
 * should be set in the Server's context before it is started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>threadPool.minThreads</td>
 * <td>int</td>
 * <td>8</td>
 * <td>Thread pool minimum threads</td>
 * </tr>
 * <tr>
 * <td>threadPool.maxThreads</td>
 * <td>int</td>
 * <td>200</td>
 * <td>Thread pool max threads</td>
 * </tr>
 * <tr>
 * <td>threadPool.threadsPriority</td>
 * <td>int</td>
 * <td>{@link Thread#NORM_PRIORITY}</td>
 * <td>Thread pool threads priority</td>
 * </tr>
 * <tr>
 * <td>threadPool.idleTimeout</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Thread pool idle timeout in milliseconds; threads that are idle for
 * longer than this period may be stopped</td>
 * </tr>
 * <tr>
 * <td>threadPool.stopTimeout</td>
 * <td>long</td>
 * <td>5000</td>
 * <td>Thread pool stop timeout in milliseconds; the maximum time allowed for
 * the service to shutdown</td>
 * </tr>
 * <tr>
 * <td>connector.acceptors</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Connector acceptor thread count; when -1, Jetty will default to 1</td>
 * </tr>
 * <tr>
 * <td>connector.selectors</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Connector selector thread count; When less or equal than 0, Jetty
 * computes a default value derived from a heuristic over available CPUs and
 * thread pool size.</td>
 * </tr>
 * <tr>
 * <td>connector.acceptQueueSize</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Connector accept queue size; also known as accept backlog</td>
 * </tr>
 * <tr>
 * <td>connector.idleTimeout</td>
 * <td>int</td>
 * <td>30000</td>
 * <td>Connector idle timeout in milliseconds; see
 * {@link Socket#setSoTimeout(int)}; this value is interpreted as the maximum
 * time between some progress being made on the connection; so if a single byte
 * is read or written, then the timeout is reset</td>
 * </tr>
 * <tr>
 * <td>connector.stopTimeout</td>
 * <td>long</td>
 * <td>30000</td>
 * <td>Connector stop timeout in milliseconds; the maximum time allowed for the
 * service to shutdown</td>
 * </tr>
 * <tr>
 * <td>http.headerCacheSize</td>
 * <td>int</td>
 * <td>512</td>
 * <td>HTTP header cache size in bytes</td>
 * </tr>
 * <tr>
 * <td>http.requestHeaderSize</td>
 * <td>int</td>
 * <td>8*1024</td>
 * <td>HTTP request header size in bytes; larger headers will allow for more
 * and/or larger cookies plus larger form content encoded in a URL; however,
 * larger headers consume more memory and can make a server more vulnerable to
 * denial of service attacks</td>
 * </tr>
 * <tr>
 * <td>http.responseHeaderSize</td>
 * <td>int</td>
 * <td>8*1024</td>
 * <td>HTTP response header size in bytes; larger headers will allow for more
 * and/or larger cookies and longer HTTP headers (e.g., for redirection);
 * however, larger headers will also consume more memory</td>
 * </tr>
 * <tr>
 * <td>http.outputBufferSize</td>
 * <td>int</td>
 * <td>32*1024</td>
 * <td>HTTP output buffer size in bytes; a larger buffer can improve performance
 * by allowing a content producer to run without blocking, however larger
 * buffers consume more memory and may induce some latency before a client
 * starts processing the content</td>
 * </tr>
 * <tr>
 * <td>lowResource.period</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>Low-resource monitor period in milliseconds; when 0, low-resource
 * monitoring is disabled</td>
 * </tr>
 * <tr>
 * <td>lowResource.threads</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Low-resource monitor, whether to check if we're low on threads</td>
 * </tr>
 * <tr>
 * <td>lowResource.maxMemory</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Low-resource monitor max memory in bytes; when 0, the check disabled;
 * memory used is calculated as (totalMemory-freeMemory)</td>
 * </tr>
 * <tr>
 * <td>lowResource.idleTimeout</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>Low-resource monitor idle timeout in milliseconds; applied to EndPoints
 * when in the low-resources state</td>
 * </tr>
 * <tr>
 * <td>server.maxConnections</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Server max connections; when 0, there is no limit</td>
 * </tr>
 * <tr>
 * <td>server.maxConnections.idleTimeout</td>
 * <td>long</td>
 * <td>0</td>
 * <td>The endpoint idle timeout in milliseconds to apply when the connection
 * limit is reached; when 0, there is no idle timeout.</td>
 * </tr>
 * <tr>
 * <td>shutdown.gracefully</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>When true, upon JVM shutdown, the Jetty server will block incoming
 * requests and wait for pending requests to end before shutting down.
 * Otherwise, incoming requests are not blocked and all requests are
 * aborted.</td>
 * </tr>
 * <tr>
 * <td>shutdown.timeout</td>
 * <td>long</td>
 * <td>30000</td>
 * <td>Server shutdown timeout in milliseconds. Defaults to 30000.</td>
 * </tr>
 * </table>
 *
 * @see <a href= "https://jetty.org/docs/jetty/12/index.html">Jetty 12 documentation</a>
 * @author Jerome Louvel
 * @author Tal Liron
 */
public abstract class JettyServerHelper extends org.restlet.engine.adapter.HttpServerHelper {

    /** The wrapped Jetty server. */
    private volatile org.eclipse.jetty.server.Server wrappedServer;

    /**
     * Constructor.
     *
     * @param server The server to help.
     */
    protected JettyServerHelper(Server server) {
        super(server);
    }

    /**
     * Creates a Jetty HTTP configuration.
     *
     * @return A Jetty HTTP configuration.
     */
    protected HttpConfiguration createHttpConfiguration() {
        final HttpConfiguration configuration = new HttpConfiguration();
        configuration.setHeaderCacheSize(getHttpHeaderCacheSize());
        configuration.setRequestHeaderSize(getHttpRequestHeaderSize());
        configuration.setResponseHeaderSize(getHttpResponseHeaderSize());
        configuration.setOutputBufferSize(getHttpOutputBufferSize());

        // ask Jetty connector to let us handling the Date header,
        // otherwise two Date headers are generated in the request
        configuration.setSendDateHeader(false);

        return configuration;
    }

    /**
     * Creates new internal Jetty connection factories.
     *
     * @param configuration The HTTP configuration.
     * @return New internal Jetty connection factories.
     */
    protected abstract ConnectionFactory[] createConnectionFactories(
            final HttpConfiguration configuration);

    /**
     * Creates the Jetty connectors.
     *
     * @param server The Jetty server.
     * @return The Jetty connectors.
     */
    protected abstract List<Connector> createConnectors(org.eclipse.jetty.server.Server server);

    /**
     * Creates a Jetty connector based on a classical TCP type of transport.
     *
     * @param server The Jetty server.
     * @return A Jetty connector.
     */
    protected ServerConnector createServerConnector(
            final org.eclipse.jetty.server.Server server, final HttpConfiguration configuration) {
        final int acceptors = getConnectorAcceptors();
        final int selectors = getConnectorSelectors();
        final Executor executor = getConnectorExecutor();
        final Scheduler scheduler = getConnectorScheduler();
        final ByteBufferPool byteBufferPool = getConnectorByteBufferPool();

        final ConnectionFactory[] connectionFactories = createConnectionFactories(configuration);

        final ServerConnector connector =
                new ServerConnector(
                        server,
                        executor,
                        scheduler,
                        byteBufferPool,
                        acceptors,
                        selectors,
                        connectionFactories);

        final String address = getHelped().getAddress();
        if (address != null) {
            connector.setHost(address);
        }
        connector.setPort(getHelped().getPort());
        connector.setIdleTimeout(getConnectorIdleTimeout());
        connector.setShutdownIdleTimeout(getShutdownTimeout());

        connector.setAcceptQueueSize(getConnectorAcceptQueueSize());
        return connector;
    }

    /**
     * Creates a Jetty low-resource monitor.
     *
     * @param server A Jetty server.
     * @return A Jetty low-resource monitor or null.
     */
    private LowResourceMonitor createLowResourceMonitor(org.eclipse.jetty.server.Server server) {
        final LowResourceMonitor result;

        final int period = getLowResourceMonitorPeriod();
        if (period > 0) {
            result = new LowResourceMonitor(server);
            result.setMonitoredConnectors(Arrays.asList(server.getConnectors()));
            result.setPeriod(period);
            result.setMonitorThreads(getLowResourceMonitorThreads());
            result.setMaxMemory(getLowResourceMonitorMaxMemory());
            result.setLowResourcesIdleTimeout(getLowResourceMonitorIdleTimeout());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Creates a Jetty server.
     *
     * @return A Jetty server.
     */
    private org.eclipse.jetty.server.Server createServer() {
        // Thread pool
        final ThreadPool threadPool = createThreadPool();

        // Server
        final org.eclipse.jetty.server.Server jettyServer =
                new org.eclipse.jetty.server.Server(threadPool);

        int serverMaxConnections = getServerMaxConnections();
        if (serverMaxConnections > 0) {
            ConnectionLimit connectionLimit =
                    new ConnectionLimit(serverMaxConnections, jettyServer);
            connectionLimit.setIdleTimeout(getServerMaxConnectionsIdleTimeout());
            jettyServer.addBean(connectionLimit);
        }

        jettyServer.setStopAtShutdown(getShutdownGracefully());
        if (getShutdownGracefully()) {
            jettyServer.setStopTimeout(getShutdownTimeout());
        } else {
            jettyServer.setStopTimeout(0);
        }

        jettyServer.setHandler(createJettyHandler());

        // Connectors
        createConnectors(jettyServer).forEach(jettyServer::addConnector);

        // Low-resource monitor (must be created after connectors have been added)
        LowResourceMonitor lowResourceMonitor = createLowResourceMonitor(jettyServer);
        jettyServer.addBean(lowResourceMonitor);

        return jettyServer;
    }

    /**
     * Creates the Jetty handler that wraps the {@link JettyServerHelper}.
     *
     * @return the Jetty handler that wraps the {@link JettyServerHelper}.
     */
    private Handler.Abstract createJettyHandler() {
        final Handler.Abstract result;

        final JettyServerHelper jettyServerHelper = this;
        Handler.Abstract jettyServerHelperWrapperHandler =
                new Handler.Abstract() {
                    @Override
                    public boolean handle(Request request, Response response, Callback callback) {
                        JettyServerCall httpCall =
                                new JettyServerCall(
                                        jettyServerHelper.getHelped(), request, response, callback);
                        jettyServerHelper.handle(httpCall);
                        return true; // Indicates that the request is accepted
                    }
                };

        if (getShutdownGracefully()) {
            // StatisticsHandler for graceful shutdown
            final StatisticsHandler statisticsHandler = new StatisticsHandler();
            statisticsHandler.setHandler(jettyServerHelperWrapperHandler);
            result = statisticsHandler;
        } else {
            result = jettyServerHelperWrapperHandler;
        }

        return result;
    }

    /**
     * Creates a Jetty thread pool.
     *
     * @return A Jetty thread pool.
     */
    private ThreadPool createThreadPool() {
        final QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(getThreadPoolMinThreads());
        threadPool.setMaxThreads(getThreadPoolMaxThreads());
        threadPool.setThreadsPriority(getThreadPoolThreadsPriority());
        threadPool.setIdleTimeout(getThreadPoolIdleTimeout());
        if (getShutdownGracefully()) {
            threadPool.setStopTimeout(getThreadPoolStopTimeout());
        } else {
            threadPool.setStopTimeout(0); // The thread pool stops immediately.
        }

        return threadPool;
    }

    /**
     * Connector acceptor thread count. Defaults to -1. When -1, Jetty will default to 1.
     *
     * @return Connector acceptor thread count.
     */
    public int getConnectorAcceptors() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("connector.acceptors", "-1"));
    }

    /**
     * Connector "accept" queue size. Defaults to 0.
     *
     * <p>Also known as "accept" backlog.
     *
     * @return Connector accept queue size.
     */
    public int getConnectorAcceptQueueSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("connector.acceptQueueSize", "0"));
    }

    /**
     * Connector byte buffer pool. Defaults to null. When null, will use a new {@link
     * ArrayByteBufferPool}.
     *
     * @return Connector byte buffer pool or null.
     */
    public ByteBufferPool getConnectorByteBufferPool() {
        return null;
    }

    /**
     * Connector executor. Defaults to null. When null, will use the server's thread pool.
     *
     * @return Connector executor or null.
     */
    public Executor getConnectorExecutor() {
        return null;
    }

    /**
     * Connector idle timeout in milliseconds. Defaults to 30000.
     *
     * <p>See {@link Socket#setSoTimeout(int)}.
     *
     * <p>This value is interpreted as the maximum time between some progress being made on the
     * connection. So if a single byte is read or written, then the timeout is reset.
     *
     * @return Connector idle timeout.
     */
    public int getConnectorIdleTimeout() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("connector.idleTimeout", "30000"));
    }

    /**
     * Connector scheduler. Defaults to null. When null, will use a new {@link
     * ScheduledExecutorScheduler}.
     *
     * @return Connector scheduler or null.
     */
    public Scheduler getConnectorScheduler() {
        return null;
    }

    /**
     * Connector selector thread count. Defaults to -1. When less or equal than 0, Jetty computes a
     * default value derived from a heuristic over available CPUs and thread pool size.
     *
     * @return Connector acceptor thread count.
     */
    public int getConnectorSelectors() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("connector.selectors", "-1"));
    }

    /**
     * HTTP header cache size in bytes. Defaults to 512.
     *
     * @return HTTP header cache size.
     */
    public int getHttpHeaderCacheSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.headerCacheSize", "1024"));
    }

    /**
     * HTTP output buffer size in bytes. Defaults to 32*1024.
     *
     * <p>A larger buffer can improve performance by allowing a content producer to run without
     * blocking, however, larger buffers consume more memory and may induce some latency before a
     * client starts processing the content.
     *
     * @return HTTP output buffer size.
     */
    public int getHttpOutputBufferSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.outputBufferSize", "32768"));
    }

    /**
     * HTTP request header size in bytes. Defaults to 8*1024.
     *
     * <p>Larger headers will allow for more and/or larger cookies plus larger form content encoded
     * in a URL. However, larger headers consume more memory and can make a server more vulnerable
     * to denial of service attacks.
     *
     * @return HTTP request header size.
     */
    public int getHttpRequestHeaderSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.requestHeaderSize", "8192"));
    }

    /**
     * HTTP response header size in bytes. Defaults to 8*1024.
     *
     * <p>Larger headers will allow for more and/or larger cookies and longer HTTP headers (e.g.,
     * for redirection). However, larger headers will also consume more memory.
     *
     * @return HTTP response header size.
     */
    public int getHttpResponseHeaderSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.responseHeaderSize", "8192"));
    }

    /**
     * Low-resource monitor idle timeout in milliseconds. Defaults to 1000.
     *
     * <p>Applied to EndPoints when in the low-resources state.
     *
     * @return Low-resource monitor idle timeout.
     */
    public int getLowResourceMonitorIdleTimeout() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("lowResource.idleTimeout", "1000"));
    }

    /**
     * Low-resource monitor max memory in bytes. Defaults to 0. When 0, the check is disabled.
     *
     * <p>Memory used is calculated as (totalMemory-freeMemory).
     *
     * @return Low-resource monitor max memory.
     */
    public long getLowResourceMonitorMaxMemory() {
        return Long.parseLong(getHelpedParameters().getFirstValue("lowResource.maxMemory", "0"));
    }

    /**
     * Low-resource monitor period in milliseconds. Defaults to 1000. When 0, low-resource
     * monitoring is disabled.
     *
     * @return Low-resource monitor period.
     */
    public int getLowResourceMonitorPeriod() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("lowResource.period", "1000"));
    }

    /**
     * Low-resource monitor, whether to check if we're low on threads. Defaults to true.
     *
     * @return Low-resource monitor threads.
     */
    public boolean getLowResourceMonitorThreads() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("lowResource.threads", "true"));
    }

    /**
     * Server max connections. Defaults to 0. When 0, the check is disabled.
     *
     * @return Low-resource monitor max connections.
     */
    public int getServerMaxConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("server.maxConnections", "0"));
    }

    /**
     * The endpoint idle timeout in milliseconds to apply when the connection limit is reached.
     * Defaults to 0. When 0, there is no idle timeout.
     *
     * <p>The maximum time allowed for the endpoint to close when the connection limit is reached.
     *
     * @return The endpoint idle timeout in milliseconds to apply when the connection limit is
     *     reached.
     */
    public long getServerMaxConnectionsIdleTimeout() {
        return Long.parseLong(
                getHelpedParameters().getFirstValue("server.maxConnections.idleTimeout", "0"));
    }

    /**
     * When true, upon JVM shutdown, the Jetty server will block incoming requests and wait for
     * pending requests to end before shutting down. Otherwise, incoming requests are not blocked
     * and all requests are aborted. Defaults to true.
     *
     * @return True if upon JVM shutdown, the Jetty server will block incoming requests and wait for
     *     pending requests to end before shutting down. Otherwise, incoming requests are not
     *     blocked and all requests are aborted.
     */
    public boolean getShutdownGracefully() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("shutdown.gracefully", "true"));
    }

    /**
     * Server shutdown timeout in milliseconds. Defaults to 30000.
     *
     * @return Server shutdown timeout.
     */
    public long getShutdownTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue("shutdown.timeout", "0"));
    }

    /**
     * Thread pool idle timeout in milliseconds. Defaults to 60000.
     *
     * <p>Threads that are idle for longer than this period may be stopped.
     *
     * @return Thread pool idle timeout.
     */
    public int getThreadPoolIdleTimeout() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("threadPool.idleTimeout", "60000"));
    }

    /**
     * Thread pool maximum threads. Defaults to 200.
     *
     * @return Thread pool maximum threads.
     */
    public int getThreadPoolMaxThreads() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("threadPool.maxThreads", "200"));
    }

    /**
     * Thread pool minimum threads. Defaults to 8.
     *
     * @return Thread pool minimum threads.
     */
    public int getThreadPoolMinThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("threadPool.minThreads", "8"));
    }

    /**
     * Thread pool stop timeout in milliseconds. Defaults to 5000.
     *
     * <p>The maximum time allowed for the service to shut down.
     *
     * @return Thread pool stop timeout.
     */
    public long getThreadPoolStopTimeout() {
        return Long.parseLong(
                getHelpedParameters().getFirstValue("threadPool.stopTimeout", "5000"));
    }

    /**
     * Thread pool threads priority. Defaults to {@link Thread#NORM_PRIORITY}.
     *
     * @return Thread pool maximum threads.
     */
    public int getThreadPoolThreadsPriority() {
        return Integer.parseInt(
                getHelpedParameters()
                        .getFirstValue(
                                "threadPool.threadsPriority",
                                String.valueOf(Thread.NORM_PRIORITY)));
    }

    /**
     * Returns the wrapped Jetty server.
     *
     * @return The wrapped Jetty server.
     */
    protected org.eclipse.jetty.server.Server getWrappedServer() {
        if (this.wrappedServer == null) {
            this.wrappedServer = createServer();
        }
        return this.wrappedServer;
    }

    /**
     * Sets the wrapped Jetty server.
     *
     * @param wrappedServer The wrapped Jetty server.
     */
    protected void setWrappedServer(org.eclipse.jetty.server.Server wrappedServer) {
        Objects.requireNonNull(wrappedServer);
        this.wrappedServer = wrappedServer;
    }

    @Override
    public void start() throws Exception {
        super.start();
        org.eclipse.jetty.server.Server server = getWrappedServer();
        AbstractNetworkConnector connector = (AbstractNetworkConnector) server.getConnectors()[0];

        getLogger()
                .info(
                        "Starting the Jetty "
                                + getProtocols()
                                + " server on port "
                                + getHelped().getPort());
        try {
            server.start();
            // We won't know the local port until after the server starts
            setEphemeralPort(connector.getLocalPort());
        } catch (Exception e) {
            // Make sure that all resources are released, otherwise thread-pool
            // may still be running.
            server.stop();
            throw e;
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger()
                .info(
                        "Stopping the Jetty "
                                + getProtocols()
                                + " server on port "
                                + getHelped().getPort());
        if (this.wrappedServer != null) {
            getWrappedServer().stop();
        }
        super.stop();
    }
}
