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

package org.restlet.example.book.rest.ch7;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;

/**
 * Resource for a persistent user.
 * 
 * @author Jerome Louvel
 */
public class UserResource extends ServerResource {

    private String login;

    private boolean modifiable;

    private String password;

    private User user;

    private String userName;

    private List<Variant> variants;

    /**
     * Updates the response to challenge the client for credentials.
     */
    public void challenge() {
        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
        getResponse().getChallengeRequests().add(
                new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Restlet"));
    }

    /**
     * Check the authorization credentials.
     * 
     * @return 1 if authentication OK, 0 if it is missing, -1 if it is wrong
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
    public Representation delete() throws ResourceException {
        if (isModifiable()) {
            switch (checkAuthorization()) {
            case 1:
                // Delete all associated bookmarks
                for (final Bookmark bookmark : this.user.getBookmarks()) {
                    getContainer().delete(bookmark);
                }

                // Delete the parent user
                getContainer().delete(this.user);

                // Commit the changes
                getContainer().commit();
                setStatus(Status.SUCCESS_OK);
                break;
            case 0:
                // No authentication provided
                challenge();
                break;
            case -1:
                // Wrong authentication provided
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                break;
            }
        } else {
            return super.delete();
        }

        return null;
    }

    @Override
    public void doInit() {
        this.userName = (String) getRequestAttributes().get("username");
        final ChallengeResponse cr = getChallengeResponse();
        this.login = (cr != null) ? cr.getIdentifier() : null;
        this.password = (cr != null) ? new String(cr.getSecret()) : null;
        this.user = findUser();

        if (this.user != null) {
            this.variants = new ArrayList<Variant>();
            this.variants.add(new Variant(MediaType.TEXT_PLAIN));
        }

        modifiable = true;
    }

    /**
     * Finds the associated user.
     * 
     * @return The user found or null.
     */
    public User findUser() {
        User result = null;

        if (this.userName != null) {
            // Create the query predicate
            final Predicate<User> predicate = new Predicate<User>() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean match(User candidate) {
                    return UserResource.this.userName.equals(candidate
                            .getName());
                }
            };

            // Query the database and get the first result
            final List<User> users = getContainer().query(predicate);
            if ((users != null) && (users.size() > 0)) {
                result = users.get(0);
            }
        }

        return result;
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if ((variant != null)
                && variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            // Creates a text representation
            final StringBuilder sb = new StringBuilder();
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

    /**
     * Returns the associated user.
     * 
     * @return The associated user.
     */
    public User getUser() {
        return this.user;
    }

    @Override
    public List<Variant> getVariants() {
        return variants;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    @Override
    public Representation put(Representation entity) throws ResourceException {
        if (isModifiable()) {
            if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
                    true)) {
                boolean canSet = true;

                if (getUser() == null) {
                    // The user doesn't exist, create it
                    setUser(new User());
                    getUser().setName(this.userName);
                    setStatus(Status.SUCCESS_CREATED);
                } else {
                    // The user already exists, check the authentication
                    switch (checkAuthorization()) {
                    case 1:
                        setStatus(Status.SUCCESS_NO_CONTENT);
                        break;
                    case 0:
                        // No authentication provided
                        challenge();
                        canSet = false;
                        break;
                    case -1:
                        // Wrong authentication provided
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        canSet = false;
                        break;
                    }
                }

                if (canSet) {
                    // Parse the entity as a web form
                    final Form form = new Form(entity);
                    getUser().setEmail(form.getFirstValue("user[email]"));
                    getUser()
                            .setFullName(form.getFirstValue("user[full_name]"));
                    getUser().setPassword(form.getFirstValue("user[password]"));

                    // Commit the changes
                    getContainer().store(getUser());
                    getContainer().commit();
                }
            }
        } else {
            return super.put(entity);
        }

        return null;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    /**
     * Sets the associated user.
     * 
     * @param user
     *            The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }

}
