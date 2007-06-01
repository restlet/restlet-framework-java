/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.MediaType;

/**
 * Representation based on a BIO characters writer. The write(Writer) method
 * needs to be overriden in subclasses.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class WriterRepresentation extends CharacterRepresentation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     */
    public WriterRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     * @param expectedSize
     *            The expected writer size in bytes.
     */
    public WriterRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType);
        setSize(expectedSize);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        write(new OutputStreamWriter(outputStream, getCharacterSet().getName()));
    }

}
