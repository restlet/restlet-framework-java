/**
 * Copyright 2005-2013 Restlet S.A.S.
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

package org.restlet.ext.swagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.jaxrs.JaxRsRestlet;
import org.restlet.ext.swagger.internal.SwaggerRestletIterable;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;

/**
 * Servers swagger api docs json 
 * 
 * @author Grzegorz Godlewski
 */
public class SwaggerApiDocsRestlet extends Restlet {
	
	Restlet apiInboundRoot;

	private String apiVersion;

	private String swaggerVersion;

	private String basePath;

	private String jsonPath;
	
	public void setApiInboundRoot(Restlet apiInboundRoot) {
		this.apiInboundRoot = apiInboundRoot;
	}

	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        String baseRef = request.getResourceRef().getBaseRef().toString(false, false);
        String resourcePath = request.getResourceRef().toString(false, false).substring(baseRef.length());

        if (resourcePath.isEmpty() || resourcePath.equals("/")) {
            Documentation documentation = new Documentation();
            documentation.setApiVersion(apiVersion);
            documentation.setSwaggerVersion(swaggerVersion);
            documentation.setBasePath(basePath);
            
            SwaggerRestletIterable crawler = new SwaggerRestletIterable(apiInboundRoot);
            for (Restlet restlet: crawler) {
            	if (restlet instanceof JaxRsRestlet) {
        			JaxRsRestlet jaxRsRestlet = (JaxRsRestlet) restlet;
        			Collection<DocumentationEndPoint> endPoints = scan(jaxRsRestlet, crawler.getCurrentPath());
        			for (DocumentationEndPoint endPoint: endPoints) {
        				endPoint.setPath(jsonPath + "/" + endPoint.getPath());
        				documentation.addApi(endPoint);	
        			}
        		}
            }
            
            response.setEntity(new JacksonRepresentation<Documentation>(documentation));
            return;
        } else {
        	if (resourcePath.startsWith("/")) resourcePath = resourcePath.substring(1);
        	
            SwaggerRestletIterable crawler = new SwaggerRestletIterable(apiInboundRoot);
            for (Restlet restlet: crawler) {
            	if (restlet instanceof JaxRsRestlet) {
        			JaxRsRestlet jaxRsRestlet = (JaxRsRestlet) restlet;
        			
    				Class<?> klazz = findJaxRsClass(jaxRsRestlet, resourcePath);
    				
    				if (klazz != null) {
        				SwaggerJaxRsResourceGenerator generator = new SwaggerJaxRsResourceGenerator();

    					generator.setup(klazz, crawler.getCurrentPath());
    					Documentation documentation = generator.parse();

        		        documentation.setApiVersion(apiVersion);
        		        documentation.setSwaggerVersion(swaggerVersion);
        		        documentation.setBasePath(basePath);
        		        documentation.setResourcePath(resourcePath);

    					response.setEntity(new JacksonRepresentation<Documentation>(documentation));
    		            return;
    				}
        		}
            }
        }
        
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }



	private Class<?> findJaxRsClass(JaxRsRestlet jaxRsRestlet, String path) {
		Set<Class<?>> classes = jaxRsRestlet.getRootResourceClasses();
		
		for (Class<?> klazz: classes) {
			Api apiAnnotation = klazz.getAnnotation(Api.class);
			if (apiAnnotation != null) {
				if (apiAnnotation.value().equals(path)) {
					return klazz;
				}
			}
		}
		
		return null;
	}

	private Collection<DocumentationEndPoint> scan(JaxRsRestlet jaxRsRestlet, String path) {
		List<DocumentationEndPoint> retVal = new ArrayList<DocumentationEndPoint>();
		Set<Class<?>> classes = jaxRsRestlet.getRootResourceClasses();
		
		for (Class<?> klazz: classes) {
			Api apiAnnotation = klazz.getAnnotation(Api.class);
			Path pathAnnotation = klazz.getAnnotation(Path.class);
			if (apiAnnotation != null && pathAnnotation != null) {
				DocumentationEndPoint ep = new DocumentationEndPoint(apiAnnotation.value(), apiAnnotation.description());
				retVal.add(ep);
			}
		}
		return retVal;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public void setSwaggerVersion(String swaggerVersion) {
		this.swaggerVersion = swaggerVersion;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
