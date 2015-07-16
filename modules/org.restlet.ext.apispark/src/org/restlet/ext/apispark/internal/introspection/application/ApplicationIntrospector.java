/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.ext.apispark.internal.introspection.application;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.introspection.DocumentedApplication;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;
import org.restlet.routing.VirtualHost;

/**
 * Publish the documentation of a Restlet-based Application to the APISpark
 * console.
 * 
 * @author Thierry Boileau
 */
public class ApplicationIntrospector extends IntrospectionUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(ApplicationIntrospector.class.getName());

    private static void addEnpoints(Application application, Reference baseRef,
            Component component, Definition definition, Contract contract,
            CollectInfo collectInfo) {
        String scheme = collectInfo.getSchemes().isEmpty() ? null : collectInfo
                .getSchemes().get(0).getName();

        // Introspect component if any
        if (baseRef != null) {
            Endpoint endpoint = new Endpoint(baseRef.getHostDomain(),
                    baseRef.getHostPort(), baseRef.getSchemeProtocol()
                            .getSchemeName(), baseRef.getPath(), scheme);
            definition.getEndpoints().add(endpoint);
        }
        if (component != null) {
            LOGGER.fine("Look for the endpoint.");
            // Look for the endpoint to which this application is attached.
            Endpoint endpoint = ComponentIntrospector.getEndpoint(
                    component.getDefaultHost(), application, scheme);
            if (endpoint != null) {
                definition.getEndpoints().add(endpoint);
            }
            for (VirtualHost virtualHost : component.getHosts()) {
                endpoint = ComponentIntrospector.getEndpoint(virtualHost,
                        application, scheme);
                if (endpoint != null) {
                    definition.getEndpoints().add(endpoint);
                }
            }
        }

        // if no endpoints are defined, add a default one to have correct scheme
        if (definition.getEndpoints().isEmpty()) {
            Endpoint endpoint = new Endpoint("example.com", 80,
                    Protocol.HTTP.getSchemeName(), "/v1", scheme);
            definition.getEndpoints().add(endpoint);
        }
    }

    /**
     * Returns an instance of what must be a subclass of
     * {@link org.restlet.Application}. Returns null in case of errors.
     * 
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of
     *         {@link org.restlet.Application}.
     */
    public static Application getApplication(String className) {
        return ReflectUtils.newInstance(className, Application.class);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            An application to introspect.
     */
    public static Definition getDefinition(Application application) {
        return getDefinition(application, null, null, false);
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     * 
     * @param application
     *            An application to introspect.
     * @param component
     *            An component to introspect in order to get extra details such
     *            as the endpoint.
     * 
     * @return An application description.
     */
    public static Definition getDefinition(Application application,
            Reference baseRef, Component component, boolean useSectionNamingPackageStrategy) {
        List<IntrospectionHelper> introspectionHelpers = IntrospectionUtils.getIntrospectionHelpers();

        // initialize the list to avoid to add a null check statement
        if (introspectionHelpers == null) {
            introspectionHelpers = new ArrayList<>();
        }
        Definition definition = new Definition();

        // Contract
        Contract contract = new Contract();
        contract.setDescription(StringUtils.nullToEmpty(application
                .getDescription()));
        if (application.toString().equals(application.getName())) {
            LOGGER.log(Level.WARNING,
                    "Please provide a name to your application by overriding its method getName. Used "
                            + application.getClass().getSimpleName() + " by default.");
            contract.setName(application.getClass().getSimpleName());
        } else {
            contract.setName(application.getName());
        }

        // Sections
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.setUseSectionNamingPackageStrategy(useSectionNamingPackageStrategy);

        if (application instanceof DocumentedApplication) {
            DocumentedApplication documentedApplication = (DocumentedApplication) application;
            if (documentedApplication.getSections() != null) {
                collectInfo.setSections(documentedApplication.getSections());
            }
        }
        definition.setContract(contract);

        // Go through restlet nodes to collect resources, representations and
        // schemes
        RestletCollector.collect(collectInfo /*
                                              * resources are added during
                                              * collect
                                              */, "" /* start path is empty */,
                application.getInboundRoot(), null /*
                                                    * there is no challenge
                                                    * scheme yet
                                                    */, introspectionHelpers);

        // add resources
        contract.setResources(collectInfo.getResources());
        // add representations
        contract.setRepresentations(collectInfo.getRepresentations());
        // add sections
        contract.setSections(collectInfo.getSections());

        addEnpoints(application, baseRef, component, definition, contract,
                collectInfo);

        sortDefinition(definition);

        updateRepresentationsSectionsFromResources(definition);

        for (IntrospectionHelper helper : introspectionHelpers) {
            helper.processDefinition(definition, application.getClass());
        }
        return definition;
    }
}
