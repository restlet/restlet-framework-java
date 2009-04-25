/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.gwt;

import java.io.Serializable;

import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

/**
 * Representation based on a serializable Java object. This internally reuses
 * the GWT-RPC serialization logic.
 * 
 * @author Jerome Louvel
 * @param <T>
 *            The class to serialize, see {@link Serializable}
 */
public class ObjectRepresentation<T extends Serializable> extends
        StringRepresentation {

    /** The wrapped object. */
    private T object;

    /** The GWT-RPC serialization policy. */
    private SerializationPolicy serializationPolicy;

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
        super(serializedObject);
        this.targetClass = targetClass;
        this.object = null;
        this.serializationPolicy = new SimpleSerializationPolicy();
    }

    /**
     * Constructor for serialization.
     * 
     * @param object
     *            The object to serialize.
     */
    @SuppressWarnings("unchecked")
    public ObjectRepresentation(T object) {
        super(null);
        this.object = object;
        this.targetClass = (Class<T>) object.getClass();
        this.serializationPolicy = new SimpleSerializationPolicy();
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
                ServerSerializationStreamReader objectReader = new ServerSerializationStreamReader(
                        Engine.getClassLoader(),
                        new SimpleSerializationPolicyProvider());
                objectReader.prepareToRead(getText());
                this.object = (T) objectReader
                        .deserializeValue(this.targetClass);
            } catch (Exception e) {
                this.object = null;
                e.printStackTrace();
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

    @Override
    public String getText() {
        if ((this.object != null) && (super.getText() == null)) {
            try {
                ServerSerializationStreamWriter objectWriter = new ServerSerializationStreamWriter(
                        getSerializationPolicy());
                objectWriter.serializeValue(this.object, this.targetClass);
                setText(objectWriter.toString());
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

}
