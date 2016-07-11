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
 * Metadata used to specify the character set of textual representations.
 * 
 * @author Jerome Louvel
 */
public final class CharacterSet extends Metadata {
    /** All character sets acceptable. */
    public static final CharacterSet ALL = new CharacterSet("*",
            "All character sets");

    /**
     * The ISO/IEC 8859-1 (Latin 1) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-1">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_1 = new CharacterSet(
            "ISO-8859-1", "ISO/IEC 8859-1 or Latin 1 character set");

    /**
     * The ISO/IEC 8859-2 (Latin 2) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-2">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_2 = new CharacterSet(
            "ISO-8859-2", "ISO/IEC 8859-2 or Latin 2 character set");

    /**
     * The ISO/IEC 8859-3 (Latin 3) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-3">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_3 = new CharacterSet(
            "ISO-8859-3", "ISO/IEC 8859-3 or Latin 3 character set");

    /**
     * The ISO/IEC 8859-4 (Latin 4) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-4">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_4 = new CharacterSet(
            "ISO-8859-4", "ISO/IEC 8859-4 or Latin 4 character set");

    /**
     * The ISO/IEC 8859-5 (Cyrillic) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-5">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_5 = new CharacterSet(
            "ISO-8859-5", "ISO/IEC 8859-5 or Cyrillic character set");

    /**
     * The ISO/IEC 8859-6 (Arabic) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-6">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_6 = new CharacterSet(
            "ISO-8859-6", "ISO/IEC 8859-6 or Arabic character set");

    /**
     * The ISO/IEC 8859-7 (Greek) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-7">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_7 = new CharacterSet(
            "ISO-8859-7", "ISO/IEC 8859-7 or Greek character set");

    /**
     * The ISO/IEC 8859-8 (Hebrew) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-8">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_8 = new CharacterSet(
            "ISO-8859-8", "ISO/IEC 8859-8 or Hebrew character set");

    /**
     * The ISO/IEC 8859-9 (Latin 5) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-9">Wikipedia page</a>
     */
    public static final CharacterSet ISO_8859_9 = new CharacterSet(
            "ISO-8859-9", "ISO/IEC 8859-9 or Latin 5 character set");

    /**
     * The ISO/IEC 8859-10 (Latin 6) character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-10">Wikipedia
     *      page</a>
     */
    public static final CharacterSet ISO_8859_10 = new CharacterSet(
            "ISO-8859-10", "ISO/IEC 8859-10 or Latin 6 character set");

    /**
     * The Macintosh ("Mac OS Roman") character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Mac_OS_Roman">Wikipedia
     *      page</a>
     */
    public static final CharacterSet MACINTOSH = new CharacterSet("macintosh",
            "Mac OS Roman character set");

    /**
     * The US-ASCII character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/US-ASCII">Wikipedia page</a>
     */
    public static final CharacterSet US_ASCII = new CharacterSet("US-ASCII",
            "US ASCII character set");

    /**
     * The UTF-16 character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia page</a>
     */
    public static final CharacterSet UTF_16 = new CharacterSet("UTF-16",
            "UTF 16 character set");

    /**
     * The UTF-8 character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia page</a>
     */
    public static final CharacterSet UTF_8 = new CharacterSet("UTF-8",
            "UTF 8 character set");

    /**
     * The Windows-1252 ('ANSI') character set.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Windows-1252">Wikipedia
     *      page</a>
     * 
     */
    public static final CharacterSet WINDOWS_1252 = new CharacterSet(
            "windows-1252", "Windows 1232 character set");

    // [ifndef gwt] member
    /**
     * The default character set of the JVM.
     * 
     * @see java.nio.charset.Charset#defaultCharset()
     */
    public static final CharacterSet DEFAULT = new CharacterSet(
            java.nio.charset.Charset.defaultCharset());

    // [ifdef gwt] member uncomment
    // public static final CharacterSet DEFAULT = new CharacterSet("ISO-8859-1",
    // "ISO/IEC 8859-1 or Latin 1 character set");

    /**
     * Handles mapping between Java character set names and IANA preferred name.
     * For example, "MACROMAN" is not an official IANA name and "ISO-8859-6" is
     * preferred over "arabic".
     * 
     * @param name
     *            The character set name.
     * @return The IANA character set name.
     */
    private static String getIanaName(String name) {
        if (name != null) {
            name = name.toUpperCase();

            if (name.equalsIgnoreCase("MACROMAN")) {
                name = MACINTOSH.getName();
            } else if (name.equalsIgnoreCase("ASCII")) {
                name = US_ASCII.getName();
            } else if (name.equalsIgnoreCase("latin1")) {
                name = ISO_8859_1.getName();
            } else if (name.equalsIgnoreCase("latin2")) {
                name = ISO_8859_2.getName();
            } else if (name.equalsIgnoreCase("latin3")) {
                name = ISO_8859_3.getName();
            } else if (name.equalsIgnoreCase("latin4")) {
                name = ISO_8859_4.getName();
            } else if (name.equalsIgnoreCase("cyrillic")) {
                name = ISO_8859_5.getName();
            } else if (name.equalsIgnoreCase("arabic")) {
                name = ISO_8859_6.getName();
            } else if (name.equalsIgnoreCase("greek")) {
                name = ISO_8859_7.getName();
            } else if (name.equalsIgnoreCase("hebrew")) {
                name = ISO_8859_8.getName();
            } else if (name.equalsIgnoreCase("latin5")) {
                name = ISO_8859_9.getName();
            } else if (name.equalsIgnoreCase("latin6")) {
                name = ISO_8859_10.getName();
            }
        }

        return name;
    }

    /**
     * Returns the character set associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated character set.
     */
    public static CharacterSet valueOf(String name) {
        CharacterSet result = null;
        name = getIanaName(name);

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(ALL.getName())) {
                result = ALL;
            } else if (name.equalsIgnoreCase(ISO_8859_1.getName())) {
                result = ISO_8859_1;
            } else if (name.equalsIgnoreCase(US_ASCII.getName())) {
                result = US_ASCII;
            } else if (name.equalsIgnoreCase(UTF_8.getName())) {
                result = UTF_8;
            } else if (name.equalsIgnoreCase(UTF_16.getName())) {
                result = UTF_16;
            } else if (name.equalsIgnoreCase(WINDOWS_1252.getName())) {
                result = WINDOWS_1252;
            } else if (name.equalsIgnoreCase(MACINTOSH.getName())) {
                result = MACINTOSH;
            } else {
                result = new CharacterSet(name);
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Constructor.
     * 
     * @param charset
     *            The character set.
     */
    public CharacterSet(final java.nio.charset.Charset charset) {
        this(charset.name(), charset.displayName());
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public CharacterSet(String name) {
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
    public CharacterSet(String name, String description) {
        super(getIanaName(name), description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return (object instanceof CharacterSet)
                && getName().equalsIgnoreCase(((CharacterSet) object).getName());
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
     * Indicates if a given character set is included in the current one. The
     * test is true if both character sets are equal or if the given character
     * set is within the range of the current one. For example, ALL includes all
     * character sets. A null character set is considered as included into the
     * current one.
     * <p>
     * Examples:
     * <ul>
     * <li>ALL.includes(UTF_16) -> true</li>
     * <li>UTF_16.includes(ALL) -> false</li>
     * </ul>
     * 
     * @param included
     *            The character set to test for inclusion.
     * @return True if the given character set is included in the current one.
     * @see #isCompatible(Metadata)
     */
    public boolean includes(Metadata included) {
        return equals(ALL) || (included == null) || equals(included);
    }

    // [ifndef gwt] method
    /**
     * Returns the NIO charset matching the character set name.
     * 
     * @return The NIO charset.
     */
    public java.nio.charset.Charset toCharset() {
        return java.nio.charset.Charset.forName(getName());
    }
}
