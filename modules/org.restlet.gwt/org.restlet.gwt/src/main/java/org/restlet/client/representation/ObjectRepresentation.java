/**
 * Copyright 2005-2020 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.representation;

import org.restlet.client.data.MediaType;
import org.restlet.client.resource.ClientProxy;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Representation based on a serializable Java object. This internally reuses
 * the GWT-RPC serialization logic.
 * 
 * @author Jerome Louvel
 * @param <T>
 *            The class to serialize.
 */
public class ObjectRepresentation<T> extends StringRepresentation {

    /** The wrapped object. */
    private T object;

    /** The serialization stream factory. */
    private SerializationStreamFactory serializationStreamFactory;

    /** Indicates if the serialized object is a String. */
    private boolean string;

    /**
     * Constructor for serialization.
     * 
     * @param resource
     *            The remote resource from which to obtain the serialization
     *            stream factory.
     * @param object
     *            The object to serialize.
     */
    public ObjectRepresentation(ClientProxy resource, T object) {
        this((resource == null) ? null : (SerializationStreamFactory) resource,
                object);
    }

    /**
     * Constructor for serialization.
     * 
     * @param remoteService
     *            The remote service from which to obtain the serialization
     *            stream factory.
     * @param object
     *            The object to serialize.
     */
    public ObjectRepresentation(RemoteService remoteService, T object) {
        this((SerializationStreamFactory) remoteService, object);
    }

    /**
     * Constructor for serialization.
     * 
     * @param serializationStreamFactory
     *            The serialization stream factory.
     * @param object
     *            The object to serialize.
     */
    public ObjectRepresentation(
            SerializationStreamFactory serializationStreamFactory, T object) {
        super(null, MediaType.APPLICATION_JAVA_OBJECT_GWT);
        this.object = object;
        this.serializationStreamFactory = serializationStreamFactory;
    }

    /**
     * Constructor for deserialization.
     * 
     * @param serializedObject
     *            The object serialization text.
     * @param resource
     *            The remote resource from which to obtain the serialization
     *            stream factory.
     */
    public ObjectRepresentation(String serializedObject, ClientProxy resource) {
        this(serializedObject, (resource == null) ? null
                : (SerializationStreamFactory) resource);
    }

    /**
     * Constructor for deserialization.
     * 
     * @param serializedObject
     *            The object serialization text.
     * @param remoteService
     *            The remote service from which to obtain the serialization
     *            stream factory.
     */
    public ObjectRepresentation(String serializedObject,
            RemoteService remoteService) {
        this(serializedObject, (SerializationStreamFactory) remoteService);
    }

    /**
     * Constructor for deserialization.
     * 
     * @param serializedObject
     *            The object serialization text.
     * @param serializationStreamFactory
     *            The serialization stream factory.
     */
    public ObjectRepresentation(String serializedObject,
            SerializationStreamFactory serializationStreamFactory) {
        this(serializedObject, serializationStreamFactory, false);
    }

    /**
     * Constructor for deserialization.
     * 
     * @param serializedObject
     *            The object serialization text.
     * @param serializationStreamFactory
     *            The serialization stream factory.
     */
    public ObjectRepresentation(String serializedObject,
            SerializationStreamFactory serializationStreamFactory,
            boolean string) {
        super(serializedObject, MediaType.APPLICATION_JAVA_OBJECT_GWT);
        this.serializationStreamFactory = serializationStreamFactory;
        this.object = null;
        this.string = string;
    }

    /**
     * The wrapped object. Triggers the deserialization if necessary.
     * 
     * @return The wrapped object.
     */
    @SuppressWarnings("unchecked")
    public T getObject() {
        if ((this.object == null) && (getText() != null)) {
            try {
                // Create a stream reader
                SerializationStreamReader streamReader = getSerializationStreamFactory()
                        .createStreamReader(getText());

                // Deserialize the object
                if (isString()) {
                    this.object = (T) streamReader.readString();
                } else {
                    this.object = (T) streamReader.readObject();
                }

            } catch (Exception e) {
                this.object = null;
                throw new RuntimeException("Unable to deserialize the representation into an object", e);
            }
        }

        return object;
    }

    /**
     * Returns the serialization stream factory.
     * 
     * @return The serialization stream factory.
     */
    public SerializationStreamFactory getSerializationStreamFactory() {
        // Create the serialization stream factory
        return serializationStreamFactory;
    }

    @Override
    public String getText() {
        if ((this.object != null) && (super.getText() == null)) {
            try {
                // Create a stream writer
                SerializationStreamWriter objectWriter = getSerializationStreamFactory()
                        .createStreamWriter();

                // Serialize the object
                if (this.object instanceof String) {
                    objectWriter.writeString((String) this.object);
                } else {
                    objectWriter.writeObject(this.object);
                }

                setText(objectWriter.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return super.getText();
    }

    /**
     * Indicates if the serialized object is a String.
     * 
     * @return True if the serialized object is a String.
     */
    public boolean isString() {
        return string;
    }

    /**
     * Sets the wrapped object.
     * 
     * @param object
     *            The wrapped object.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Sets the serialization stream factory.
     * 
     * @param serializationStreamFactory
     *            The serialization stream factory.
     */
    public void setSerializationStreamFactory(
            SerializationStreamFactory serializationStreamFactory) {
        this.serializationStreamFactory = serializationStreamFactory;
    }

    /**
     * Indicates if the serialized object is a String.
     * 
     * @param string
     *            True if the serialized object is a String.
     */
    public void setString(boolean string) {
        this.string = string;
    }

}
