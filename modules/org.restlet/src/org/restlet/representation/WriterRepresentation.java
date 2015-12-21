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
import java.io.Reader;

import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

/**
 * Representation based on a BIO characters writer. This class is a good basis
 * to write your own representations, especially for the dynamic and large ones. <br>
 * <br>
 * For this you just need to create a subclass and override the abstract
 * Representation.write(Writer) method. This method will later be called back by
 * the connectors when the actual representation's content is needed.
 * 
 * @author Jerome Louvel
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
    public Reader getReader() throws IOException {
        return IoUtils.getReader(this);
    }

}
