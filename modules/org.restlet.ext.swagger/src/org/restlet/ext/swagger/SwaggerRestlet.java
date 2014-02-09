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
 * Restlet that supports Swagger documentation. Relies on JaxRs extension.
 * 
 * @author Grzegorz Godlewski
 */
public class SwaggerRestlet extends Restlet {
    /** The Restlet base path. */
    private String basePath;

    /** The Restlet base path. */
    private Restlet inboundRoot;

    private String jsonPath;

    private String swaggerVersion;

    private String version;

    /**
     * Returns the class of Restlet associated to the given path relatively to a
     * root Restlet.
     * 
     * @param rootRestlet
     *            The root Restlet.
     * @param path
     *            The path to find.
     * @return The class of Restlet associated to the given path relatively to a
     *         root Restlet.
     */

    private Class<?> findJaxRsClass(JaxRsRestlet rootRestlet, String path) {
        if (path != null) {
            Set<Class<?>> classes = rootRestlet.getRootResourceClasses();

            for (Class<?> clazz : classes) {
                Api apiAnnotation = clazz.getAnnotation(Api.class);
                if (apiAnnotation != null && path.equals(apiAnnotation.value())) {
                    return clazz;
                }
            }
        }

        return null;
    }

    public Restlet getInboundRoot() {
        return inboundRoot;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        String baseRef = request.getResourceRef().getBaseRef()
                .toString(false, false);
        String resourcePath = request.getResourceRef().toString(false, false)
                .substring(baseRef.length());

        if (resourcePath.isEmpty() || resourcePath.equals("/")) {
            Documentation documentation = new Documentation();
            documentation.setApiVersion(version);
            documentation.setSwaggerVersion(swaggerVersion);
            documentation.setBasePath(basePath);

            SwaggerRestletIterable crawler = new SwaggerRestletIterable(
                    inboundRoot);
            for (Restlet restlet : crawler) {
                if (restlet instanceof JaxRsRestlet) {
                    JaxRsRestlet jaxRsRestlet = (JaxRsRestlet) restlet;
                    Collection<DocumentationEndPoint> endPoints = scan(
                            jaxRsRestlet, crawler.getCurrentPath());
                    for (DocumentationEndPoint endPoint : endPoints) {
                        endPoint.setPath(jsonPath + "/" + endPoint.getPath());
                        documentation.addApi(endPoint);
                    }
                }
            }

            response.setEntity(new JacksonRepresentation<Documentation>(
                    documentation));
            return;
        } else {
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            SwaggerRestletIterable crawler = new SwaggerRestletIterable(
                    inboundRoot);
            for (Restlet restlet : crawler) {
                if (restlet instanceof JaxRsRestlet) {
                    JaxRsRestlet jaxRsRestlet = (JaxRsRestlet) restlet;

                    Class<?> clazz = findJaxRsClass(jaxRsRestlet, resourcePath);

                    if (clazz != null) {
                        SwaggerJaxRsResourceGenerator generator = new SwaggerJaxRsResourceGenerator();

                        generator.setup(clazz, crawler.getCurrentPath());
                        Documentation documentation = generator.parse();

                        documentation.setApiVersion(version);
                        documentation.setSwaggerVersion(swaggerVersion);
                        documentation.setBasePath(basePath);
                        documentation.setResourcePath(resourcePath);

                        response.setEntity(new JacksonRepresentation<Documentation>(
                                documentation));
                        return;
                    }
                }
            }
        }

        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    /**
     * Introspects the given root Restlet and returns a documentation for each
     * sub Restlet.
     * 
     * @param rootRestlet
     *            The root Restlet.
     * @param path
     *            The associated path.
     * @return The documentation for the set of sub Restlet.
     */
    private Collection<DocumentationEndPoint> scan(JaxRsRestlet rootRestlet,
            String path) {
        List<DocumentationEndPoint> retVal = new ArrayList<DocumentationEndPoint>();
        Set<Class<?>> classes = rootRestlet.getRootResourceClasses();

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

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setInboundRoot(Restlet apiInboundRoot) {
        this.inboundRoot = apiInboundRoot;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
