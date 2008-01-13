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

package org.restlet.example.book.rest.ch7;

import java.util.List;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;

/**
 * Resource for a persistent user.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class UserResource extends Resource {

    private String login;

    private String password;

    private User user;

    private String userName;

    /**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     * @param request
     *                The request to handle.
     * @param response
     *                The response to return.
     */
    public UserResource(Context context, Request request, Response response) {
        super(context, request, response);
        this.userName = (String) request.getAttributes().get("username");
        ChallengeResponse cr = request.getChallengeResponse();
        this.login = (cr != null) ? cr.getIdentifier() : null;
        this.password = (cr != null) ? new String(cr.getSecret()) : null;
        this.user = findUser();

        if (user != null) {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        setModifiable(true);
    }

    /**
     * Updates the response to challenge the client for credentials.
     */
    public void challenge() {
        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
        getResponse().setChallengeRequest(
                new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Restlet"));
    }

    /**
     * Check the authorization credentials.
     * 
     * @return 1 if authentication ok, 0 if it is missing, -1 if it is wrong
     */
    public int checkAuthorization() {
        int result = 0;

        if (this.user != null) {
            if ((this.login != null) && (this.password != null)) {
                // Credentials provided
                if (this.userName.equals(this.login)
                        && this.password.equals(this.user.getPassword())) {
                    result = 1;
                } else {
                    result = -1;
                }
            }
        }

        return result;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        switch (checkAuthorization()) {
        case 1:
            // Delete all associated bookmarks
            for (Bookmark bookmark : this.user.getBookmarks()) {
                getContainer().delete(bookmark);
            }

            // Delete the parent user
            getContainer().delete(this.user);

            // Commit the changes
            getContainer().commit();
            getResponse().setStatus(Status.SUCCESS_OK);
            break;
        case 0:
            // No authentication provided
            challenge();
            break;
        case -1:
            // Wrong authenticaiton provided
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            break;
        }
    }

    /**
     * Finds the associated user.
     * 
     * @return The user found or null.
     */
    public User findUser() {
        User result = null;

        if (userName != null) {
            // Create the query predicate
            Predicate<User> predicate = new Predicate<User>() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean match(User candidate) {
                    return userName.equals(candidate.getName());
                }
            };

            // Query the database and get the first result
            List<User> users = getContainer().query(predicate);
            if ((users != null) && (users.size() > 0)) {
                result = users.get(0);
            }
        }

        return result;
    }

    /**
     * Returns the parent application.
     * 
     * @return the parent application.
     */
    @Override
    public Application getApplication() {
        return (Application) super.getApplication();
    }

    /**
     * Returns the database container.
     * 
     * @return the database container.
     */
    public ObjectContainer getContainer() {
        return getApplication().getContainer();
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation result = null;

        if ((variant != null)
                && variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            // Creates a text representation
            StringBuilder sb = new StringBuilder();
            sb.append("------------\n");
            sb.append("User details\n");
            sb.append("------------\n\n");
            sb.append("Name:  ").append(this.user.getFullName()).append('\n');
            sb.append("Email: ").append(this.user.getEmail()).append('\n');
            result = new StringRepresentation(sb);
        }

        return result;
    }

    /**
     * Returns the associated user.
     * 
     * @return The associated user.
     */
    public User getUser() {
        return this.user;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM, true)) {
            boolean canSet = true;

            if (getUser() == null) {
                // The user doesn't exist, create it
                setUser(new User());
                getUser().setName(this.userName);
                getResponse().setStatus(Status.SUCCESS_CREATED);
            } else {
                // The user already exists, check the authentication
                switch (checkAuthorization()) {
                case 1:
                    getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                    break;
                case 0:
                    // No authentication provided
                    challenge();
                    canSet = false;
                    break;
                case -1:
                    // Wrong authenticaiton provided
                    getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    canSet = false;
                    break;
                }
            }

            if (canSet) {
                // Parse the entity as a web form
                Form form = new Form(entity);
                getUser().setEmail(form.getFirstValue("user[email]"));
                getUser().setFullName(form.getFirstValue("user[full_name]"));
                getUser().setPassword(form.getFirstValue("user[password]"));

                // Commit the changes
                getContainer().set(getUser());
                getContainer().commit();
            }
        }
    }

    /**
     * Sets the associated user.
     * 
     * @param user
     *                The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }

}
