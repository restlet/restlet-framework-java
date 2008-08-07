/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

/**
 * Description of data contained in a resource representation. "A representation
 * consists of data, metadata describing the data, and, on occasion, metadata to
 * describe the metadata (usually for the purpose of verifying message
 * integrity). Metadata is in the form of name-value pairs, where the name
 * corresponds to a standard that defines the value's structure and semantics.
 * Response messages may include both representation metadata and resource
 * metadata: information about the resource that is not specific to the supplied
 * representation." Roy T. Fielding
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source
 *      dissertation</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Metadata {
    /** The metadata name like "text/html" or "compress" or "iso-8851-1". */
    private String name;

    /** The description of this metadata. */
    private String description;

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
