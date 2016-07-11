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

package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Empty representation with no content. It is always considered available but
 * calling the {@link #getText()} method for example will return an empty
 * string. It can also have regular metadata available.
 * 
 * @author Jerome Louvel
 */
public class EmptyRepresentation extends Representation {

    /**
     * Constructor.
     */
    public EmptyRepresentation() {
        setAvailable(false);
        setTransient(true);
        setSize(0);
    }

    // [ifndef gwt] method
    @Override
    public java.nio.channels.ReadableByteChannel getChannel()
            throws IOException {
        return null;
    }

    @Override
    public Reader getReader() throws IOException {
        return null;
    }

    @Override
    public InputStream getStream() throws IOException {
        return null;
    }

    // [ifndef gwt] method
    @Override
    public String getText() throws IOException {
        return null;
    }

    // [ifdef gwt] method uncomment
    // @Override
    // public String getText() throws IOException {
    // return "";
    // }

    // [ifndef gwt] method
    @Override
    public void write(java.io.Writer writer) throws IOException {
        // Do nothing
    }

    // [ifndef gwt] method
    @Override
    public void write(java.nio.channels.WritableByteChannel writableChannel)
            throws IOException {
        // Do nothing
    }

    // [ifndef gwt] method
    @Override
    public void write(OutputStream outputStream) throws IOException {
        // Do nothing
    }
}
