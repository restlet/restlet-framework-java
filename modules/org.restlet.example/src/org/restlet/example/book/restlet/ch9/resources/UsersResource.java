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

package org.restlet.example.book.restlet.ch9.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.User;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a list of users.
 * 
 */
public class UsersResource extends BaseResource {

    /** The list of application's account. */
    private List<User> users;

    public UsersResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        if (getCurrentUser().isAdministrator()) {
            users = getDAOFactory().getUserDAO().getUsers();
        } else {
            users = new ArrayList<User>();
            users.add(getCurrentUser());
        }
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);

        User user = new User();
        user.setFirstName(form.getFirstValue("firstName"));
        user.setLastName(form.getFirstValue("lastName"));
        user.setLogin(form.getFirstValue("login"));
        user.setPassword(form.getFirstValue("password"));
        user
                .setAdministrator((form.getFirstValue("administrator") == null ? false
                        : true));

        user = getDAOFactory().getUserDAO().createUser(user);
        Reference userRef = new Reference(getRequest().getResourceRef(), user
                .getId());
        getResponse().redirectSeeOther(userRef);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("users", users);
        dataModel.put("resourceRef", getRequest().getResourceRef());

        TemplateRepresentation representation = new TemplateRepresentation(
                "users.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }
}
