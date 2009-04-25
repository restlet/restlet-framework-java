package org.restlet.ext.gwt;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class SimpleSerializationPolicy extends SerializationPolicy {

    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {
        return true;
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
