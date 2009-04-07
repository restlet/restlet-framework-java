/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.example.ext.rdf.foaf.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.example.ext.rdf.foaf.Application;
import org.restlet.example.ext.rdf.foaf.objects.ObjectsFacade;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import freemarker.template.Configuration;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 */
public class BaseResource extends ServerResource {

    /**
     * Returns the reference of a resource according to its id and the reference
     * of its "parent".
     * 
     * @param parentRef
     *            parent reference.
     * @param childId
     *            id of this resource
     * @return the reference object of the child resource.
     */
    protected Reference getChildReference(Reference parentRef, String childId) {
        if (parentRef.getIdentifier().endsWith("/")) {
            return new Reference(parentRef.getIdentifier() + childId);
        } else {
            return new Reference(parentRef.getIdentifier() + "/" + childId);
        }
    }

    /**
     * Returns the Freemarker's configuration object used for the generation of
     * all HTML representations.
     * 
     * @return the Freemarker's configuration object.
     */
    private Configuration getFmcConfiguration() {
        final Application application = (Application) getApplication();
        return application.getFmc();
    }

    /**
     * Returns a templated representation dedicated to HTML content.
     * 
     * @param templateName
     *            the name of the template.
     * @param dataModel
     *            the collection of data processed by the template engine.
     * @param mediaType
     *            The media type of the representation.
     * @return the representation.
     */
    protected Representation getTemplateRepresentation(String templateName,
            Map<String, Object> dataModel, MediaType mediaType) {
        // The template representation is based on Freemarker.
        return new TemplateRepresentation(templateName, getFmcConfiguration(),
                dataModel, mediaType);
    }

    /**
     * Gives access to the Objects layer.
     * 
     * @return a facade.
     */
    protected ObjectsFacade getObjectsFacade() {
        final Application application = (Application) getApplication();
        return application.getObjectsFacade();
    }
}
