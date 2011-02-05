/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.ext.rdf.foaf;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.example.ext.rdf.foaf.data.ListFacade;
import org.restlet.example.ext.rdf.foaf.objects.ObjectsFacade;
import org.restlet.example.ext.rdf.foaf.resources.ContactResource;
import org.restlet.example.ext.rdf.foaf.resources.ContactsResource;
import org.restlet.example.ext.rdf.foaf.resources.UserResource;
import org.restlet.example.ext.rdf.foaf.resources.UsersResource;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

/**
 * The main Web application.
 */
public class Application extends org.restlet.Application {

    /**
     * Returns a Properties instance loaded from the given URI.
     * 
     * @param propertiesUri
     *            The URI of the properties file.
     * @return A Properties instance loaded from the given URI.
     * @throws IOException
     */
    public static Properties getProperties(String propertiesUri)
            throws IOException {
        ClientResource resource = new ClientResource(propertiesUri);
        try {
            resource.get();
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot access to the configuration file: \"");
            stringBuilder.append(propertiesUri);
            stringBuilder.append("\"");
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        Properties properties = new Properties();
        properties.load(resource.getResponseEntity().getStream());
        return properties;
    }

    public static void main(String... args) throws Exception {
        // Create a component with an HTTP server connector
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8585);
        component.getClients().add(Protocol.FILE);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.HTTP);
        // Attach the application to the default host and start it
        component.getDefaultHost().attach("/foaf", new Application());
        component.start();
    }

    /** Facade object for all access to data. */
    private final ObjectsFacade dataFacade;

    /** Freemarker configuration object. */
    private freemarker.template.Configuration fmc;

    /**
     * File path of the root directory of the web files (images, templates,
     * etc).
     */
    private final String webRootPath;

    /**
     * Constructor.
     * 
     * @throws IOException
     */
    public Application() throws IOException {
        // List of protocols required by the application.
        getConnectorService().getClientProtocols().add(Protocol.FILE);
        getConnectorService().getClientProtocols().add(Protocol.CLAP);
        getConnectorService().getClientProtocols().add(Protocol.HTTP);

        // Look for the configuration file in the classpath
        Properties properties = getProperties("clap://class/config/foafApplication.properties");
        this.webRootPath = properties.getProperty("web.root.path");

        /** Create and chain the Objects and Data facades. */
        this.dataFacade = new ObjectsFacade(new ListFacade());

        try {
            final File templateDir = new File(webRootPath + "/tmpl");
            this.fmc = new freemarker.template.Configuration();
            this.fmc.setDirectoryForTemplateLoading(templateDir);
        } catch (Exception e) {
            getLogger().severe("Unable to configure FreeMarker.");
            e.printStackTrace();
        }

        getMetadataService().addExtension("rdf", MediaType.APPLICATION_RDF_XML,
                true);
        getTunnelService().setExtensionsTunnel(true);
    }

    @Override
    public Restlet createInboundRoot() {
        final Router router = new Router(getContext());

        // Redirect by defaul to the lst of users.
        router.attachDefault(new Redirector(getContext(), "/users",
                Redirector.MODE_CLIENT_PERMANENT));

        final Directory imgDirectory = new Directory(getContext(),
                LocalReference.createFileReference(webRootPath + "/images"));
        // Add a route for the image resources
        router.attach("/images", imgDirectory);

        final Directory cssDirectory = new Directory(getContext(),
                LocalReference
                        .createFileReference(webRootPath + "/stylesheets"));
        // Add a route for the CSS resources
        router.attach("/stylesheets", cssDirectory);

        // Add a route for a Users resource
        router.attach("/users", UsersResource.class);

        // Add a route for a User resource
        router.attach("/users/{userId}", UserResource.class);

        // Add a route for a Contacts resource
        router.attach("/users/{userId}/contacts", ContactsResource.class);

        // Add a route for a Contact resource
        router.attach("/users/{userId}/contacts/{contactId}",
                ContactResource.class);

        return router;
    }

    /**
     * Returns the freemarker configuration object.
     * 
     * @return the freemarker configuration object.
     */
    public freemarker.template.Configuration getFmc() {
        return this.fmc;
    }

    /**
     * Returns the data facade.
     * 
     * @return the data facade.
     */
    public ObjectsFacade getObjectsFacade() {
        return this.dataFacade;
    }
}
