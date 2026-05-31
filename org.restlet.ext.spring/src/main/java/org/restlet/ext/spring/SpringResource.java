/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.Representation;
import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Spring Resource based on a Restlet Representation. DON'T GET CONFUSED, Spring's notion of
 * Resource is different from Restlet's one, actually it's closer to Restlet's Representations.
 *
 * @author Jerome Louvel
 */
public class SpringResource extends AbstractResource {
    /** The description. */
    @NonNull private final String description;

    /** Indicates if the representation has already been read. */
    private volatile boolean read = false;

    /** The wrapped representation. */
    private final Representation representation;

    /**
     * Constructor.
     *
     * @param representation The description.
     */
    public SpringResource(Representation representation) {
        this(representation, "Restlet Representation");
    }

    /**
     * Constructor.
     *
     * @param representation The description.
     * @param description The description.
     */
    public SpringResource(Representation representation, @Nullable String description) {
        if (representation == null) {
            throw new IllegalArgumentException("Representation must not be null");
        }

        this.representation = representation;
        this.description = (description != null) ? description : "";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SpringResource that)) {
            return false;
        }
        return Objects.equals(representation, that.representation);
    }

    /** This implementation always returns <code>true</code>. */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * Returns the description.
     *
     * @return The description.
     */
    @Override
    @NonNull public String getDescription() {
        return this.description;
    }

    /**
     * This implementation throws IllegalStateException if attempting to read the underlying stream
     * multiple times.
     */
    @Override
    @NonNull public InputStream getInputStream() throws IOException, IllegalStateException {
        if (this.read && this.representation.isTransient()) {
            throw new IllegalStateException(
                    "Representation has already been read and is transient.");
        }

        this.read = true;
        InputStream stream = this.representation.getStream();
        if (stream == null) {
            throw new IllegalStateException("representation stream");
        }
        return stream;
    }

    /** This implementation returns the hash code of the underlying InputStream. */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(this.representation);
    }

    /** This implementation always returns <code>true</code>. */
    @Override
    public boolean isOpen() {
        return true;
    }
}
