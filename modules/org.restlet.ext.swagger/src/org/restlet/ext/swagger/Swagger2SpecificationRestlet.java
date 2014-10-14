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
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.cors.CorsResponseHelper;
import org.restlet.ext.apispark.internal.conversion.Swagger2Translator;
import org.restlet.ext.apispark.internal.introspection.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;

import com.wordnik.swagger.models.Swagger;

/**
 * Restlet that generates Swagger documentation in the format defined by the
 * swagger-spec project.<br>
 * It helps to generate the high level documentation for the whole API (set by
 * calling {@link #setApiInboundRoot(org.restlet.Application)} or
 * {@link #setApiInboundRoot(org.restlet.Restlet)} methods, and the
 * documentation for each resource.<br>
 * By default it instrospects the chain of Application's routers, filters,
 * restlet.<br>
 * Use the {@link org.restlet.ext.swagger.JaxrsSwaggerSpecificationRestlet}
 * restlet for Jax-RS applications.
 * 
 * @author Thierry Boileau
 * @link https://github.com/wordnik/swagger-ui
 * @link https://helloreverb.com/developers/swagger
 */
public class Swagger2SpecificationRestlet extends Restlet {

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
    public static final Float SWAGGER_VERSION = 2.0f;

    /** Helper used to add CORS response headers */
    private CorsResponseHelper corsResponseHelper = new CorsResponseHelper();

    /**
     * Default constructor.<br>
     * Sets the {@link #swaggerVersion} to
     * {@link com.wordnik.swagger.core.SwaggerSpec#version()}.
     */
    public Swagger2SpecificationRestlet() {
        this(null);
    }

    /**
     * Constructor.<br>
     * Sets the {@link #swaggerVersion} to
     * {@link com.wordnik.swagger.core.SwaggerSpec#version()}.
     * 
     * @param context
     *            The context.
     */
    public Swagger2SpecificationRestlet(Context context) {
        super(context);
    }

    /**
     * Returns the Swagger documentation of the Web API.
     * 
     * @return The Swagger documentation of the Web API.
     */
    public Representation getSwagger() {
        return new JacksonRepresentation<Swagger>(
                Swagger2Translator.getSwagger(getDefinition()));
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
            synchronized (Swagger2SpecificationRestlet.class) {
                definition = ApplicationIntrospector.getDefinition(application);
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

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        // CORS support for Swagger-UI
        corsResponseHelper
                .addCorsResponseHeaderIfCorsRequest(request, response);

        if (Method.GET.equals(request.getMethod())) {
            response.setEntity(getSwagger());
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
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
     * @param jsonPath
     *            The base path of the API's resource.
     */
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

}
