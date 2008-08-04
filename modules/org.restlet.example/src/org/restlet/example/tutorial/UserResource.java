/*
 * Copyright 2005-2007 Noelios Technologies.
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

package org.restlet.example.tutorial;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Related to the part 12 of the tutorial.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class UserResource extends Resource {
    String userName;

    Object user;

    public UserResource(Context context, Request request, Response response) {
        super(context, request, response);
        this.userName = (String) request.getAttributes().get("user");
        this.user = null; // Could be a lookup to a domain object.

        // Here we add the representation variants exposed
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;
        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            result = new StringRepresentation("Account of user \""
                    + this.userName + "\"");
        }
        return result;
    }
}
