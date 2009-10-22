/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.engine.http.HttpServerHelper;
import org.restlet.ext.netty.internal.NettyParams;
import org.restlet.util.Series;

/**
 * Abstract Netty Web server connector. Parameters, listed below, are used to
 * configure both a parent channel and its child channels. To configure the
 * child channels, prepend "child." prefix to the actual parameter names of a
 * child channel. They should be set in the Server's context before it is
 * started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>bufferFactoryClass</td>
 * <td>String</td>
 * <td>org.jboss.netty.buffer.HeapChannelBufferFactory</td>
 * <td>Channel buffer allocation strategy.</td>
 * </tr>
 * <tr>
 * <td>connectTimeoutMillis</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Connect timeout of the channel in milliseconds. Sets to 0 to disable it.</td>
 * </tr>
 * <tr>
 * <td>keepAlive</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Turn on/off socket keep alive.</td>
 * </tr>
 * <tr>
 * <td>reuseAddress</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Enable/Disable reuse address for socket.</td>
 * </tr>
 * <tr>
 * <td>receiveBufferSize</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Provide the size of the buffer actually used by the platform when
 * receiving in data on this socket.</td>
 * </tr>
 * <tr>
 * <td>sendBufferSize</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Set a hint the size of the underlying buffers for outgoing network I/O.</td>
 * </tr>
 * <tr>
 * <td>trafficClass</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets traffic class or type-of-service octet in the IP header for packets
 * sent from this Socket. As the underlying network implementation may ignore
 * this value applications should consider it a hint.See
 * 
 * @see http://java.sun.com/javase/6/docs/api/java/net/Socket.html?is-
 *      external=true#setTrafficClass(int).</td>
 *      </tr>
 *      </table>
 * 
 * @see <a href="http://jboss.org/netty/">Netty home page</a>
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
public abstract class NettyServerHelper extends HttpServerHelper {

    private static final String CHILD_CHANNEL_PREFIX = "child.";

    private static final String RESTLET_NETTY_SERVER = "restlet-netty-server";

    private ChannelGroup allChannels = new DefaultChannelGroup(
            RESTLET_NETTY_SERVER);

    private ChannelFactory factory = new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

    /**
     * Constructor.
     * 
     * @param server
     *            The server that will be helped.
     */
    public NettyServerHelper(Server server) {
        super(server);
    }

    /**
     * Returns the Netty pipeline factory.
     * 
     * @return The Netty pipeline factory.
     */
    protected abstract ChannelPipelineFactory getPipelineFatory();

    @Override
    public synchronized void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(getPipelineFatory());

        // Copy the parameters as channel options
        setServerParameters(bootstrap);

        int port = getHelped().getPort();
        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        InetSocketAddress address = (InetSocketAddress) channel
                .getLocalAddress();
        setEphemeralPort(address.getPort());
        allChannels.add(channel);
        getLogger().log(Level.INFO,
                "Started Netty " + getProtocols() + " server");
    }

    /**
     * <p>
     * Pass netty channel parameters through bootstrap.
     * </p>
     * 
     * @param bootstrap
     *            - server bootstrap instance
     */
    private void setServerParameters(final ServerBootstrap bootstrap) {
        Series<Parameter> options = getHelpedParameters();

        for (Parameter option : options) {
            String paramName = option.getName();
            if (paramName.startsWith(CHILD_CHANNEL_PREFIX)) {
                paramName = option.getName().substring(
                        CHILD_CHANNEL_PREFIX.length());
            }
            NettyParams param = NettyParams.valueOf(paramName);
            if (param != null) {
                final Object value = param.getValue(option.getValue());
                if (value != null) {
                    bootstrap.setOption(option.getName(), value);
                }
            }

        }
    }

    @Override
    public synchronized void stop() throws Exception {
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        factory.releaseExternalResources();
        getLogger().log(Level.INFO,
                "Stopped Netty " + getProtocols() + " server");
        super.stop();
    }

}
