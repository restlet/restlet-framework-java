package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.net.JxtaMulticastServer;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class DefaultAsynchronousConnection extends AbstractConnection implements AsynchronousConnection {

    private JxtaMulticastServer server;
    private ConnectionListener listener;

    public DefaultAsynchronousConnection(URI id, String name, NetworkHandler networkHandler, ConnectionListener listener) {
        super(id, name, networkHandler);

        this.listener = listener;
    }

    public void send(byte[] data) throws IOException {
        sendTo(data, null);
    }

    public void sendTo(byte[] data, InetAddress to) throws IOException {
        if (server == null) {
            throw new IllegalStateException("connection is not established");
        }

        server.sendTo(data, to);
    }

    public void start() throws NetworkException {
        if (server !=  null) {
            return;
        }

        // todo: subgroups
        PeerGroup group = getNetworkHandler().getNetwork().getNetPeerGroup();

        // todo: socket factory based on id
        server = new JxtaMulticastServer(PipeID.create(getId()), getName(), group, listener);

        server.start();
    }

    public void stop() throws NetworkException {
        if (server == null) {
            return;
        }

        server.stop();
    }

    protected void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }
}