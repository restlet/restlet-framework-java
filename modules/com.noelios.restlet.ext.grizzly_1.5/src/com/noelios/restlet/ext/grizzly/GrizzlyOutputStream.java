package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.sun.grizzly.util.OutputWriter;

/**
 * Output stream connected to a socket channel.
 */
public class GrizzlyOutputStream extends OutputStream {
    private SocketChannel socketChannel;

    public GrizzlyOutputStream(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void write(int b) throws IOException {
        OutputWriter.flushChannel(this.socketChannel, ByteBuffer
                .wrap(new byte[] { (byte) b }));
    }
}
