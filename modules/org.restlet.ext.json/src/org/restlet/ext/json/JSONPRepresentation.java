/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.io.NioUtils;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

/**
 * Wrappers that adds a JSONP header and footer to JSON representations. The
 * goal is to make them accessible to web browser without restriction from
 * single origin policies.
 * 
 * @author Mark Kharitonov
 */
public class JsonpRepresentation extends WrapperRepresentation {
    /** The name of the JavaScript callback method. */
    private final String callback;

    /** The actual status code. */
    private final Status status;

    /**
     * Constructor.
     * 
     * @param callback
     *            The name of the JavaScript callback method.
     * @param status
     *            The actual status code.
     * @param wrappedRepresentation
     */
    public JsonpRepresentation(String callback, Status status,
            Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.callback = callback;
        this.status = status;
    }

    /**
     * Returns the name of the JavaScript callback method.
     * 
     * @return The name of the JavaScript callback method.
     */
    public String getCallback() {
        return callback;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return NioUtils.getChannel(getStream());
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_JAVASCRIPT;
    }

    @Override
    public long getSize() {
        long result = super.getSize();

        if (result > 0
                && MediaType.APPLICATION_JSON.equals(super.getMediaType())) {
            return result + getCallback().length()
                    + "({status:,body:});".length()
                    + Integer.toString(getStatus().getCode()).length();
        }

        return UNKNOWN_SIZE;
    }

    /**
     * Returns the actual status code.
     * 
     * @return The actual status code.
     */
    public Status getStatus() {
        return status;
    }

    @Override
    public InputStream getStream() throws IOException {
        return BioUtils.getStream(this);
    }

    @Override
    public String getText() throws IOException {
        return BioUtils.toString(getStream());
    }

    @Override
    public void write(java.io.Writer writer) throws IOException {
        OutputStream os = BioUtils.getStream(writer, getCharacterSet());
        write(os);
        os.flush();
    }

    /**
     * Writes the callback method wrapper first, including the actual HTTP
     * status code, then the existing JSON content as a body.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(getCallback().getBytes());
        outputStream.write("({status:".getBytes());
        outputStream.write(Integer.toString(getStatus().getCode()).getBytes());
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
        OutputStream os = NioUtils.getStream(writableChannel);
        write(os);
        os.flush();
    }
}