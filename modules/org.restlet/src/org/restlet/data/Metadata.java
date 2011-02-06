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

/**
 * Representations metadata for content negotiation. Metadata is in the form of
 * name-value pairs, where the name corresponds to a standard that defines the
 * value's structure and semantics. Response messages may include both
 * representation metadata and resource metadata: information about the resource
 * that is not specific to the supplied representation." Roy T. Fielding
 * 
 * @see Preference
 * @see <a href=
 *      "http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2"
 *      >Source dissertation</a>
 * @author Jerome Louvel
 */
public abstract class Metadata {
    /** The metadata name like "text/html" or "compress" or "iso-8851-1". */
    private final String name;

    /** The description of this metadata. */
    private final String description;

    /**
     * Returns the parent metadata if available or null.
     * 
     * @return The parent metadata.
     */
    public abstract Metadata getParent();

    /**
     * Indicates if a given metadata is included in the current one. The test is
     * true if both metadata are equal or if the given metadata is within the
     * range of the current one. For example, {@link MediaType#ALL} includes all
     * media types.
     * <p>
     * Examples:
     * <ul>
     * <li>TEXT_ALL.includes(TEXT_PLAIN) -> true</li>
     * <li>TEXT_PLAIN.includes(TEXT_ALL) -> false</li>
     * </ul>
     * 
     * @param included
     *            The metadata to test for inclusion.
     * @return True if the given metadata is included in the current one.
     * @see #isCompatible(Metadata)
     */
    public abstract boolean includes(Metadata included);

    /**
     * Checks if this metadata is compatible with the given metadata.
     * <p>
     * Examples:
     * <ul>
     * <li>TEXT_ALL.isCompatible(TEXT_PLAIN) -> true</li>
     * <li>TEXT_PLAIN.isCompatible(TEXT_ALL) -> true</li>
     * <li>TEXT_PLAIN.isCompatible(APPLICATION_ALL) -> false</li>
     * </ul>
     * 
     * @param otherMetadata
     *            The other metadata to compare.
     * @return True if the metadata are compatible.
     * @see #includes(Metadata)
     */
    public boolean isCompatible(Metadata otherMetadata) {
        return (otherMetadata != null)
                && (includes(otherMetadata) || otherMetadata.includes(this));
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The unique name.
     */
    public Metadata(String name) {
        this(name, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The unique name.
     * @param description
     *            The description.
     */
    public Metadata(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Metadata)
                && ((Metadata) object).getName().equals(getName());
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
     * Returns the name (ex: "text/html" or "compress" or "iso-8851-1").
     * 
     * @return The name (ex: "text/html" or "compress" or "iso-8851-1").
     */
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().hashCode();
    }

    /**
     * Returns the metadata name.
     * 
     * @return The metadata name.
     */
    @Override
    public String toString() {
        return getName();
    }
}
