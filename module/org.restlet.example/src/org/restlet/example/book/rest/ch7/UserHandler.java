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

import java.util.List;

import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import com.db4o.query.Predicate;

/**
 * @author Jerome Louvel (contact@noelios.com)
 */
public class UserHandler extends Handler {

    @Override
    public Resource findTarget(final Request request, Response response) {
        Resource result = null;

        // Create the query predicate
        Predicate<User> predicate = new Predicate<User>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean match(User candidate) {
                String userName = (String) request.getAttributes().get(
                        "username");
                return (userName != null)
                        && (userName.equals(candidate.getName()));
            }
        };

        // Query the database and get the first result
        List<User> users = Application.CONTAINER.query(predicate);
        if ((users != null) && (users.size() > 0)) {
            result = new UserResource(users.get(0));
        }

        return result;
    }
}
