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
 * Validation tag equivalent to the HTTP entity tag. "A strong entity tag may be shared by two
 * entities of a resource only if they are equivalent by octet equality.<br/> A weak entity tag may be shared
 * by two entities of a resource only if the entities are equivalent and could be substituted for each other
 * with no significant change in semantics."
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity Tags</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP Entity Tag Cache
 * Validators</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tag
{
	/** Tag matching any other tag, used in call's condition data. */
	public static final Tag ALL = new Tag("*");

	/** The opaque tag string. */
	private String tag;

	/** The tag weakness. */
	private boolean weak;

	/**
	 * Constructor.
	 * @param tag The tag value.
	 * @param weak The weakness indicator.
	 */
	public Tag(String tag, boolean weak)
	{
		this.tag = tag;
		this.weak = weak;
	}

	/**
	 * Constructor.
	 * @param name The tag name similar to the HTTP tag string; if it starts with a 'W'
	 * the tag will be marked as weak and the data following the 'W' used as the tag;
	 * otherwise it should be surrounded with quotes (e.g., "sometag")
	 */
	public Tag(String name)
	{
		if (name.startsWith("W"))
		{
			this.weak = true;
			name = name.substring(1);
		}
		else
		{
			this.weak = false;
		}

		if (name.startsWith("\"") && name.endsWith("\""))
		{
			this.tag = name.substring(1, name.length() - 1);
		}
		else if (name.equals("*"))
		{
			this.tag = "*";
		}
		else
		{
			throw new IllegalArgumentException("Invalid tag format detected: " + name);
		}
	}

	/**
	 * Returns the equivalent HTTP string.
	 * @return The equivalent HTTP string.
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity Tags</a>
	 */
	public String getName()
	{
		if (getOpaqueTag().equals("*"))
		{
			return "*";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			if (isWeak()) sb.append("W/");
			return sb.append('"').append(getOpaqueTag()).append('"').toString();
		}
	}

	/**
	 * Returns the description.
	 * @return The description.
	 */
	public String getDescription()
	{
		return "Validation tag equivalent to the HTTP entity tag";
	}

	/**
	 * Indicates if the tag is weak.
	 * @return True if the tag is weak, false if the tag is strong.
	 */
	public boolean isWeak()
	{
		return weak;
	}

	/**
	 * Sets the tag weakness.
	 * @param weak True if the tag is weak, false if the tag is strong.
	 */
	public void setWeak(boolean weak)
	{
		this.weak = weak;
	}

	/**
	 * Returns the opaque tag string.
	 * @return The opaque tag string.
	 */
	public String getOpaqueTag()
	{
		return tag;
	}

	/**
	 * Sets the opaque tag string.
	 * @param tag The opaque tag string.
	 */
	public void setOpaqueTag(String tag)
	{
		this.tag = tag;
	}

	/**
	 * Indicates if both tags are equal.
	 * @param tag The tag to compare.
	 * @return True if both tags are equal.
	 */
	public boolean equals(Tag tag)
	{
		return (tag == null) ? false : getName().equals(tag.getName());
	}

}
