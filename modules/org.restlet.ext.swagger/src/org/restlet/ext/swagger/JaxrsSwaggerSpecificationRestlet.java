/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.jaxrs.JaxRsRestlet;
import org.restlet.ext.swagger.internal.SwaggerJaxRsResourceGenerator;
import org.restlet.ext.swagger.internal.SwaggerRestletIterable;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;

/**
 * Restlet that generates Swagger documentation in the format defined by the
 * swagger-spec project.<br>
 * It helps to generate the high level documentation for the whole API (set by
 * calling {@link #setApiInboundRoot(Application)} or
 * {@link #setApiInboundRoot(Restlet)} methods, and the documentation for each
 * resource.<br>
 * Supports only Jaxrs application and collection of JaxRsRestlet.
 * 
 * @author Grzegorz Godlewski
 * @see https://github.com/wordnik/swagger-ui
 * @see https://helloreverb.com/developers/swagger
 */
public class JaxrsSwaggerSpecificationRestlet extends
        SwaggerSpecificationRestlet {

    /**
     * Default constructor.<br>
     * Sets the {@link #swaggerVersion} to {@link SwaggerSpec#version()}.
     */
    public JaxrsSwaggerSpecificationRestlet() {
        this(null);
    }

    /**
     * Constructor.<br>
     * Sets the {@link #swaggerVersion} to {@link SwaggerSpec#version()}.
     * 
     * @param context
     *            The context.
     */
    public JaxrsSwaggerSpecificationRestlet(Context context) {
        super(context);
    }

    private Class<?> findJaxRsClass(JaxRsRestlet jaxRsRestlet, String path) {
        Set<Class<?>> classes = jaxRsRestlet.getRootResourceClasses();

        for (Class<?> clazz : classes) {
            Api apiAnnotation = clazz.getAnnotation(Api.class);
            if (apiAnnotation != null) {
                if (apiAnnotation.value() == null
                        || apiAnnotation.value().isEmpty()) {
                    if (path == null || path.isEmpty()) {
                        return clazz;
                    }
                } else if (apiAnnotation.value().equals(path)) {
                    return clazz;
                } else if (apiAnnotation.value().startsWith("/")) {
                    if (apiAnnotation.value().substring(1).equals(path)) {
                        return clazz;
                    }
                } else if (apiAnnotation.value().equals(path.substring(1))) {
                    return clazz;
                }
            }
        }

        return null;
    }

    @Override
    public Representation getApiDeclaration(String resourcePath) {
        Documentation documentation = null;

        SwaggerRestletIterable crawler = new SwaggerRestletIterable(
                apiInboundRoot);
        for (Restlet restlet : crawler) {
            JaxRsRestlet jaxRsRestlet = getNextJaxRsRestlet(restlet);
            if (restlet != null) {
                Class<?> clazz = findJaxRsClass(jaxRsRestlet, resourcePath);

                if (clazz != null) {
                    SwaggerJaxRsResourceGenerator generator = new SwaggerJaxRsResourceGenerator();
                    generator.setup(clazz, crawler.getCurrentPath());
                    documentation = generator.parse();

                    documentation.setApiVersion(getApiVersion());
                    documentation.setSwaggerVersion(getSwaggerVersion());
                    documentation.setBasePath(getBasePath());
                    documentation.setResourcePath(resourcePath);
                    break;
                }
            }
        }
        return new JacksonRepresentation<Documentation>(documentation);
    }

    private JaxRsRestlet getNextJaxRsRestlet(Restlet restlet) {
        if (restlet instanceof JaxRsRestlet) {
            return (JaxRsRestlet) restlet;
        } else if (restlet instanceof Filter) {
            return getNextJaxRsRestlet(((Filter) restlet).getNext());
        }
        return null;
    }

    @Override
    public Representation getResourceListing() {
        Documentation documentation = new Documentation();
        documentation.setApiVersion(getApiVersion());
        documentation.setSwaggerVersion(getSwaggerVersion());
        documentation.setBasePath(getBasePath());

        SwaggerRestletIterable crawler = new SwaggerRestletIterable(
                apiInboundRoot);
        for (Restlet restlet : crawler) {
            JaxRsRestlet jaxRsRestlet = getNextJaxRsRestlet(restlet);
            if (restlet != null) {
                Collection<DocumentationEndPoint> endPoints = scan(
                        jaxRsRestlet, crawler.getCurrentPath());
                for (DocumentationEndPoint endPoint : endPoints) {
                    if (endPoint.getPath() != null) {
                        if (getJsonPath() != null) {
                            if (getJsonPath().endsWith("/")) {
                                if (endPoint.getPath() != null
                                        && endPoint.getPath().startsWith("/")) {
                                    endPoint.setPath(getJsonPath()
                                            + endPoint.getPath().substring(1));
                                } else {
                                    endPoint.setPath(getJsonPath()
                                            + endPoint.getPath());
                                }
                            } else {
                                if (endPoint.getPath() != null
                                        && endPoint.getPath().startsWith("/")) {
                                    endPoint.setPath(getJsonPath()
                                            + endPoint.getPath());
                                } else {
                                    endPoint.setPath(getJsonPath() + "/"
                                            + endPoint.getPath());
                                }
                            }
                        } else {
                            endPoint.setPath(endPoint.getPath());
                        }
                    } else {
                        if (getJsonPath() != null) {
                            endPoint.setPath(getJsonPath());
                        }
                    }

                    documentation.addApi(endPoint);
                }
            }
        }
        return new JacksonRepresentation<Documentation>(documentation);
    }

    private Collection<DocumentationEndPoint> scan(JaxRsRestlet jaxRsRestlet,
            String path) {
        List<DocumentationEndPoint> retVal = new ArrayList<DocumentationEndPoint>();
        Set<Class<?>> classes = jaxRsRestlet.getRootResourceClasses();

        for (Class<?> clazz : classes) {
            Api apiAnnotation = clazz.getAnnotation(Api.class);
            Path pathAnnotation = clazz.getAnnotation(Path.class);
            if (apiAnnotation != null && pathAnnotation != null) {
                DocumentationEndPoint ep = new DocumentationEndPoint(
                        apiAnnotation.value(), apiAnnotation.description());
                retVal.add(ep);
            }
        }
        return retVal;
    }

}
