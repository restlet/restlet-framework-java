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
import java.io.OutputStream;
import java.io.Reader;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Representation based on a BIO stream.
 *
 * @author Jerome Louvel
 */
public abstract class StreamRepresentation extends Representation {

    /**
     * Constructor.
     *
     * @param mediaType The media type.
     */
    protected StreamRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public Reader getReader() throws IOException {
        return IoUtils.getReader(getStream(), getCharacterSet());
    }

    @Override
    public void write(java.io.Writer writer) throws IOException {
        OutputStream os = IoUtils.getStream(writer, getCharacterSet());
        write(os);
        os.flush();
    }
}
