package org.restlet.engine.http.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.Server;
import org.restlet.engine.ConnectorHelper;

public abstract class ServerConnection extends Connection<Server> {

    public ServerConnection(ConnectorHelper<Server> helper, Socket socket)
            throws IOException {
        super(helper, socket);
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract ReadableByteChannel getRequestEntityChannel(long size);

    /**
     * Returns the request entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract InputStream getRequestEntityStream(long size);

    /**
     * Returns the request head channel if it exists.
     * 
     * @return The request head channel if it exists.
     */
    public abstract ReadableByteChannel getRequestHeadChannel();

    /**
     * Returns the request head stream if it exists.
     * 
     * @return The request head stream if it exists.
     */
    public abstract InputStream getRequestHeadStream();

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public abstract WritableByteChannel getResponseEntityChannel();

    /**
     * Returns the response entity stream if it exists.
     * 
     * @return The response entity stream if it exists.
     */
    public abstract OutputStream getResponseEntityStream();

}
