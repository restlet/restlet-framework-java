/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

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
        } catch (final ObjectsException e) {
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
