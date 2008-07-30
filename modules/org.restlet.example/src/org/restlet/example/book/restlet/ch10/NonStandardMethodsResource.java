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

package org.restlet.example.book.restlet.ch10;

import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

/**
 *
 */
public class NonStandardMethodsResource extends Resource {

    /**
     * Does this resource handle "TEST" requests?
     * 
     * @return true.
     */
    public boolean allowTest() {
        return true;
    }

    /**
     * Handles "TEST" requests.
     */
    public void handleTest() {
        if (getRequest().isEntityAvailable()) {
            getResponse().setEntity(getRequest().getEntity());
            getResponse().setStatus(Status.SUCCESS_OK);
        } else {
            getResponse().setEntity(
                    new StringRepresentation("The entity was not available."));
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

}
