/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.spring;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.resource.Representation;
import org.springframework.core.io.AbstractResource;

/**
 * Spring Resource based on a Restlet Representation. Do not get confused,
 * Spring's notion of Resource is different from Restlet's one, actually it's
 * closer to Restlet's Representations.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringResource extends AbstractResource {
    /** The wrapped representation. */
    private final Representation representation;

    /** The description. */
    private final String description;

    /** Indicates if the representation has already been read. */
    private boolean read = false;

    /**
     * Constructor.
     * 
     * @param representation
     *            The description.
     */
    public SpringResource(Representation representation) {
        this(representation, "Resource loaded through a Representation");
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
     * This implementation always returns <code>true</code>.
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * This implementation always returns <code>true</code>.
     */
    @Override
    public boolean isOpen() {
        return true;
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
     * Returns the description.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return this.description;
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
     * This implementation returns the hash code of the underlying InputStream.
     */
    @Override
    public int hashCode() {
        return this.representation.hashCode();
    }

}