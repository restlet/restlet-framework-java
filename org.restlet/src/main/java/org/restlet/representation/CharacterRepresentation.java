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
import java.io.Writer;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Representation based on a BIO character stream.
 *
 * @author Jerome Louvel
 */
public abstract class CharacterRepresentation extends Representation {
    /**
     * Constructor.
     *
     * @param mediaType The media type.
     */
    protected CharacterRepresentation(MediaType mediaType) {
        super(mediaType);
        setCharacterSet(CharacterSet.UTF_8);
    }

    @Override
    public InputStream getStream() throws IOException {
        return IoUtils.getStream(getReader(), getCharacterSet());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        Writer writer = IoUtils.getWriter(outputStream, getCharacterSet());
        write(writer);
        writer.flush();
    }
}
