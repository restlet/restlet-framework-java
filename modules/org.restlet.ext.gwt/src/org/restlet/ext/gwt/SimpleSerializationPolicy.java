/**
 * Copyright 2005-2011 Noelios Technologies.
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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Serialization policy that allows the serialization of all the classes and
 * fields.
 * 
 * @author Jerome Louvel
 */
public class SimpleSerializationPolicy extends SerializationPolicy {

    private static final SimpleSerializationPolicy instance = new SimpleSerializationPolicy();

    /**
     * Returns the common instance of this simple policy file.
     * 
     * @return The common instance of this simple policy file.
     */
    public static SerializationPolicy getInstance() {
        return instance;
    }

    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {
        return (clazz != null);
    }

    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {
        return (clazz != null);
    }

    @Override
    public void validateDeserialize(Class<?> clazz)
            throws SerializationException {
        // No validation
    }

    @Override
    public void validateSerialize(Class<?> clazz) throws SerializationException {
        // No validation
    }

}
