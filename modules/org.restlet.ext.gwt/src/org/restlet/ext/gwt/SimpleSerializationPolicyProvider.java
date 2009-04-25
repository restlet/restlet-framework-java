package org.restlet.ext.gwt;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

public class SimpleSerializationPolicyProvider implements
        SerializationPolicyProvider {

    public SerializationPolicy getSerializationPolicy(String moduleBaseURL,
            String serializationPolicyStrongName) {
        return new SimpleSerializationPolicy();
    }

}
