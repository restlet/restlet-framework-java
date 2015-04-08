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

package org.restlet.ext.netty;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.restlet.Server;
import org.restlet.ext.netty.internal.HttpServerInitializer;

/**
 * 
 * @author Jerome Louvel
 */
public abstract class NettyServerHelper extends
        org.restlet.engine.adapter.HttpServerHelper {

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public NettyServerHelper(Server server) {
        super(server);
    }

    private ServerBootstrap serverBootstrap;

    private Channel channel;

    protected Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected void setServerBootstrap(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }

    protected ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    private NioEventLoopGroup bossGroup;

    protected NioEventLoopGroup getBossGroup() {
        return bossGroup;
    }

    protected void setBossGroup(NioEventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    protected NioEventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    protected void setWorkerGroup(NioEventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    private NioEventLoopGroup workerGroup;

    @Override
    public void start() throws Exception {
        super.start();
        setBossGroup(new NioEventLoopGroup(1));
        setWorkerGroup(new NioEventLoopGroup());
        setServerBootstrap(new ServerBootstrap());
        getServerBootstrap().option(ChannelOption.SO_BACKLOG, 1024);
        getServerBootstrap().group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpServerInitializer(this, null));
        setChannel(serverBootstrap.bind(getHelped().getPort()).sync().channel());
        setEphemeralPort(((InetSocketAddress) getChannel().localAddress())
                .getPort());
        getLogger().info(
                "Starting the Netty " + getProtocols() + " server on port "
                        + getHelped().getPort());
    }

    @Override
    public void stop() throws Exception {
        getLogger().info(
                "Stopping the Netty " + getProtocols() + " server on port "
                        + getHelped().getPort());
        getChannel().close().sync();
        getBossGroup().shutdownGracefully();
        getWorkerGroup().shutdownGracefully();
        super.stop();
    }

}
