package com.noelios.restlet.ext.grizzly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.Server;
import org.restlet.resource.Representation;

import com.noelios.restlet.http.StreamServerCall;

public class GrizzlyServerCall extends StreamServerCall {
    /**
     * Constructor.
     * 
     * @param server
     *            The server connector.
     * @param requestStream
     *            The request input stream.
     */
    public GrizzlyServerCall(Server server, InputStream requestStream,
            OutputStream responseStream) {
        super(server, requestStream, new ByteArrayOutputStream(4096));
    }

    public ByteArrayOutputStream getResponseStream() {
        return (ByteArrayOutputStream) super.getResponseStream();
    }

    @Override
    public void writeResponseBody(Representation entity) throws IOException {
        super.writeResponseBody(entity);
    }
    

}
