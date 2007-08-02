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

package org.restlet.data;

/**
 * Modifier of a representation's media type. Useful to apply compression
 * without losing the identity of the underlying media type.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Encoding extends Metadata {
    /** All encodings acceptable. */
    public static final Encoding ALL = new Encoding("*", "All encodings");

    /** The GNU Zip encoding. */
    public static final Encoding GZIP = new Encoding("gzip", "GZip compression");

    /** The Info-Zip encoding. */
    public static final Encoding ZIP = new Encoding("zip", "Zip compression");

    /** The common Unix file compression. */
    public static final Encoding COMPRESS = new Encoding("compress",
            "Common Unix compression");

    /** The zlib format defined by RFC 1950 and 1951. */
    public static final Encoding DEFLATE = new Encoding("deflate",
            "Deflate compression using the zlib format");

    /** The default (identity) encoding. */
    public static final Encoding IDENTITY = new Encoding("identity",
            "The default encoding with no transformation");

    /**
     * Returns the encoding associated to a name. If an existing constant exists
     * then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *                The name.
     * @return The associated encoding.
     */
    public static Encoding valueOf(final String name) {
        Encoding result = null;

        if (name != null) {
            if (name.equalsIgnoreCase(ALL.getName()))
                result = ALL;
            else if (name.equalsIgnoreCase(GZIP.getName()))
                result = GZIP;
            else if (name.equalsIgnoreCase(ZIP.getName()))
                result = ZIP;
            else if (name.equalsIgnoreCase(COMPRESS.getName()))
                result = COMPRESS;
            else if (name.equalsIgnoreCase(DEFLATE.getName()))
                result = DEFLATE;
            else if (name.equalsIgnoreCase(IDENTITY.getName()))
                result = IDENTITY;
            else
                result = new Encoding(name);
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     */
    public Encoding(final String name) {
        this(name, "Encoding applied to a representation");
    }

    /**
     * Constructor.
     * 
     * @param name
     *                The name.
     * @param description
     *                The description.
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }
}
