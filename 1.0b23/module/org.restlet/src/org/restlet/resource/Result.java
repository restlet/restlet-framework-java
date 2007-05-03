/*
 * Copyright 2005-2006 Noelios Consulting.
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
import org.restlet.data.Status;
import org.restlet.resource.Representation;

/**
 * Contains the results information returned by some methods in Resource.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Result {
    /** The optional entity. */
    private Representation entity;

    /** The status. */
    private Status status;

    /** The optional redirection reference. */
    private Reference redirectRef;

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
        this.entity = entity;
        this.status = status;
        this.redirectRef = redirectionRef;
    }

    /**
     * Returns the entity.
     * 
     * @return the entity or null.
     */
    public Representation getEntity() {
        return this.entity;
    }

    /**
     * Returns the redirection reference.
     * 
     * @return the redirection reference or null.
     * @deprecated Use getRedirectRef() instead.
     */
    @Deprecated
    public Reference getRedirectionRef() {
        return getRedirectRef();
    }

    /**
     * Returns the redirection reference.
     * 
     * @return the redirection reference or null.
     */
    public Reference getRedirectRef() {
        return this.redirectRef;
    }

    /**
     * Returns the status.
     * 
     * @return the status.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets the entity.
     * 
     * @param entity
     *            The entity.
     * @deprecated Not needed.
     */
    @Deprecated
    public void setEntity(Representation entity) {
        this.entity = entity;
    }

    /**
     * Sets the redirection reference.
     * 
     * @param redirectionRef
     *            The redirection reference.
     * @deprecated Not needed.
     */
    @Deprecated
    public void setRedirectionRef(Reference redirectionRef) {
        this.redirectRef = redirectionRef;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status.
     * @deprecated Not needed.
     */
    @Deprecated
    public void setStatus(Status status) {
        this.status = status;
    }

}
