/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import java.util.Objects;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.representation.RepresentationInfo;

/**
 * Validation tag equivalent to an HTTP entity tag (E-Tag). A strong entity tag may be shared by two
 * entities of a resource only if they are equivalent by octet equality.<br>
 * <br>
 * A weak entity tag may be shared by two entities of a resource only if the entities are equivalent
 * and could be substituted for each other with no significant change in semantics."
 *
 * @see RepresentationInfo#getTag()
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity
 *     Tags</a>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP Entity Tag
 *     Cache Validators</a>
 * @author Jerome Louvel
 */
public final class Tag {
    /** Tag matching any other tag used in call's condition data. */
    public static final Tag ALL = Tag.parse("*");

    /**
     * Parses a tag formatted as defined by the HTTP standard.
     *
     * @param httpTag The HTTP tag string; if it starts with 'W/' the tag will be marked as weak and
     *     the data following the 'W/' used as the tag; otherwise it should be surrounded with
     *     quotes (e.g., "sometag").
     * @return A new tag instance.
     * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity
     *     Tags</a>
     */
    public static Tag parse(String httpTag) {
        Tag result = null;
        boolean weak = false;
        String httpTagCopy = httpTag;

        if (httpTagCopy.startsWith("W/")) {
            weak = true;
            httpTagCopy = httpTagCopy.substring(2);
        }

        if (httpTagCopy.startsWith("\"") && httpTagCopy.endsWith("\"")) {
            result = new Tag(httpTagCopy.substring(1, httpTagCopy.length() - 1), weak);
        } else if (httpTagCopy.equals("*")) {
            result = new Tag("*", weak);
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING, "Invalid tag format detected: {0}", httpTagCopy);
        }

        return result;
    }

    /** The name. */
    private final String name;

    /** The tag weakness. */
    private final boolean weak;

    /**
     * Default constructor. The opaque tag is set to null, and the weakness indicator is set to
     * true.
     */
    public Tag() {
        this(null, true);
    }

    /**
     * Constructor of weak tags.
     *
     * @param opaqueTag The tag value.
     */
    public Tag(String opaqueTag) {
        this(opaqueTag, true);
    }

    /**
     * Constructor.
     *
     * @param opaqueTag The tag value.
     * @param weak The weakness indicator.
     */
    public Tag(final String opaqueTag, boolean weak) {
        this.name = opaqueTag;
        this.weak = weak;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return equals(obj, true);
    }

    /**
     * Indicates if both tags are equal.
     *
     * @param obj The object to compare to.
     * @param checkWeakness The equality test takes care or not of the weakness.
     * @return True if both tags are equal.
     */
    public boolean equals(final Object obj, boolean checkWeakness) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tag that)) {
            return false;
        }
        return Objects.equals(getName(), that.getName())
                && (checkWeakness || isWeak() == that.isWeak());
    }

    /**
     * Returns tag formatted as an HTTP tag string.
     *
     * @return The formatted HTTP tag string.
     * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity
     *     Tags</a>
     */
    public String format() {
        if ("*".equals(getName())) {
            return "*";
        }

        final StringBuilder sb = new StringBuilder();
        if (isWeak()) {
            sb.append("W/");
        }
        return sb.append('"').append(getName()).append('"').toString();
    }

    /**
     * Returns the name, corresponding to an HTTP opaque tag value.
     *
     * @return The name, corresponding to an HTTP opaque tag value.
     */
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return format().hashCode();
    }

    /**
     * Indicates if the tag is weak.
     *
     * @return True if the tag is weak, false if the tag is strong.
     */
    public boolean isWeak() {
        return this.weak;
    }

    /**
     * Returns the name.
     *
     * @return The name.
     */
    @Override
    public String toString() {
        return getName();
    }
}
