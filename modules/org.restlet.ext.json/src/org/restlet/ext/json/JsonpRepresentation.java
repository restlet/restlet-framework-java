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

package org.restlet.ext.json;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

/**
 * Wrappers that adds a JSONP header and footer to JSON representations. The
 * goal is to make them accessible to web browser without restriction from
 * single origin policies.
 * 
 * @author Mark Kharitonov
 * @author Jerome Louvel
 */
public class JsonpRepresentation extends WriterRepresentation {
    /** The name of the JavaScript callback method. */
    private final String callback;

    /** The actual status code. */
    private final Status status;

    /** The wrapped JSON representation. */
    private final Representation wrappedRepresentation;

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
        super(MediaType.APPLICATION_JAVASCRIPT);
        this.callback = callback;
        this.status = status;
        this.wrappedRepresentation = wrappedRepresentation;
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
    public long getSize() {
        long result = wrappedRepresentation.getSize();

        if (result > 0
                && MediaType.APPLICATION_JSON.equals(wrappedRepresentation
                        .getMediaType())) {
            try {
                java.io.StringWriter sw = new java.io.StringWriter();
                write(sw);
                sw.flush();
                return sw.toString().length();
            } catch (IOException e) {
                return UNKNOWN_SIZE;
            }
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
    public void write(java.io.Writer writer) throws IOException {
        writer.write(getCallback());
        writer.write("({\"status\":");
        writer.write(Integer.toString(getStatus().getCode()));
        writer.write(",\"body\":");

        if (MediaType.APPLICATION_JSON.equals(wrappedRepresentation
                .getMediaType())) {
            IoUtils.copy(wrappedRepresentation.getReader(), writer);
        } else {
            writer.write("\"");
            String text = wrappedRepresentation.getText();

            if (text.indexOf('\"') >= 0) {
                text = text.replace("\"", "\\\"");
            }

            writer.write(text);
            writer.write("\"");
        }

        writer.write("});");
    }

}
