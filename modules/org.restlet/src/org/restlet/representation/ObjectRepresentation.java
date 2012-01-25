/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.restlet.data.MediaType;

/**
 * Representation based on a serializable Java object.
 * 
 * @author Jerome Louvel
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
            setMediaType(MediaType.APPLICATION_JAVA_OBJECT);
            InputStream is = serializedRepresentation.getStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            this.object = (T) ois.readObject();

            if (is.read() != -1) {
                throw new IOException(
                        "The input stream has not been fully read.");
            }

            ois.close();
            // [ifndef android]
        } else if (serializedRepresentation.getMediaType().equals(
                MediaType.APPLICATION_JAVA_OBJECT_XML)) {
            setMediaType(MediaType.APPLICATION_JAVA_OBJECT_XML);
            InputStream is = serializedRepresentation.getStream();
            java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(is);
            this.object = (T) decoder.readObject();

            if (is.read() != -1) {
                throw new IOException(
                        "The input stream has not been fully read.");
            }

            decoder.close();
            // [enddef]
        } else {
            throw new IllegalArgumentException(
                    "The serialized representation must have this media type: "
                            + MediaType.APPLICATION_JAVA_OBJECT.toString()
                            + " or this one: "
                            + MediaType.APPLICATION_JAVA_OBJECT_XML.toString());
        }
    }

    /**
     * Constructor for the {@link MediaType#APPLICATION_JAVA_OBJECT} type.
     * 
     * @param object
     *            The serializable object.
     */
    public ObjectRepresentation(T object) {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        this.object = object;
    }

    /**
     * Constructor for either the {@link MediaType#APPLICATION_JAVA_OBJECT} type
     * or the {@link MediaType#APPLICATION_XML} type. In the first case, the
     * Java Object Serialization mechanism is used, based on
     * {@link ObjectOutputStream}. In the latter case, the JavaBeans XML
     * serialization is used, based on {@link java.beans.XMLEncoder}.
     * 
     * @param object
     *            The serializable object.
     * @param mediaType
     *            The media type.
     */
    public ObjectRepresentation(T object, MediaType mediaType) {
        super((mediaType == null) ? MediaType.APPLICATION_JAVA_OBJECT
                : mediaType);
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
        if (MediaType.APPLICATION_JAVA_OBJECT.isCompatible(getMediaType())) {
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(getObject());
            oos.flush();
            // [ifndef android]
        } else if (MediaType.APPLICATION_JAVA_OBJECT_XML
                .isCompatible(getMediaType())) {
            java.beans.XMLEncoder encoder = new java.beans.XMLEncoder(
                    outputStream);
            encoder.writeObject(getObject());
            encoder.close();
            // [enddef]
        }
    }

}
