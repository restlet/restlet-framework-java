/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
 * @see <a href="http://www.springframework.org/">Spring home page</a>
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
