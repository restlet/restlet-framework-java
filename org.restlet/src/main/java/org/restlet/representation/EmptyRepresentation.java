/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Empty representation with no content. It is always considered available but calling the {@link
 * #getText()} method for example will return an empty string. It can also have regular metadata
 * available.
 *
 * @author Jerome Louvel
 */
public class EmptyRepresentation extends Representation {

    /** Constructor. */
    public EmptyRepresentation() {
        setAvailable(false);
        setTransient(true);
        setSize(0);
    }

    @Override
    public Reader getReader() throws IOException {
        return null;
    }

    @Override
    public InputStream getStream() throws IOException {
        return null;
    }

    @Override
    public String getText() throws IOException {
        return null;
    }

    @Override
    public void write(java.io.Writer writer) throws IOException {
        // Do nothing
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        // Do nothing
    }
}
