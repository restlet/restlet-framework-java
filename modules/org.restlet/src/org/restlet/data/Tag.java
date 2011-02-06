/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.representation.RepresentationInfo;

/**
 * Validation tag equivalent to an HTTP entity tag (E-Tag). "A strong entity tag
 * may be shared by two entities of a resource only if they are equivalent by
 * octet equality.<br>
 * <br>
 * A weak entity tag may be shared by two entities of a resource only if the
 * entities are equivalent and could be substituted for each other with no
 * significant change in semantics."
 * 
 * @see RepresentationInfo#getTag()
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
 *      Entity Tags</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP
 *      Entity Tag Cache Validators</a>
 * @author Jerome Louvel
 */
public final class Tag {
    /** Tag matching any other tag, used in call's condition data. */
    public static final Tag ALL = Tag.parse("*");

    /**
     * Parses a tag formatted as defined by the HTTP standard.
     * 
     * @param httpTag
     *            The HTTP tag string; if it starts with 'W/' the tag will be
     *            marked as weak and the data following the 'W/' used as the
     *            tag; otherwise it should be surrounded with quotes (e.g.,
     *            "sometag").
     * @return A new tag instance.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
     *      Entity Tags</a>
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
            result = new Tag(
                    httpTagCopy.substring(1, httpTagCopy.length() - 1), weak);
        } else if (httpTagCopy.equals("*")) {
            result = new Tag("*", weak);
        } else {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Invalid tag format detected: " + httpTagCopy);
        }

        return result;
    }

    /** The name. */
    private volatile String name;

    /** The tag weakness. */
    private final boolean weak;

    /**
     * Default constructor. The opaque tag is set to null and the weakness
     * indicator is set to true.
     */
    public Tag() {
        this(null, true);
    }

    /**
     * Constructor of weak tags.
     * 
     * @param opaqueTag
     *            The tag value.
     */
    public Tag(final String opaqueTag) {
        this(opaqueTag, true);
    }

    /**
     * Constructor.
     * 
     * @param opaqueTag
     *            The tag value.
     * @param weak
     *            The weakness indicator.
     */
    public Tag(final String opaqueTag, boolean weak) {
        this.name = opaqueTag;
        this.weak = weak;
    }

    /**
     * Indicates if both tags are equal.
     * 
     * @param object
     *            The object to compare to.
     * @return True if both tags are equal.
     */
    @Override
    public boolean equals(final Object object) {
        return equals(object, true);
    }

    /**
     * Indicates if both tags are equal.
     * 
     * @param object
     *            The object to compare to.
     * @param checkWeakness
     *            the equality test takes care or not of the weakness.
     * 
     * @return True if both tags are equal.
     */
    public boolean equals(final Object object, boolean checkWeakness) {
        boolean result = (object != null) && (object instanceof Tag);

        if (result) {
            final Tag that = (Tag) object;

            if (checkWeakness) {
                result = (that.isWeak() == isWeak());
            }

            if (result) {
                if (getName() == null) {
                    result = (that.getName() == null);
                } else {
                    result = getName().equals(that.getName());
                }
            }
        }

        return result;
    }

    /**
     * Returns tag formatted as an HTTP tag string.
     * 
     * @return The formatted HTTP tag string.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
     *      Entity Tags</a>
     */
    public String format() {
        if (getName().equals("*")) {
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
