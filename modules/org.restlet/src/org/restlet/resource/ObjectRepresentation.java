/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
 */
public class ObjectRepresentation extends OutputRepresentation {
    /** The serializable object. */
    private Object object;

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
    public ObjectRepresentation(Representation serializedRepresentation)
            throws IOException, ClassNotFoundException,
            IllegalArgumentException {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        if (serializedRepresentation.getMediaType().equals(
                MediaType.APPLICATION_JAVA_OBJECT)) {
            ObjectInputStream ois = new ObjectInputStream(serializedRepresentation.getStream());
            this.object = ois.readObject();
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
    public ObjectRepresentation(Serializable object) {
        super(MediaType.APPLICATION_JAVA_OBJECT);
        this.object = object;
    }

    /**
     * Returns the represented object.
     * 
     * @return The represented object.
     * @throws IOException
     */
    public Object getObject() throws IOException {
        return this.object;
    }

    /**
     * Writes the datum as a stream of bytes.
     * 
     * @param outputStream
     *            The stream to use when writing.
     */
	@Override
    public void write(OutputStream outputStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(getObject());
        oos.close();
    }

}
