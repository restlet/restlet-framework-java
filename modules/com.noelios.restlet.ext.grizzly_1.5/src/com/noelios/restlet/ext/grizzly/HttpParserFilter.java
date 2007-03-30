package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.noelios.restlet.http.HttpServerHelper;
import com.noelios.restlet.http.StreamServerCall;
import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.util.ByteBufferInputStream;
import com.sun.grizzly.util.WorkerThread;

public class HttpParserFilter implements ProtocolFilter {

    private HttpServerHelper helper;

    public HttpParserFilter(HttpServerHelper helper) {
        this.helper = helper;
    }

    public boolean execute(Context context) throws IOException {
        ByteBuffer byteBuffer = ((WorkerThread) Thread.currentThread())
                .getByteBuffer();
        ByteBufferInputStream is = new ByteBufferInputStream();
        SelectionKey key = context.getSelectionKey();
        is.setSelectionKey(key);
        is.setByteBuffer(byteBuffer);
        //SocketChannel socketChannel = (SocketChannel) key.channel();

        // Create the HTTP call
        StreamServerCall serverCall = new StreamServerCall(null, is, null);
        boolean keepAlive = false;

        // Handle the call
        this.helper.handle(serverCall);

        // Prepare for additional calls?
        if (keepAlive) {
            context
                    .setKeyRegistrationState(Context.KeyRegistrationState.REGISTER);
        } else {
            context
                    .setKeyRegistrationState(Context.KeyRegistrationState.CANCEL);
        }

        // Clean up
        byteBuffer.clear();

        // This is the last filter
        return true;
    }

    public boolean postExecute(Context context) throws IOException {
        return false;
    }

}
