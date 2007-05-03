/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

/**
 * Character set used to encode characters in textual representations.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class CharacterSet extends Metadata
{
	/** All character sets acceptable. */
	public static final CharacterSet ALL = new CharacterSet("*", "All character sets");

	/**
	 * The ISO/IEC 8859-1 or Latin 1 character set.
	 * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-1">Wikipedia page</a>
	 */
	public static final CharacterSet ISO_8859_1 = new CharacterSet("ISO-8859-1",
			"ISO/IEC 8859-1 or Latin 1 character set");

	/**
	 * The US-ASCII character set.
	 * @see <a href="http://en.wikipedia.org/wiki/US-ASCII">Wikipedia page</a>
	 */
	public static final CharacterSet US_ASCII = new CharacterSet("US-ASCII",
			"US ASCII character set");

	/**
	 * The UTF-8 character set.
	 * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia page</a>
	 */
	public static final CharacterSet UTF_8 = new CharacterSet("UTF-8",
			"UTF 8 character set");

	/**
	 * The UTF-16 character set.
	 * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia page</a>
	 */
	public static final CharacterSet UTF_16 = new CharacterSet("UTF-16",
			"UTF 16 character set");

	/**
	 * Constructor.
	 * @param name The name.
	 */
	public CharacterSet(String name)
	{
		this(name == null ? null : name.toUpperCase(),
				"Character set or range of character sets");
	}

	/**
	 * Constructor.
	 * @param name The name.
	 * @param description The description. 
	 */
	public CharacterSet(String name, String description)
	{
		super(name == null ? null : name.toUpperCase(), description);
	}

	/**
	 * Indicates if the character set is equal to a given one.
	 * @param object The object to compare to.
	 * @return True if the character set is equal to a given one.
	 */
	public boolean equals(Object object)
	{
		return (object instanceof CharacterSet)
				&& getName().equalsIgnoreCase(((CharacterSet) object).getName());
	}
}
