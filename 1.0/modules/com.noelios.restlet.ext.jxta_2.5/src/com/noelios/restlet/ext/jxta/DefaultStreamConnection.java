package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.net.JxtaSocketServer;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.NetworkException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class DefaultStreamConnection extends AbstractConnection implements StreamConnection {

    private JxtaSocketServer server;
    private ConnectionListener listener;

    public DefaultStreamConnection(URI id, String name, NetworkHandler networkHandler) {
        super(id, name, networkHandler);
    }

    // todo: impelement
    public InputStream getInputStream() {
        return null;
    }

    // todo: implement
    public OutputStream getOutputStream() {
        return null;
    }

    // todo: implement
    public void receive(Socket socket) {
    }

    public void start() throws NetworkException {
        if (server != null) {
            return;
        }

        // todo: subgroups
        PeerGroup group = getNetworkHandler().getNetwork().getNetPeerGroup();

        // todo: socket factory based on id
        server = new JxtaSocketServer(getName(), group, PipeID.create(getId()));

        server.start();
    }

    public void stop() throws NetworkException {
        if (server == null) {
            return;
        }

        server.stop();
    }
}