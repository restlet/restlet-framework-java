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

package org.restlet.ext.jaxrs.client;

import java.lang.reflect.Method;

import javax.ws.rs.Path;

import org.restlet.Request;
import org.restlet.engine.resource.ClientInvocationHandler;
import org.restlet.resource.ClientResource;

/**
 * Reflection proxy invocation handler created for the
 * {@link JaxRsClientResource#wrap(Class)} and related methods.
 * 
 * @see JaxRsClientResource
 * @see ClientInvocationHandler
 * 
 * @author Shaun Elliott
 * 
 * @param <T>
 *            The annotated resource interface.
 */
public class JaxRsClientInvocationHandler<T> extends ClientInvocationHandler<T> {
    
    /**
     * Constructor.
     * 
     * @param clientResource
     *            The client resource.
     * @param resourceInterface The annotated resource interface.
     */
    public JaxRsClientInvocationHandler(ClientResource clientResource,
            Class<? extends T> resourceInterface) {
        super(clientResource, resourceInterface, JaxRsAnnotationUtils
                .getInstance());
    }

    @Override
    protected Request getRequest(Method javaMethod) {
        Request request = super.getRequest(javaMethod);
        
        Path methodPathAnnotation = javaMethod.getAnnotation(Path.class);
        if (methodPathAnnotation != null) {
            String methodPath = methodPathAnnotation.value();
            if (methodPath != null && methodPath.length() > 0) {
                request.getResourceRef().setPath(
                        request.getResourceRef().getPath() + "/" + methodPath);
            }
        }

        return request;
    }

}
