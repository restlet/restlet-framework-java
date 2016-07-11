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

package org.restlet.data;

/**
 * Modifier of a representation's media type. Useful to apply compression
 * without losing the identity of the underlying media type.
 * 
 * @author Jerome Louvel
 */
public final class Encoding extends Metadata {
    /** All encodings acceptable. */
    public static final Encoding ALL = new Encoding("*", "All encodings");

    /** The common Unix file compression. */
    public static final Encoding COMPRESS = new Encoding("compress",
            "Common Unix compression");

    /** The zlib format defined by RFC 1950 and 1951. */
    public static final Encoding DEFLATE = new Encoding("deflate",
            "Deflate compression using the zlib format");

    /** The zlib format defined by RFC 1950 and 1951, without wrapping. */
    public static final Encoding DEFLATE_NOWRAP = new Encoding(
            "deflate-no-wrap",
            "Deflate compression using the zlib format (without wrapping)");

    /** The FreeMarker encoding. */
    public static final Encoding FREEMARKER = new Encoding("freemarker",
            "FreeMarker templated representation");

    /** The GNU Zip encoding. */
    public static final Encoding GZIP = new Encoding("gzip", "GZip compression");

    /** The default (identity) encoding. */
    public static final Encoding IDENTITY = new Encoding("identity",
            "The default encoding with no transformation");

    /** The Velocity encoding. */
    public static final Encoding VELOCITY = new Encoding("velocity",
            "Velocity templated representation");

    /** The Info-Zip encoding. */
    public static final Encoding ZIP = new Encoding("zip", "Zip compression");

    /**
     * Returns the encoding associated to a name. If an existing constant exists
     * then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated encoding.
     */
    public static Encoding valueOf(final String name) {
        Encoding result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(ALL.getName())) {
                result = ALL;
            } else if (name.equalsIgnoreCase(GZIP.getName())) {
                result = GZIP;
            } else if (name.equalsIgnoreCase(ZIP.getName())) {
                result = ZIP;
            } else if (name.equalsIgnoreCase(COMPRESS.getName())) {
                result = COMPRESS;
            } else if (name.equalsIgnoreCase(DEFLATE.getName())) {
                result = DEFLATE;
            } else if (name.equalsIgnoreCase(DEFLATE_NOWRAP.getName())) {
                result = DEFLATE_NOWRAP;
            } else if (name.equalsIgnoreCase(IDENTITY.getName())) {
                result = IDENTITY;
            } else if (name.equalsIgnoreCase(FREEMARKER.getName())) {
                result = FREEMARKER;
            } else if (name.equalsIgnoreCase(VELOCITY.getName())) {
                result = VELOCITY;
            } else {
                result = new Encoding(name);
            }
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public Encoding(final String name) {
        this(name, "Encoding applied to a representation");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     */
    public Encoding(final String name, final String description) {
        super(name, description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Encoding)
                && getName().equalsIgnoreCase(((Encoding) object).getName());
    }

    @Override
    public Metadata getParent() {
        return equals(ALL) ? null : ALL;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }

    /**
     * Indicates if a given encoding is included in the current one. The test is
     * true if both encodings are equal or if the given encoding is within the
     * range of the current one. For example, ALL includes all encodings. A null
     * encoding is considered as included into the current one.
     * <p>
     * Examples:
     * <ul>
     * <li>ALL.includes(COMPRESS) -> true</li>
     * <li>COMPRESS.includes(ALL) -> false</li>
     * </ul>
     * 
     * @param included
     *            The encoding to test for inclusion.
     * @return True if the given encoding is included in the current one.
     * @see #isCompatible(Metadata)
     */
    public boolean includes(Metadata included) {
        return equals(ALL) || (included == null) || equals(included);
    }
}
