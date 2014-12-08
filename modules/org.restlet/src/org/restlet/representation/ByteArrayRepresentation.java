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

import java.io.ByteArrayInputStream;

import org.restlet.data.MediaType;

/**
 * Representation wrapping a byte array.
 * 
 * @author Jerome Louvel
 */
public class ByteArrayRepresentation extends InputRepresentation {

    /**
     * Constructor.
     * 
     * @param byteArray
     *            The byte array to wrap.
     */
    public ByteArrayRepresentation(byte[] byteArray) {
        super(new ByteArrayInputStream(byteArray));
    }

    /**
     * 
     * @param byteArray
     *            The byte array to wrap.
     * @param offSet
     *            The offset inside the byte array.
     * @param length
     *            The length to expose inside the byte array.
     */
    public ByteArrayRepresentation(byte[] byteArray, int offSet, int length) {
        super(new ByteArrayInputStream(byteArray, offSet, length));
    }

    /**
     * 
     * @param byteArray
     *            The byte array to wrap.
     * @param offSet
     *            The offset inside the byte array.
     * @param length
     *            The length to expose inside the byte array.
     * @param mediaType
     */
    public ByteArrayRepresentation(byte[] byteArray, int offSet, int length,
            MediaType mediaType) {
        super(new ByteArrayInputStream(byteArray, offSet, length), mediaType);
    }

    /**
     * 
     * @param byteArray
     *            The byte array to wrap.
     * @param offSet
     *            The offset inside the byte array.
     * @param length
     *            The length to expose inside the byte array.
     * @param mediaType
     *            The media type.
     * @param expectedSize
     */
    public ByteArrayRepresentation(byte[] byteArray, int offSet, int length,
            MediaType mediaType, long expectedSize) {
        super(new ByteArrayInputStream(byteArray, offSet, length), mediaType,
                expectedSize);
    }

    /**
     * Constructor.
     * 
     * @param byteArray
     *            The byte array to wrap.
     * @param mediaType
     *            The media type.
     */
    public ByteArrayRepresentation(byte[] byteArray, MediaType mediaType) {
        super(new ByteArrayInputStream(byteArray), mediaType);
    }

    /**
     * 
     * @param byteArray
     *            The byte array to wrap.
     * @param mediaType
     *            The media type.
     * @param expectedSize
     */
    public ByteArrayRepresentation(byte[] byteArray, MediaType mediaType,
            long expectedSize) {
        super(new ByteArrayInputStream(byteArray), mediaType, expectedSize);
    }

}
