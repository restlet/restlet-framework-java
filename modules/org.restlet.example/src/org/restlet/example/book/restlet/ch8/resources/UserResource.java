/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch8.resources;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user.
 */
public class UserResource extends BaseResource {

    /** The user represented by this resource. */
    private User user;

    /** The list of mailboxes owned by this user. */
    private List<Mailbox> mailboxes;

    public UserResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            // Get user thanks to its ID taken from the resource's
            // URI.
            final String userId = Reference.decode((String) request
                    .getAttributes().get("userId"));
            this.user = getObjectsFacade().getUserById(userId);

            if (this.user != null) {
                this.mailboxes = getObjectsFacade().getMailboxes(this.user);
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        } else {
            // Anonymous access.
            setModifiable(false);
        }
    }

    /**
     * Remove this resource.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        getObjectsFacade().deleteUser(this.user);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("user", this.user);
        dataModel.put("mailboxes", this.mailboxes);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("user.html", dataModel);
    }

    /**
     * Update the underlying user according to the given representation.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        this.user
                .setAdministrator(form.getFirstValue("administrator") == null ? this.user
                        .isAdministrator()
                        : true);
        this.user.setFirstName(form.getFirstValue("firstName"));
        this.user.setLastName(form.getFirstValue("lastName"));
        this.user.setLogin(form.getFirstValue("login"));
        this.user.setPassword(form.getFirstValue("password"));
        getObjectsFacade().updateUser(this.user);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }
}
