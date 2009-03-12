/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.example.book.restlet.ch8.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.ObjectsException;
import org.restlet.example.book.restlet.ch8.objects.User;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Resource for a list of users.
 */
public class UsersResource extends BaseResource {

    /** The list of application's account. */
    private List<User> users;

    public UsersResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getCurrentUser() != null) {
            // Authenticated access.
            if (getCurrentUser().isAdministrator()) {
                setModifiable(true);
                this.users = getObjectsFacade().getUsers();
            } else {
                this.users = new ArrayList<User>();
                this.users.add(getCurrentUser());
            }
        } else {
            // Anonymous access.
            setModifiable(false);
        }

        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    /**
     * Accept the representation of a new user, and create it.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);

        User user = new User();
        user.setFirstName(form.getFirstValue("firstName"));
        user.setLastName(form.getFirstValue("lastName"));
        user.setLogin(form.getFirstValue("login"));
        user.setPassword(form.getFirstValue("password"));
        user
                .setAdministrator((form.getFirstValue("administrator") == null ? false
                        : true));

        try {
            user = getObjectsFacade().createUser(user);
            getResponse().redirectSeeOther(
                    getChildReference(getRequest().getResourceRef(), user
                            .getId()));
        } catch (ObjectsException e) {
            final Map<String, Object> dataModel = new TreeMap<String, Object>();
            dataModel.put("currentUser", getCurrentUser());
            dataModel.put("users", this.users);
            dataModel.put("resourceRef", getRequest().getResourceRef());
            dataModel.put("rootRef", getRequest().getRootRef());
            dataModel.put("firstName", form.getFirstValue("firstName"));
            dataModel.put("lastName", form.getFirstValue("lastName"));
            dataModel.put("login", form.getFirstValue("login"));
            dataModel.put("password", form.getFirstValue("password"));
            dataModel.put("errorMessage", e.getMessage());

            getResponse().setEntity(
                    getHTMLTemplateRepresentation("users.html", dataModel));
        }
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("users", this.users);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("users.html", dataModel);
    }
}
