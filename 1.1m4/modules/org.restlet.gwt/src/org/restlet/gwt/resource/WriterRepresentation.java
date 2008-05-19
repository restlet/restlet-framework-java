/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.util.ByteUtils;

/**
 * Representation based on a BIO characters writer. This class is a good basis
 * to write your own representations, especially for the dynamic and large ones.
 * For this you just need to create a subclass and override the abstract
 * Representation.write(Writer) method. This method will later be called back by
 * the connectors when the actual representation's content is needed.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class WriterRepresentation extends CharacterRepresentation {

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's mediaType.
     */
    public WriterRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's mediaType.
     * @param expectedSize
     *                The expected writer size in bytes.
     */
    public WriterRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
    }

    @Override
    public Reader getReader() throws IOException {
        return ByteUtils.getReader(this);
    }

    /**
     * Calls parent's implementation.
     */
    @Override
    public void release() {
        super.release();
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        write(new OutputStreamWriter(outputStream, getCharacterSet().getName()));
    }

}
