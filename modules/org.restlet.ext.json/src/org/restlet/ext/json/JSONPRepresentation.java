package com.adeo.wikeo.util;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.io.NioUtils;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * COPY FROM https://bitbucket.org/markkharitonov/restlet-jsonp-filter/wiki/Home
 */
public class JSONPRepresentation extends WrapperRepresentation {
    private final String callback;
    private final Status status;

    public JSONPRepresentation(String callback, Status status, Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.callback = callback;
        this.status = status;
    }

    @Override
    public long getSize() {
        long result = super.getSize();
        if (result > 0 && MediaType.APPLICATION_JSON.equals(super.getMediaType())) {
            return result + callback.length() + "({status:,body:});".length() + Integer.toString(status.getCode()).length();
        }
        return UNKNOWN_SIZE;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return NioUtils.getChannel(getStream());
    }

    @Override
    public InputStream getStream() throws IOException {
        return BioUtils.getInputStream(this);
    }

    @Override
    public String getText() throws IOException {
        return BioUtils.toString(getStream());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(callback.getBytes());
        outputStream.write("({status:".getBytes());
        outputStream.write(Integer.toString(status.getCode()).getBytes());
        outputStream.write(",body:".getBytes());
        if (MediaType.APPLICATION_JSON.equals(super.getMediaType())) {
            BioUtils.copy(super.getStream(), outputStream);
        } else {
            outputStream.write("'".getBytes());
            String text = super.getText();
            if (text.indexOf('\'') >= 0) {
                text = text.replace("\'", "\\\'");
            }
            outputStream.write(text.getBytes());
            outputStream.write("'".getBytes());
        }
        outputStream.write("});".getBytes());
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        write(NioUtils.getStream(writableChannel));
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_JAVASCRIPT;
    }
}