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
 * Validation tag equivalent to the HTTP entity tag. "A strong entity tag may be
 * shared by two entities of a resource only if they are equivalent by octet
 * equality.<br/> A weak entity tag may be shared by two entities of a resource
 * only if the entities are equivalent and could be substituted for each other
 * with no significant change in semantics."
 * 
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
 *      Entity Tags</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP
 *      Entity Tag Cache Validators</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Tag {
	/** Tag matching any other tag, used in call's condition data. */
	public static final Tag ALL = Tag.parse("*");

	/**
	 * Parses a tag formatted as defined by the HTTP standard.
	 * 
	 * @param httpTag
	 *            The HTTP tag string; if it starts with a 'W' the tag will be
	 *            marked as weak and the data following the 'W' used as the tag;
	 *            otherwise it should be surrounded with quotes (e.g.,
	 *            "sometag").
	 * @return A new tag instance.
	 * @see <a
	 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
	 *      Entity Tags</a>
	 */
	public static Tag parse(String httpTag) {
		Tag result = new Tag();

		if (httpTag.startsWith("W")) {
			result.setWeak(true);
			httpTag = httpTag.substring(1);
		} else {
			result.setWeak(false);
		}

		if (httpTag.startsWith("\"") && httpTag.endsWith("\"")) {
			result.setOpaqueTag(httpTag.substring(1, httpTag.length() - 1));
		} else if (httpTag.equals("*")) {
			result.setOpaqueTag("*");
		} else {
			throw new IllegalArgumentException("Invalid tag format detected: "
					+ httpTag);
		}

		return result;
	}

	/** The opaque tag string. */
	private String opaqueTag;

	/** The tag weakness. */
	private boolean weak;

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
	public Tag(String opaqueTag) {
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
	public Tag(String opaqueTag, boolean weak) {
		this.opaqueTag = opaqueTag;
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
	public boolean equals(Object object) {
		boolean result = (object != null) && (object instanceof Tag);

		if (result) {
			Tag that = (Tag) object;
			result = (that.isWeak() == isWeak());

			if (getOpaqueTag() == null) {
				result = (that.getOpaqueTag() == null);
			} else {
				result = getOpaqueTag().equals(that.getOpaqueTag());
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
		if (getOpaqueTag().equals("*")) {
			return "*";
		} else {
			StringBuilder sb = new StringBuilder();
			if (isWeak())
				sb.append("W/");
			return sb.append('"').append(getOpaqueTag()).append('"').toString();
		}
	}

	/**
	 * Returns the description.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return "Validation tag equivalent to the HTTP entity tag";
	}

	/**
	 * Returns the opaque tag string.
	 * 
	 * @return The opaque tag string.
	 */
	public String getOpaqueTag() {
		return opaqueTag;
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
		return weak;
	}

	/**
	 * Sets the opaque tag string.
	 * 
	 * @param opaqueTag
	 *            The opaque tag string.
	 */
	public void setOpaqueTag(String opaqueTag) {
		this.opaqueTag = opaqueTag;
	}

	/**
	 * Sets the tag weakness.
	 * 
	 * @param weak
	 *            True if the tag is weak, false if the tag is strong.
	 */
	public void setWeak(boolean weak) {
		this.weak = weak;
	}

}
