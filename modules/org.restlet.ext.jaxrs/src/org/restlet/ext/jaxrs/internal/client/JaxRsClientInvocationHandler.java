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

package org.restlet.ext.jaxrs.internal.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.restlet.Request;
import org.restlet.engine.header.Header;
import org.restlet.engine.resource.ClientInvocationHandler;
import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.service.ConverterService;
import org.restlet.util.Series;

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

    private ClientResource clientResource;

	/**
     * Constructor.
     * 
     * @param clientResource
     *            The client resource.
     * @param resourceInterface
     *            The annotated resource interface.
     */
    public JaxRsClientInvocationHandler(ClientResource clientResource,
            Class<? extends T> resourceInterface) {
        super(clientResource, resourceInterface, JaxRsAnnotationUtils
                .getInstance());
        
        this.clientResource = clientResource;
    }

    @Override
    protected Request getRequest(Method javaMethod, Object[] args) {
        Request request = super.getRequest(javaMethod, args);
        
        setRequestParams(javaMethod, args, request);

        setRequestPathToAnnotationPath(javaMethod, request);

        return request;
    }

	private void setRequestParams(Method javaMethod, Object[] args,
			Request request) {
		Annotation[][] parameterAnnotations = javaMethod.getParameterAnnotations();
        for( Annotation[] annotations : parameterAnnotations ) {
            for( Annotation annotation : annotations ) {
                
                if(annotation instanceof HeaderParam) {
                    HeaderParam headerParam = (HeaderParam)annotation;
                    String value = headerParam.value();
                    Series< Header > headers = Util.getHttpHeaders( request );

                    ConverterService converterService = clientResource.getApplication().getConverterService();
                    
                    //TODO - don't just use args[0], each param should be set
                    Representation representation = converterService.toRepresentation( args[0] );
                    try {
                        headers.add( value, representation.getText()  );
                    }
                    catch ( IOException exception ) {
                        throw new RuntimeException(exception);
                    }
                } else if(annotation instanceof QueryParam) {
                    // TODO - encode & map QueryParam
                    //resourceRef.addQueryParameter( new Parameter( "point", "<java.awt.Point>   <x>22</x>   <y>33</y> </java.awt.Point>" ) );
                }
                // TODO - other param types
            }
        }
	}

	private void setRequestPathToAnnotationPath(Method javaMethod,
			Request request) {
		Path methodPathAnnotation = javaMethod.getAnnotation(Path.class);
        if (methodPathAnnotation != null) {
            String methodPath = methodPathAnnotation.value();
            if (methodPath != null && methodPath.length() > 0) {
                request.getResourceRef().setPath(
                        request.getResourceRef().getPath() + "/" + methodPath);
            }
        }
	}

}
