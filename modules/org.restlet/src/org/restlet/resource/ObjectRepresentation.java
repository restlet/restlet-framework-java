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

package org.restlet.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.restlet.data.MediaType;

/**
 * Representation based on a serializable Java object.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @param <T>
 *            The class to serialize, see {@link Serializable}
 */
public class ObjectRepresentation<T extends Serializable> extends
        OutputRepresentation {
    /** The serializable object. */
    private volatile T object;

    /**
     * Constructor reading the object from a serialized representation. This
     * representation must have the proper media type:
     * "application/x-java-serialized-object".
     * 
     * @param serializedRepresentation
     *            The serialized representation.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public ObjectRepresentation(Representation serializedRepresentation)
            throws IOException, ClassNotFoundException,
            IllegalArgumentException {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        if (serializedRepresentation.getMediaType().equals(
                MediaType.APPLICATION_JAVA_OBJECT)) {
            final ObjectInputStream ois = new ObjectInputStream(
                    serializedRepresentation.getStream());
            this.object = (T) ois.readObject();
            ois.close();
        } else {
            throw new IllegalArgumentException(
                    "The serialized representation must have this media type: "
                            + MediaType.APPLICATION_JAVA_OBJECT.toString());
        }
    }

    /**
     * Constructor
     * 
     * @param object
     *            The serializable object.
     */
    public ObjectRepresentation(T object) {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        this.object = object;
    }

    /**
     * Returns the represented object.
     * 
     * @return The represented object.
     * @throws IOException
     */
    public T getObject() throws IOException {
        return this.object;
    }

    /**
     * Releases the represented object.
     */
    @Override
    public void release() {
        setObject(null);
        super.release();
    }

    /**
     * Sets the represented object.
     * 
     * @param object
     *            The represented object.
     */
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        final ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(getObject());
        oos.close();
    }

}
