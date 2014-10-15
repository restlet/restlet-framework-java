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

import com.wordnik.swagger.models.Swagger;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.cors.CorsResponseHelper;
import org.restlet.ext.apispark.internal.conversion.swagger.v2_0.Swagger2Translator;
import org.restlet.ext.apispark.internal.introspection.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.introspection.IntrospectorPlugin;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Restlet that generates Swagger documentation in the format defined by the
 * swagger-spec project v2.0.<br>
 * It helps to generate the high level documentation for the whole API (set by
 * calling {@link #setApiInboundRoot(org.restlet.Application)} or
 * {@link #setApiInboundRoot(org.restlet.Restlet)} methods, and the documentation for each
 * resource.<br>
 * By default it instrospects the chain of Application's routers, filters,
 * restlet.<br>
 * Use the {@link org.restlet.ext.swagger.JaxrsSwaggerSpecificationRestlet} restlet for Jax-RS
 * applications.
 *
 * <p>
 * Usage example (in an {@link Application} class):
 * <pre>
 * new Swagger2SpecificationRestlet(this)
 *      .attach(baseRouter);
 * </pre>
 * or
 * <pre>
 * new Swagger2SpecificationRestlet()
 *      .setApiInboundRoot(this)
 *      .addIntrospectorPlugin(new SwaggerAnnotationIntrospectorPlugin()) //provided by swagger-annotation extension
 *      .attach(baseRouter);
 * </pre>
 * </p>
 *
 * @author Manuel Boillod
 * @see <a href="http://github.com/wordnik/swagger-ui">Swagger UI (github)</a>
 * @see <a href="http://petstore.swagger.wordnik.com">Petstore sample application of Swagger-UI</a>
 * @see <a href="http://swagger.io/">Swagger.io website</a>
 */
public class Swagger2SpecificationRestlet extends Restlet {

    /** The version of Swagger. */
    public static final Float SWAGGER_VERSION = 2.0f;

    /** The root Restlet to describe. */
    private Restlet apiInboundRoot;

    /** The version of the API. */
    private String apiVersion;

    /** The Application to describe. */
    private Application application;

    //todo clean unused attributes
    /** The base path of the API. */
    private String basePath;

    /** The RWADef of the API. */
    private Definition definition;

    /** The root Restlet to describe. */
    private String jsonPath;

    /** List of additionnal introspector plugins to use */
    private List<IntrospectorPlugin> introspectorPlugins = new ArrayList<IntrospectorPlugin>();

    /** Helper used to add CORS response headers */
    private CorsResponseHelper corsResponseHelper = new CorsResponseHelper();

    /**
     * Default constructor.<br>
     */
    public Swagger2SpecificationRestlet() {
    }

    /**
     * Constructor.<br>
     *
     * @param application
     *            The application to describe.
     */
    public Swagger2SpecificationRestlet(Application application) {
        this.application = application;
    }

    /**
     * Constructor.<br>
     *
     * @param apiInboundRoot
     *            The api inbound root to describe.
     */
    public Swagger2SpecificationRestlet(Restlet apiInboundRoot) {
        this.application = application;
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
                definition = ApplicationIntrospector.getDefinition(application,
                        null,
                        null,
                        introspectorPlugins);
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
    public Representation getSwagger() {
        return new JacksonRepresentation<Swagger>(
                Swagger2Translator.getSwagger(getDefinition()));
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        // CORS support for Swagger-UI
        corsResponseHelper.addCorsResponseHeaderIfCorsRequest(request, response);

        if (Method.GET.equals(request.getMethod())) {
            response.setEntity(getSwagger());
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

    }

    /**
     * Defines one route (by default "/swagger.json") for serving the
     * application specification.
     *
     * @param router
     *            The router on which defining the new route.
     *
     * @see #attach(org.restlet.routing.Router, String) to attach it with a custom path
     */
    public void attach(Router router) {
        attach(router, "/swagger.json");
    }

    /**
     * Defines one route (by default "/swagger.json") for serving the
     * application specification.
     *
     * @param router
     *            The router on which defining the new route.
     * @param path
     *            The root path of the documentation Restlet.
     *
     * @see #attach(org.restlet.routing.Router) to attach it with the default path
     */
    public void attach(Router router, String path) {
        router.attach(path, this);
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
     * Add an introspector plugin to default introspector
     *
     * @param introspectorPlugin
     *          Introspector Plugin to add
     *
     */
    public Swagger2SpecificationRestlet addIntrospectorPlugin(IntrospectorPlugin introspectorPlugin) {
        introspectorPlugins.add(introspectorPlugin);
        return this;
    }

    /**
     * Sets the API's version.
     *
     * @param apiVersion
     *            The API version.
     */
    public Swagger2SpecificationRestlet setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets the base path of the API.
     *
     * @param basePath
     *            The base path of the API
     */
    public Swagger2SpecificationRestlet setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    /**
     * Sets the base path of the API's resource.
     *
     * @param jsonPath
     *            The base path of the API's resource.
     */
    public Swagger2SpecificationRestlet setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

}
