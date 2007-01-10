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

    private User user;

    public UserResource(String userName) {
        this.userName = userName;
        this.user = findUser(userName);

        if (user != null) {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
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
    public Result delete() {
        if (this.userName != null) {
            Application.CONTAINER.delete(this.userName);
            Application.CONTAINER.commit();
            return new Result(Status.SUCCESS_OK);
        } else {
            return new Result(Status.SERVER_ERROR_INTERNAL);
        }
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
            // Parse the entity as a web form
            Form form = new Form(entity);

            // If the user doesn't exist, create it
            if (this.user == null) {
                this.user = new User();
                this.user.setName(this.userName);
                result = new Result(Status.SUCCESS_CREATED);
            } else {
                result = new Result(Status.SUCCESS_NO_CONTENT);
            }

            this.user.setEmail(form.getFirstValue("user[email]"));
            this.user.setFullName(form.getFirstValue("user[full_name]"));
            this.user.setPassword(form.getFirstValue("user[password]"));

            // Commit the changes
            Application.CONTAINER.set(this.user);
            Application.CONTAINER.commit();
        }

        return result;
    }

}
