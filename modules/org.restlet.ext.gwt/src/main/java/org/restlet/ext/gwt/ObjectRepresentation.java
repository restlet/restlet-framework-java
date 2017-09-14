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

package org.restlet.ext.gwt;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

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

    /** The GWT-RPC serialization policy. */
    private SerializationPolicy serializationPolicy;

    /** The GWT-RPC serialization policy provider. */
    private SerializationPolicyProvider serializationPolicyProvider;

    /** The target object class. */
    private Class<T> targetClass;

    /**
     * Constructor for deserialization.
     * 
     * @param serializedObject
     *            The object serialization text.
     * @param targetClass
     *            The target object class.
     */
    public ObjectRepresentation(String serializedObject, Class<T> targetClass) {
        super(serializedObject, MediaType.APPLICATION_JAVA_OBJECT_GWT);
        this.targetClass = targetClass;
        this.object = null;
        this.serializationPolicy = SimpleSerializationPolicy.getInstance();
        this.serializationPolicyProvider = new SimpleSerializationPolicyProvider();
    }

    /**
     * Constructor for serialization.
     * 
     * @param object
     *            The object to serialize.
     */
    @SuppressWarnings("unchecked")
    public ObjectRepresentation(T object) {
        super(null, MediaType.APPLICATION_JAVA_OBJECT_GWT);
        this.object = object;
        this.targetClass = (Class<T>) object.getClass();
        this.serializationPolicy = SimpleSerializationPolicy.getInstance();
        this.serializationPolicyProvider = new SimpleSerializationPolicyProvider();
    }

    /**
     * The wrapped object. Triggers the deserialization if necessary.
     * 
     * @return The wrapped object.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public T getObject() throws IOException {
        if ((this.object == null) && (getText() != null)) {
            try {
                ServerSerializationStreamReader objectReader = new ServerSerializationStreamReader(
                        Engine.getInstance().getClassLoader(),
                        new SimpleSerializationPolicyProvider());
                String encodedString = getText();

                if (encodedString.indexOf('|') == -1) {
                    encodedString = AbstractSerializationStream.SERIALIZATION_STREAM_VERSION
                            + "|1|0|0|0|" + getText() + '|';
                }

                objectReader.prepareToRead(encodedString);
                this.object = (T) objectReader
                        .deserializeValue(this.targetClass);
            } catch (Exception e) {
                this.object = null;
                IOException ioe = new IOException(
                        "Couldn't read the GWT object representation: "
                                + e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }

        }

        return object;
    }

    /**
     * Returns the GWT-RPC serialization policy.
     * 
     * @return The GWT-RPC serialization policy.
     */
    public SerializationPolicy getSerializationPolicy() {
        return serializationPolicy;
    }

    /**
     * Returns the GWT-RPC serialization policy provider.
     * 
     * @return The GWT-RPC serialization policy provider.
     */
    public SerializationPolicyProvider getSerializationPolicyProvider() {
        return serializationPolicyProvider;
    }

    @Override
    public String getText() {
        if ((this.object != null) && (super.getText() == null)) {
            try {
                ServerSerializationStreamWriter objectWriter = new ServerSerializationStreamWriter(
                        getSerializationPolicy());
                objectWriter.serializeValue(this.object, this.targetClass);
                setText("//OK" + objectWriter.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return super.getText();
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
     * Sets the GWT-RPC serialization policy.
     * 
     * @param serializationPolicy
     *            The GWT-RPC serialization policy.
     */
    public void setSerializationPolicy(SerializationPolicy serializationPolicy) {
        this.serializationPolicy = serializationPolicy;
    }

    /**
     * Sets the GWT-RPC serialization policy provider.
     * 
     * @param serializationPolicyProvider
     *            The GWT-RPC serialization policy provider.
     */
    public void setSerializationPolicyProvider(
            SerializationPolicyProvider serializationPolicyProvider) {
        this.serializationPolicyProvider = serializationPolicyProvider;
    }

}
