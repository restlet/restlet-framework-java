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

package org.restlet.ext.spring;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.resource.Representation;
import org.springframework.core.io.AbstractResource;

/**
 * Spring Resource based on a Restlet Representation. DON'T GET CONFUSED,
 * Spring's notion of Resource is different from Restlet's one, actually it's
 * closer to Restlet's Representations.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringResource extends AbstractResource {
    /** The description. */
    private final String description;

    /** Indicates if the representation has already been read. */
    private volatile boolean read = false;

    /** The wrapped representation. */
    private final Representation representation;

    /**
     * Constructor.
     * 
     * @param representation
     *                The description.
     */
    public SpringResource(Representation representation) {
        this(representation, "Restlet Representation");
    }

    /**
     * Constructor.
     * 
     * @param representation
     *                The description.
     * @param description
     *                The description.
     */
    public SpringResource(Representation representation, String description) {
        if (representation == null) {
            throw new IllegalArgumentException(
                    "Representation must not be null");
        }

        this.representation = representation;
        this.description = (description != null) ? description : "";
    }

    /**
     * This implementation compares the underlying InputStream.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this || (obj instanceof SpringResource && ((SpringResource) obj).representation
                .equals(this.representation)));
    }

    /**
     * This implementation always returns <code>true</code>.
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * Returns the description.
     * 
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * This implementation throws IllegalStateException if attempting to read
     * the underlying stream multiple times.
     */
    public InputStream getInputStream() throws IOException,
            IllegalStateException {
        if (this.read && this.representation.isTransient()) {
            throw new IllegalStateException(
                    "Representation has already been read and is transient.");
        }

        this.read = true;
        return this.representation.getStream();
    }

    /**
     * This implementation returns the hash code of the underlying InputStream.
     */
    @Override
    public int hashCode() {
        return this.representation.hashCode();
    }

    /**
     * This implementation always returns <code>true</code>.
     */
    @Override
    public boolean isOpen() {
        return true;
    }

}