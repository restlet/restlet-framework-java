/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executor;

import javax.servlet.ServletException;

import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.restlet.Server;
import org.restlet.ext.jetty.internal.JettyServerCall;

/**
 * Abstract Jetty web server connector. Here is the list of parameters that are
 * supported. They should be set in the Server's context before it is started:
 * <table>
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
 * <td>Connector acceptor thread count; when -1, Jetty will default to
 * {@link Runtime#availableProcessors()} / 2, with a minimum of 1</td>
 * </tr>
 * <tr>
 * <td>connector.selectors</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Connector selector thread count; when -1, Jetty will default to
 * {@link Runtime#availableProcessors()}</td>
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
 * <td>connector.soLingerTime</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Connector TCP/IP SO linger time in milliseconds; when -1 is disabled; see
 * {@link Socket#setSoLinger(boolean, int)}</td>
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
 * and/or larger cookies and longer HTTP headers (e.g. for redirection);
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
 * <td>Low resource monitor period in milliseconds; when 0, low resource
 * monitoring is disabled</td>
 * </tr>
 * <tr>
 * <td>lowResource.threads</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Low resource monitor, whether to check if we're low on threads</td>
 * </tr>
 * <tr>
 * <td>lowResource.maxMemory</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Low resource monitor max memory in bytes; when 0, the check disabled;
 * memory used is calculated as (totalMemory-freeMemory)</td>
 * </tr>
 * <tr>
 * <td>lowResource.maxConnections</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Low resource monitor max connections; when 0, the check is disabled</td>
 * </tr>
 * <tr>
 * <td>lowResource.idleTimeout</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>Low resource monitor idle timeout in milliseconds; applied to EndPoints
 * when in the low resources state</td>
 * </tr>
 * <tr>
 * <td>lowResource.stopTimeout</td>
 * <td>long</td>
 * <td>30000</td>
 * <td>Low resource monitor stop timeout in milliseconds; the maximum time
 * allowed for the service to shutdown</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://www.eclipse.org/jetty/">Jetty home page</a>
 * @author Jerome Louvel
 * @author Tal Liron
 */
public abstract class JettyServerHelper extends
        org.restlet.engine.adapter.HttpServerHelper {

    /**
     * Jetty server wrapped by a parent Restlet HTTP server connector.
     * 
     * @author Jerome Louvel
     * @author Tal Liron
     */
    private static class WrappedServer extends org.eclipse.jetty.server.Server {
        private final JettyServerHelper helper;

        /**
         * Constructor.
         * 
         * @param server
         *            The Jetty HTTP server.
         * @param threadPool
         *            The thread pool.
         */
        public WrappedServer(JettyServerHelper server, ThreadPool threadPool) {
            super(threadPool);
            this.helper = server;
        }

        /**
         * Handler method converting a Jetty HttpChannel into a Restlet Call.
         * 
         * @param channel
         *            The channel to handle.
         */
        @Override
        public void handle(HttpChannel<?> channel) throws IOException,
                ServletException {
            this.helper.handle(new JettyServerCall(this.helper.getHelped(),
                    channel));
        }
    }

    /** The wrapped Jetty server. */
    private volatile org.eclipse.jetty.server.Server wrappedServer;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public JettyServerHelper(Server server) {
        super(server);
    }

    /**
     * Creates a Jetty HTTP configuration.
     * 
     * @return A Jetty HTTP configuration.
     */
    private HttpConfiguration createConfiguration() {
        final HttpConfiguration configuration = new HttpConfiguration();
        configuration.setHeaderCacheSize(getHttpHeaderCacheSize());
        configuration.setRequestHeaderSize(getHttpRequestHeaderSize());
        configuration.setResponseHeaderSize(getHttpResponseHeaderSize());
        configuration.setOutputBufferSize(getHttpOutputBufferSize());
        return configuration;
    }

    /**
     * Creates a new internal Jetty connection factory.
     * 
     * @param configuration
     *            The HTTP configuration.
     * @return A new internal Jetty connection factory.
     */
    protected abstract ConnectionFactory createConnectionFactory(
            HttpConfiguration configuration);

    /**
     * Creates a Jetty connector.
     * 
     * @param server
     *            The Jetty server.
     * @return A Jetty connector.
     */
    private Connector createConnector(org.eclipse.jetty.server.Server server) {
        final HttpConfiguration configuration = createConfiguration();
        final ConnectionFactory connectionFactory = createConnectionFactory(configuration);

        final int acceptors = getConnectorAcceptors();
        final int selectors = getConnectorSelectors();
        final Executor executor = getConnectorExecutor();
        final Scheduler scheduler = getConnectorScheduler();
        final ByteBufferPool byteBufferPool = getConnectorByteBufferPool();

        final ServerConnector connector = new ServerConnector(server, executor,
                scheduler, byteBufferPool, acceptors, selectors,
                connectionFactory);

        final String address = getHelped().getAddress();
        if (address != null)
            connector.setHost(address);
        connector.setPort(getHelped().getPort());

        connector.setAcceptQueueSize(getConnectorAcceptQueueSize());
        connector.setIdleTimeout(getConnectorIdleTimeout());
        connector.setSoLingerTime(getConnectorSoLingerTime());
        connector.setStopTimeout(getConnectorStopTimeout());

        return connector;
    }

    /**
     * Creates a Jetty low resource monitor.
     * 
     * @param server
     *            A Jetty server.
     * @return A Jetty low resource monitor or null.
     */
    private LowResourceMonitor createLowResourceMonitor(
            org.eclipse.jetty.server.Server server) {
        final int period = getLowResourceMonitorPeriod();
        if (period > 0) {
            final LowResourceMonitor lowResourceMonitor = new LowResourceMonitor(
                    server);
            lowResourceMonitor.setMonitoredConnectors(Arrays.asList(server
                    .getConnectors()));
            lowResourceMonitor.setPeriod(period);
            lowResourceMonitor
                    .setMonitorThreads(getLowResourceMonitorThreads());
            lowResourceMonitor.setMaxMemory(getLowResourceMonitorMaxMemory());
            lowResourceMonitor
                    .setMaxConnections(getLowResourceMonitorMaxConnections());
            lowResourceMonitor
                    .setLowResourcesIdleTimeout(getLowResourceMonitorIdleTimeout());
            lowResourceMonitor
                    .setStopTimeout(getLowResourceMonitorStopTimeout());
            server.addBean(lowResourceMonitor);
            return lowResourceMonitor;
        }
        return null;
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
        final org.eclipse.jetty.server.Server server = new WrappedServer(this,
                threadPool);

        // Connector
        final Connector connector = createConnector(server);
        server.addConnector(connector);

        // Low resource monitor (must be created after connectors have been
        // added)
        createLowResourceMonitor(server);

        return server;
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
        threadPool.setStopTimeout(getThreadPoolStopTimeout());
        return threadPool;
    }

    /**
     * Connector acceptor thread count. Defaults to -1. When -1, Jetty will
     * default to {@link Runtime#availableProcessors()} / 2, with a minimum of
     * 1.
     * 
     * @return Connector acceptor thread count.
     */
    public int getConnectorAcceptors() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.acceptors", "-1"));
    }

    /**
     * Connector accept queue size. Defaults to 0.
     * <p>
     * Also known as accept backlog.
     * 
     * @return Connector accept queue size.
     */
    public int getConnectorAcceptQueueSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.acceptQueueSize", "0"));
    }

    /**
     * Connector byte buffer pool. Defaults to null. When null, will use a new
     * {@link ArrayByteBufferPool}.
     * 
     * @return Connector byte buffer pool or null.
     */
    public ByteBufferPool getConnectorByteBufferPool() {
        return null;
    }

    /**
     * Connector executor. Defaults to null. When null, will use the server's
     * thread pool.
     * 
     * @return Connector executor or null.
     */
    public Executor getConnectorExecutor() {
        return null;
    }

    /**
     * Connector idle timeout in milliseconds. Defaults to 30000.
     * <p>
     * See {@link Socket#setSoTimeout(int)}.
     * <p>
     * This value is interpreted as the maximum time between some progress being
     * made on the connection. So if a single byte is read or written, then the
     * timeout is reset.
     * 
     * @return Connector idle timeout.
     */
    public int getConnectorIdleTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.idleTimeout", "30000"));
    }

    /**
     * Connector scheduler. Defaults to null. When null, will use a new
     * {@link ScheduledExecutorScheduler}.
     * 
     * @return Connector scheduler or null.
     */
    public Scheduler getConnectorScheduler() {
        return null;
    }

    /**
     * Connector selector thread count. Defaults to -1. When 0, Jetty will
     * default to {@link Runtime#availableProcessors()}.
     * 
     * @return Connector acceptor thread count.
     */
    public int getConnectorSelectors() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.selectors", "-1"));
    }

    /**
     * Connector TCP/IP SO linger time in milliseconds. Defaults to -1
     * (disabled).
     * <p>
     * See {@link Socket#setSoLinger(boolean, int)}.
     * 
     * @return Connector TCP/IP SO linger time.
     */
    public int getConnectorSoLingerTime() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.soLingerTime", "-1"));
    }

    /**
     * Connector stop timeout in milliseconds. Defaults to 30000.
     * <p>
     * The maximum time allowed for the service to shutdown.
     * 
     * @return Connector stop timeout.
     */
    public int getConnectorStopTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connector.stopTimeout", "30000"));
    }

    /**
     * HTTP header cache size in bytes. Defaults to 512.
     * 
     * @return HTTP header cache size.
     */
    public int getHttpHeaderCacheSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "http.headerCacheSize", "512"));
    }

    /**
     * HTTP output buffer size in bytes. Defaults to 32*1024.
     * <p>
     * A larger buffer can improve performance by allowing a content producer to
     * run without blocking, however larger buffers consume more memory and may
     * induce some latency before a client starts processing the content.
     * 
     * @return HTTP output buffer size.
     */
    public int getHttpOutputBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "http.outputBufferSize", "32768"));
    }

    /**
     * HTTP request header size in bytes. Defaults to 8*1024.
     * <p>
     * Larger headers will allow for more and/or larger cookies plus larger form
     * content encoded in a URL. However, larger headers consume more memory and
     * can make a server more vulnerable to denial of service attacks.
     * 
     * @return HTTP request header size.
     */
    public int getHttpRequestHeaderSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "http.requestHeaderSize", "8192"));
    }

    /**
     * HTTP response header size in bytes. Defaults to 8*1024.
     * <p>
     * Larger headers will allow for more and/or larger cookies and longer HTTP
     * headers (e.g. for redirection). However, larger headers will also consume
     * more memory.
     * 
     * @return HTTP response header size.
     */
    public int getHttpResponseHeaderSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "http.responseHeaderSize", "8192"));
    }

    /**
     * Low resource monitor idle timeout in milliseconds. Defaults to 1000.
     * <p>
     * Applied to EndPoints when in the low resources state.
     * 
     * @return Low resource monitor idle timeout.
     */
    public int getLowResourceMonitorIdleTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowResource.idleTimeout", "1000"));
    }

    /**
     * Low resource monitor max connections. Defaults to 0. When 0, the check is
     * disabled.
     * 
     * @return Low resource monitor max connections.
     */
    public int getLowResourceMonitorMaxConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowResource.maxConnections", "0"));
    }

    /**
     * Low resource monitor max memory in bytes. Defaults to 0. When 0, the
     * check disabled.
     * <p>
     * Memory used is calculated as (totalMemory-freeMemory).
     * 
     * @return Low resource monitor max memory.
     */
    public long getLowResourceMonitorMaxMemory() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "lowResource.maxMemory", "0"));
    }

    /**
     * Low resource monitor period in milliseconds. Defaults to 1000. When 0,
     * low resource monitoring is disabled.
     * 
     * @return Low resource monitor period.
     */
    public int getLowResourceMonitorPeriod() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowResource.period", "1000"));
    }

    /**
     * Low resource monitor stop timeout in milliseconds. Defaults to 30000.
     * <p>
     * The maximum time allowed for the service to shutdown.
     * 
     * @return Low resource monitor stop timeout.
     */
    public long getLowResourceMonitorStopTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "lowResource.stopTimeout", "30000"));
    }

    /**
     * Low resource monitor, whether to check if we're low on threads. Defaults
     * to true.
     * 
     * @return Low resource monitor threads.
     */
    public boolean getLowResourceMonitorThreads() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "lowResource.threads", "true"));
    }

    /**
     * Thread pool idle timeout in milliseconds. Defaults to 60000.
     * <p>
     * Threads that are idle for longer than this period may be stopped.
     * 
     * @return Thread pool idle timeout.
     */
    public int getThreadPoolIdleTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadPool.idleTimeout", "60000"));
    }

    /**
     * Thread pool maximum threads. Defaults to 200.
     * 
     * @return Thread pool maximum threads.
     */
    public int getThreadPoolMaxThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadPool.maxThreads", "200"));
    }

    /**
     * Thread pool minimum threads. Defaults to 8.
     * 
     * @return Thread pool minimum threads.
     */
    public int getThreadPoolMinThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadPool.minThreads", "8"));
    }

    /**
     * Thread pool stop timeout in milliseconds. Defaults to 5000.
     * <p>
     * The maximum time allowed for the service to shutdown.
     * 
     * @return Thread pool stop timeout.
     */
    public long getThreadPoolStopTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "threadPool.stopTimeout", "5000"));
    }

    /**
     * Thread pool threads priority. Defaults to {@link Thread#NORM_PRIORITY}.
     * 
     * @return Thread pool maximum threads.
     */
    public int getThreadPoolThreadsPriority() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadPool.threadsPriority",
                String.valueOf(Thread.NORM_PRIORITY)));
    }

    /**
     * Returns the wrapped Jetty server.
     * 
     * @return The wrapped Jetty server.
     */
    protected org.eclipse.jetty.server.Server getWrappedServer() {
        if (this.wrappedServer == null)
            this.wrappedServer = createServer();
        return this.wrappedServer;
    }

    /**
     * Sets the wrapped Jetty server.
     * 
     * @param wrappedServer
     *            The wrapped Jetty server.
     */
    protected void setWrappedServer(
            org.eclipse.jetty.server.Server wrappedServer) {
        this.wrappedServer = wrappedServer;
    }

    @Override
    public void start() throws Exception {
        super.start();
        org.eclipse.jetty.server.Server server = getWrappedServer();
        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        getLogger().info(
                "Starting the Jetty " + getProtocols() + " server on port "
                        + getHelped().getPort());
        server.start();

        // We won't know the local port until after the server starts
        setEphemeralPort(connector.getLocalPort());
    }

    @Override
    public void stop() throws Exception {
        getLogger().info("Stopping a Jetty HTTP/HTTPS server");

        getWrappedServer().stop();

        super.stop();
    }
}
