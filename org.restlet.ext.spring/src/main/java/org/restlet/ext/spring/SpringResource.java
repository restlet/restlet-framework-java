/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.spring;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Representation;
import org.springframework.core.io.AbstractResource;

/**
 * Spring Resource based on a Restlet Representation. DON'T GET CONFUSED,
 * Spring's notion of Resource is different from Restlet's one, actually it's
 * closer to Restlet's Representations.
 * 
 * @author Jerome Louvel
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
     *            The description.
     */
    public SpringResource(Representation representation) {
        this(representation, "Restlet Representation");
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The description.
     * @param description
     *            The description.
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
        return ((obj == this) || ((obj instanceof SpringResource) && ((SpringResource) obj).representation
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
        return SystemUtils.hashCode(this.representation);
    }

    /**
     * This implementation always returns <code>true</code>.
     */
    @Override
    public boolean isOpen() {
        return true;
    }

}
