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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.apispark.internal.conversion.SwaggerTranslator;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.swagger.ApiDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceListing;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.swagger.internal.reflect.Introspector;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import com.wordnik.swagger.core.SwaggerSpec;

/**
 * Restlet that generates Swagger documentation in the format defined by the
 * swagger-spec project.<br>
 * It helps to generate the high level documentation for the whole API (set by
 * calling {@link #setApiInboundRoot(Application)} or
 * {@link #setApiInboundRoot(Restlet)} methods, and the documentation for each
 * resource.<br>
 * By default it instrospects the chain of Application's routers, filters,
 * restlet.<br>
 * Use the {@link JaxrsSwaggerSpecificationRestlet} restlet for Jax-RS
 * applications.
 * 
 * @author Thierry Boileau
 * @see https://github.com/wordnik/swagger-ui
 * @see https://helloreverb.com/developers/swagger
 */
public class SwaggerSpecificationRestlet extends Restlet {

    /** The root Restlet to describe. */
    Restlet apiInboundRoot;

    /** The version of the API. */
    private String apiVersion;

    /** The Application to describe. */
    Application application;

    /** The base path of the API. */
    private String basePath;

    /** The RWADef of the API. */
    private Definition definition;

    /** The root Restlet to describe. */
    private String jsonPath;

    /** The version of Swagger. */
    private String swaggerVersion;

    /**
     * Default constructor.<br>
     * Sets the {@link #swaggerVersion} to {@link SwaggerSpec#version()}.
     */
    public SwaggerSpecificationRestlet() {
        this(null);
    }

    /**
     * Constructor.<br>
     * Sets the {@link #swaggerVersion} to {@link SwaggerSpec#version()}.
     * 
     * @param context
     *            The context.
     */
    public SwaggerSpecificationRestlet(Context context) {
        super(context);
        swaggerVersion = "1.2";
    }

    /**
     * Returns the Swagger documentation of a given resource, also known as
     * "API Declaration" in Swagger vocabulary.
     * 
     * @param category
     *            The category of the resource to describe.
     * @return The representation of the API declaration.
     */
    public Representation getApiDeclaration(String category) {
        return new JacksonRepresentation<ApiDeclaration>(
                SwaggerTranslator.getApiDeclaration(category, getDefinition()));
    }

    /**
     * Returns the root Restlet for the given application.
     * 
     * @return The root Restlet for the given application.
     */
    public Restlet getApiInboundRoot() {
        if (apiInboundRoot == null) {
            if (application != null) {
                apiInboundRoot = application.getInboundRoot();
            }
        }

        return apiInboundRoot;
    }

    /**
     * Returns the API's version.
     * 
     * @return The API's version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns the base path of the API.
     * 
     * @return The base path of the API.
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the application's definition.
     * 
     * @return The application's definition.
     */
    private synchronized Definition getDefinition() {
        if (definition == null) {
            synchronized (SwaggerSpecificationRestlet.class) {
                Introspector i = new Introspector(application, false);
                definition = i.getDefinition();
                // This data seems necessary for Swagger codegen.
                if (definition.getVersion() == null) {
                    definition.setVersion("1.0");
                }
            }
        }

        return definition;
    }

    /**
     * Returns the base path of the API's resource.
     * 
     * @return The base path of the API's resource.
     */
    public String getJsonPath() {
        return jsonPath;
    }

    /**
     * Returns the representation of the whole resource listing of the
     * Application.
     * 
     * @return The representation of the whole resource listing of the
     *         Application.
     */
    public Representation getResourceListing() {
        return new JacksonRepresentation<ResourceListing>(
                SwaggerTranslator.getResourcelisting(getDefinition()));
    }

    /**
     * Returns the version of Swagger used to generate this documentation.
     * 
     * @return The version of Swagger used to generate this documentation.
     */
    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        // CORS support for Swagger-UI
        Series<Header> headers = new Series<Header>(Header.class);
        headers.set(
                "Access-Control-Expose-Headers",
                "Authorization, Link, X-RateLimit-Limit, X-RateLimit-Remaining, X-OAuth-Scopes, X-Accepted-OAuth-Scopes");
        headers.set("Access-Control-Allow-Headers", "Authorization,");
        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Methods", "GET");
        Series<Header> reqHeaders = (Series<Header>) request.getAttributes()
                .get(HeaderConstants.ATTRIBUTE_HEADERS);
        String requestOrigin = reqHeaders.getFirstValue("Origin", "*");
        headers.set("Access-Control-Allow-Origin", requestOrigin);
        response.getAttributes()
                .put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        if (!Method.GET.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        Object resource = request.getAttributes().get("resource");

        if (resource instanceof String) {
            response.setEntity(getApiDeclaration((String) resource));
        } else {
            response.setEntity(getResourceListing());
        }
    }

    /**
     * Sets the root Restlet for the given application.
     * 
     * @param application
     *            The application.
     */
    public void setApiInboundRoot(Application application) {
        this.application = application;
    }

    /**
     * Sets the root Restlet for the given application.
     * 
     * @param apiInboundRoot
     *            The application's root Restlet.
     */
    public void setApiInboundRoot(Restlet apiInboundRoot) {
        this.apiInboundRoot = apiInboundRoot;
    }

    /**
     * Sets the API's version.
     * 
     * @param apiVersion
     *            The API version.
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Sets the base path of the API.
     * 
     * @param basePath
     *            The base path of the API
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Sets the base path of the API's resource.
     * 
     * @param basePath
     *            The base path of the API's resource.
     */
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    /**
     * Sets the version of Swagger used to generate this documentation.
     * 
     * @param swaggerVersion
     *            The version of Swagger.
     */
    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

}
