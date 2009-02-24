/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.example.book.restlet.ch8.resources;

import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.Application;
import org.restlet.example.book.restlet.ch8.RmepGuard;
import org.restlet.example.book.restlet.ch8.objects.ObjectsFacade;
import org.restlet.example.book.restlet.ch8.objects.User;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;

import freemarker.template.Configuration;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 */
public class BaseResource extends Resource {

    /** Current user (if the request is authenticated). */
    private User currentUser;

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
        // All access to the root resource is filtered by a Guard which controls
        // the provided login/password, looks for the corresponding user and put
        // this user object in the request's attributes.
        this.currentUser = (User) getRequest().getAttributes().get(
                RmepGuard.CURRENT_USER);
        if (this.currentUser == null) {
            // This request does not target the root resource.
            // This request may be anonymous or the client may preemptively
            // authenticate it.
            if (getRequest().getChallengeResponse() != null) {
                this.currentUser = getObjectsFacade().getUserByLoginPwd(
                        getRequest().getChallengeResponse().getIdentifier(),
                        getRequest().getChallengeResponse().getSecret());
            }
        }
    }

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
     * Returns a User object representing the current user connected or null, if
     * the access is anonymous.
     * 
     * @return the current user connected or null if the access is anonymous.
     */
    protected User getCurrentUser() {
        return this.currentUser;
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
     * @return the representation.
     */
    protected Representation getHTMLTemplateRepresentation(String templateName,
            Map<String, Object> dataModel) {
        // The template representation is based on Freemarker.
        return new TemplateRepresentation(templateName, getFmcConfiguration(),
                dataModel, MediaType.TEXT_HTML);
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
