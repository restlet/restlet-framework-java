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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.User;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user.
 * 
 */
public class UserResource extends BaseResource {

    /** The user represented by this resource. */
    private User user;

    /** The list of mailboxes owned by this user. */
    private List<Mailbox> mailboxes;

    public UserResource(Context context, Request request, Response response) {
        super(context, request, response);
        String userId = (String) request.getAttributes().get("userId");
        user = getDAOFactory().getUserDAO().getUserById(userId);

        if (user != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
            mailboxes = getDAOFactory().getUserDAO().getMailboxes(user);
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        getDAOFactory().getUserDAO().deleteUser(user);
        // TODO revenir à la première page de l'application
        getResponse().redirectSeeOther("");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("user", user);
        dataModel.put("mailboxes", mailboxes);
        dataModel.put("resourceRef", getRequest().getResourceRef());

        TemplateRepresentation representation = new TemplateRepresentation(
                "user.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        user
                .setAdministrator(form.getFirstValue("administrator") == null ? user
                        .isAdministrator()
                        : true);
        user.setFirstName(form.getFirstValue("firstName"));
        user.setLastName(form.getFirstValue("lastName"));
        user.setLogin(form.getFirstValue("login"));
        user.setPassword(form.getFirstValue("password"));
        getDAOFactory().getUserDAO().updateUser(user);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }
}
