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
 * Character set used to encode characters in textual representations.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class CharacterSet extends Metadata {
    /** All character sets acceptable. */
    public static final CharacterSet ALL = new CharacterSet("*",
            "All character sets");

    /**
     * The ISO/IEC 8859-1 or Latin 1 character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-1">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_1 = new CharacterSet(
            "ISO-8859-1", "ISO/IEC 8859-1 or Latin 1 character set");

    /**
     * The US-ASCII character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/US-ASCII">Wikipedia page</a>
     */
    public static final CharacterSet US_ASCII = new CharacterSet("US-ASCII",
            "US ASCII character set");

    /**
     * The UTF-8 character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia page</a>
     */
    public static final CharacterSet UTF_8 = new CharacterSet("UTF-8",
            "UTF 8 character set");

    /**
     * The UTF-16 character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia page</a>
     */
    public static final CharacterSet UTF_16 = new CharacterSet("UTF-16",
            "UTF 16 character set");

    /**
     * Returns the character set associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated character set.
     */
    public static CharacterSet valueOf(final String name) {
        CharacterSet result = null;

        if (name != null) {
            if (name.equalsIgnoreCase(ALL.getName()))
                result = ALL;
            else if (name.equalsIgnoreCase(ISO_8859_1.getName()))
                result = ISO_8859_1;
            else if (name.equalsIgnoreCase(US_ASCII.getName()))
                result = US_ASCII;
            else if (name.equalsIgnoreCase(UTF_8.getName()))
                result = UTF_8;
            else if (name.equalsIgnoreCase(UTF_16.getName()))
                result = UTF_16;
            else
                result = new CharacterSet(name);
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public CharacterSet(final String name) {
        this(name == null ? null : name.toUpperCase(),
                "Character set or range of character sets");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     */
    public CharacterSet(final String name, final String description) {
        super(name == null ? null : name.toUpperCase(), description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof CharacterSet)
                && getName()
                        .equalsIgnoreCase(((CharacterSet) object).getName());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }
}
