/*
 * Copyright 2005-2008 Noelios Technologies.
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
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Related to the part 12 of the tutorial.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class OrdersResource extends UserResource {

    public OrdersResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            result = new StringRepresentation("Orders of user \""
                    + this.userName + "\"");
        }

        return result;
    }

}
