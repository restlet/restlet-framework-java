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

package org.restlet.resource;

import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

/**
 * Contains the results information returned by some methods in Resource.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @deprecated Use Response instead
 */
@Deprecated
public final class Result extends Response {
    /**
     * Constructor.
     * 
     * @param status
     *            The status.
     */
    public Result(Status status) {
        this(status, null, null);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status.
     * @param redirectionRef
     *            The redirection reference.
     */
    public Result(Status status, Reference redirectionRef) {
        this(status, null, redirectionRef);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status.
     * @param entity
     *            The entity.
     */
    public Result(Status status, Representation entity) {
        this(status, entity, null);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status.
     * @param entity
     *            The entity.
     * @param redirectionRef
     *            The redirection reference.
     */
    public Result(Status status, Representation entity, Reference redirectionRef) {
        super(status, entity, redirectionRef);
    }

}
