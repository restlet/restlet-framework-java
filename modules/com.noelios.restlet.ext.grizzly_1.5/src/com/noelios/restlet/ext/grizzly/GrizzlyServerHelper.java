/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.grizzly;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.http.HttpServerHelper;
import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultInstanceHandler;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.filter.ReadFilter;

/**
 * @author Jerome Louvel (contact@noelios.com)
 */
public class GrizzlyServerHelper extends HttpServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public GrizzlyServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public void start() throws Exception {
        super.start();

        // Create the server socket address
        int port = getServer().getPort();
        String address = getServer().getAddress();
        InetSocketAddress socketAddress;
        if (address == null) {
            socketAddress = new InetSocketAddress(port);
        } else {
            socketAddress = new InetSocketAddress(address, port);
        }

        // Create and bind the server socket
        int socketBackLog = 50;
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        ServerSocket socket = socketChannel.socket();
        socket.setReuseAddress(true);
        socket.bind(socketAddress, socketBackLog);
        socketChannel.configureBlocking(false);

        // Create the Grizzly controller
        Controller controller = new Controller();
        controller.setInstanceHandler(new DefaultInstanceHandler() {
            public ProtocolChain poll() {
                ProtocolChain protocolChain = protocolChains.poll();
                if (protocolChain == null) {
                    protocolChain = new DefaultProtocolChain();
                    protocolChain.addFilter(new ReadFilter());
                    protocolChain.addFilter(new HttpParserFilter(GrizzlyServerHelper.this));
                }
                return protocolChain;
            }
        });

    }

}
