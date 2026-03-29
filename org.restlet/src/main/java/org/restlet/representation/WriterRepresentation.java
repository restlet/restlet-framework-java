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
import java.io.Reader;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Representation based on a BIO characters writer. This class is a good basis to write your own
 * representations, especially for the dynamic and large ones. <br>
 * <br>
 * For this you need to create a subclass and override the abstract Representation.write(Writer)
 * method. This method will later be called back by the connectors when the actual representation's
 * content is needed.
 *
 * @author Jerome Louvel
 */
public abstract class WriterRepresentation extends CharacterRepresentation {

    /**
     * Constructor.
     *
     * @param mediaType The representation's mediaType.
     */
    protected WriterRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     *
     * @param mediaType The representation's mediaType.
     * @param expectedSize The expected writer size in bytes.
     */
    protected WriterRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
    }

    @Override
    public Reader getReader() throws IOException {
        return IoUtils.getReader(this);
    }
}
