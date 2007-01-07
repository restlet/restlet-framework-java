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

package org.restlet.example.book.rest.ch7;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Result;

/**
 * Manages the creation of users.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class UsersResource extends Resource {

    private Reference baseRef;

    public UsersResource(Reference baseRef) {
        this.baseRef = baseRef;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Result post(Representation entity) {
        Result result = null;

        if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            // Parse the entity as a web form
            Form form = new Form(entity);

            // Create a new user
            User newUser = new User();
            newUser.setEmail(form.getFirstValue("user[email]"));
            newUser.setFullName(form.getFirstValue("user[full_name]"));
            newUser.setName(form.getFirstValue("user[name]"));
            newUser.setPassword(form.getFirstValue("user[password]"));

            // Test if user already exists
            if (UserResource.findUser(newUser.getName()) != null) {
                result = new Result(Status.CLIENT_ERROR_CONFLICT);
            } else {
                // Save the new user
                Application.CONTAINER.set(newUser);
                Application.CONTAINER.commit();

                // Update the result
                result = new Result(Status.SUCCESS_CREATED, new Reference(
                        this.baseRef.toString() + "/" + newUser.getName()));
            }
        }

        return result;
    }

}
