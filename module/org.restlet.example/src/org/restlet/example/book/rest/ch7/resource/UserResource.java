/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.example.book.rest.ch7.resource;

import java.util.List;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.example.book.rest.ch7.Application;
import org.restlet.example.book.rest.ch7.domain.User;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Result;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.db4o.query.Predicate;

/**
 * Resource for a user.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class UserResource extends Resource {

    public static User findUser(final String userName) {
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
            List<User> users = Application.CONTAINER.query(predicate);
            if ((users != null) && (users.size() > 0)) {
                result = users.get(0);
            }
        }

        return result;
    }

    private String userName;

    private String login;

    private String password;

    private User user;

    public UserResource(String userName, String login, String password) {
        this.userName = userName;
        this.login = login;
        this.password = password;
        this.user = findUser(userName);

        if (user != null) {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }
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
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public Result delete() {
        Result result = null;

        switch (checkAuthorization()) {
        case 1:
            Application.CONTAINER.delete(this.user);
            Application.CONTAINER.commit();
            result = new Result(Status.SUCCESS_OK);
        case 0:
            // No authentication provided
            result = new Result(Status.CLIENT_ERROR_CONFLICT);
        case -1:
            // Wrong authenticaiton provided
            result = new Result(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        return result;
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
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

    @Override
    public Result put(Representation entity) {
        Result result = null;

        if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            boolean canSet = true;

            if (this.user == null) {
                // The user doesn't exist, create it
                this.user = new User();
                this.user.setName(this.userName);
                result = new Result(Status.SUCCESS_CREATED);
            } else {
                // The user already exists, check the authentication

                result = new Result(Status.SUCCESS_NO_CONTENT);
            }

            if (canSet) {
                // Parse the entity as a web form
                Form form = new Form(entity);
                this.user.setEmail(form.getFirstValue("user[email]"));
                this.user.setFullName(form.getFirstValue("user[full_name]"));
                this.user.setPassword(form.getFirstValue("user[password]"));

                // Commit the changes
                Application.CONTAINER.set(this.user);
                Application.CONTAINER.commit();
            }
        }

        return result;
    }
}
