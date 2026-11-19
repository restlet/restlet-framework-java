/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.restlet.engine.util.DateUtils;
import org.restlet.representation.RepresentationInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Set of conditions applying to a request. This is equivalent to the HTTP
 * conditional headers.<br>
 * <br>
 * The entity tags are compared as RFC 9110 section 13.1 requires: "If-Range"
 * uses the strong comparison ({@link Tag#strongMatch(Tag)}, a weak tag never
 * matches), "If-None-Match" uses the weak comparison
 * ({@link Tag#weakMatch(Tag)}). "If-Match" ignores the weakness, which the
 * strong comparison of the RFC would not; it does not in Restlet 3.0. Note that
 * {@code new Tag(String)} creates a <b>weak</b> tag: use
 * {@code new Tag(String, false)} for a resource that clients must be able to
 * resume with "If-Range". The dates have a precision of one second, and an
 * "If-Modified-Since" date later than the current time is ignored (RFC 2616
 * section 14.25, kept although RFC 9110 does not require it).
 * 
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24"
 *      >If-Match</a>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25"
 *      >If-Modified-Since</a>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26"
 *      >If-None-Match</a>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.27"
 *      >If-Range</a>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28"
 *      >If-Unmodified-Since</a>
 * 
 * @author Jerome Louvel
 */
public final class Conditions {
	/** The "if-match" condition. */
	private volatile List<Tag> match;

	/** The "if-modified-since" condition. */
	private volatile Date modifiedSince;

	/** The "if-none-match" condition. */
	private volatile List<Tag> noneMatch;

	/** The "if-range" condition as a Date. */
	private volatile Date rangeDate;

	/** The "if-range" condition as an entity tag. */
	private volatile Tag rangeTag;

	/** The "if-unmodified-since" condition */
	private volatile Date unmodifiedSince;

	/**
	 * Constructor.
	 */
	public Conditions() {
	}

	/**
	 * Returns the modifiable list of tags that must be matched. Creates a new
	 * instance if no one has been set.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Match" header.
	 * 
	 * @return The "if-match" condition.
	 */
	public List<Tag> getMatch() {
		// Lazy initialization with double-check.
		List<Tag> m = this.match;
		if (m == null) {
			synchronized (this) {
				m = this.match;
				if (m == null) {
					this.match = m = new ArrayList<Tag>();
				}
			}
		}
		return m;
	}

	/**
	 * Returns the condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Modified-Since" header.
	 * 
	 * @return The condition date.
	 */
	public Date getModifiedSince() {
		return this.modifiedSince;
	}

	/**
	 * Returns the modifiable list of tags that mustn't match. Creates a new
	 * instance if no one has been set.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-None-Match" header.
	 * 
	 * @return The list of tags that mustn't match.
	 */
	public List<Tag> getNoneMatch() {
		// Lazy initialization with double-check.
		List<Tag> n = this.noneMatch;
		if (n == null) {
			synchronized (this) {
				n = this.noneMatch;
				if (n == null) {
					this.noneMatch = n = new ArrayList<Tag>();
				}
			}
		}
		return n;
	}

	/**
	 * Returns the range condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Range" header.
	 * 
	 * @return The range condition date.
	 */
	public Date getRangeDate() {
		return rangeDate;
	}

	/**
	 * Returns the range conditional status of an entity.
	 * 
	 * @param representationInfo The representation information that will be tested.
	 * @return the status of the response.
	 */
	public Status getRangeStatus(RepresentationInfo representationInfo) {
		return getRangeStatus((representationInfo == null) ? null : representationInfo.getTag(),
				(representationInfo == null) ? null : representationInfo.getModificationDate());
	}

	/**
	 * Returns the range conditional status of an entity.
	 * 
	 * @param tag              The tag of the entity.
	 * @param modificationDate The modification date of the entity.
	 * @return The status of the response.
	 */
	public Status getRangeStatus(Tag tag, Date modificationDate) {
		Status result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
		if (getRangeTag() != null) {
			boolean all = getRangeTag().equals(Tag.ALL);

			// If a tag exists. RFC 9110 section 13.1.5: strong comparison, a weak
			// tag never matches.
			if (tag != null) {
				if (all || getRangeTag().strongMatch(tag)) {
					result = Status.SUCCESS_OK;
				}
			}
		} else if (getRangeDate() != null) {
			// If a modification date exists. The dates of the headers have a
			// precision of one second.
			if (modificationDate != null && DateUtils.equals(getRangeDate(), modificationDate)) {
				result = Status.SUCCESS_OK;
			}
		}

		return result;
	}

	/**
	 * Returns the range condition based on the entity tag of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Range" header.
	 * 
	 * @return The range entity tag.
	 */
	public Tag getRangeTag() {
		return rangeTag;
	}

	/**
	 * Indicates if a list of tags contains "*".
	 * 
	 * @param tags The tags to look into.
	 * @return True if one of the tags is "*".
	 */
	private static boolean isAll(List<Tag> tags) {
		for (Tag tag : tags) {
			if (Tag.ALL.getName().equals(tag.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the conditional status of a variant using a given method.
	 * 
	 * @param method           The request method.
	 * @param entityExists     Indicates if the entity exists.
	 * @param tag              The tag.
	 * @param modificationDate The modification date.
	 * @return Null if the requested method can be performed, the status of the
	 *         response otherwise.
	 */
	public Status getStatus(Method method, boolean entityExists, Tag tag, Date modificationDate) {
		Status result = null;

		// Is the "if-Match" rule followed or not?
		if ((this.match != null) && !this.match.isEmpty()) {
			boolean matched = false;
			boolean failed = false;
			boolean all = isAll(getMatch());
			String statusMessage = null;

			if (entityExists) {
				// If a tag exists
				if (!all && (tag != null)) {
					// Check if it matches one of the representations already
					// cached by the client
					Tag matchTag;

					for (Iterator<Tag> iter = getMatch().iterator(); !matched && iter.hasNext();) {
						matchTag = iter.next();
						matched = matchTag.equals(tag, false);
					}
				} else {
					matched = all;
				}
			} else {
				// See
				// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24
				// If none of the entity tags match, or if "*" is given and no
				// current entity exists, the server MUST NOT perform the
				// requested method
				failed = all;
				statusMessage = "A non existing resource can't match any tag.";
			}

			failed = failed || !matched;

			if (failed) {
				result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
				if (statusMessage != null) {
					result = new Status(result, statusMessage);
				}
			}
		}

		// Is the "if-None-Match" rule followed or not?
		if ((result == null) && (this.noneMatch != null) && !this.noneMatch.isEmpty()) {
			boolean matched = false;

			if (entityExists) {
				if (isAll(getNoneMatch())) {
					// RFC 9110 section 13.1.2: "*" is false if any current
					// representation exists
					matched = true;
				} else if (tag != null) {
					// Check if it matches one of the representations
					// already cached by the client. RFC 9110 section 13.1.2:
					// weak comparison, whatever the method.
					Tag noneMatchTag;

					for (Iterator<Tag> iter = getNoneMatch().iterator(); !matched && iter.hasNext();) {
						noneMatchTag = iter.next();
						matched = noneMatchTag.weakMatch(tag);
					}

					// The current representation matches one of those already
					// cached by the client
					if (matched) {
						// Check if the current representation has been updated
						// since the "if-modified-since" date. In this case, the
						// rule is followed.
						Date modifiedSince = getModifiedSince();
						boolean isModifiedSince = (modifiedSince != null) && (DateUtils.after(new Date(), modifiedSince)
								|| (modificationDate == null) || DateUtils.after(modifiedSince, modificationDate));
						matched = !isModifiedSince;
					}
				}
			}

			if (matched) {
				if (Method.GET.equals(method) || Method.HEAD.equals(method)) {
					result = Status.REDIRECTION_NOT_MODIFIED;
				} else {
					result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
				}
			}
		}

		// Is the "if-Modified-Since" rule followed or not?
		if ((result == null) && (getModifiedSince() != null)) {
			Date modifiedSince = getModifiedSince();
			boolean isModifiedSince = (DateUtils.after(new Date(), modifiedSince) || (modificationDate == null)
					|| DateUtils.after(modifiedSince, modificationDate));

			if (!isModifiedSince) {
				if (Method.GET.equals(method) || Method.HEAD.equals(method)) {
					result = Status.REDIRECTION_NOT_MODIFIED;
				} else {
					result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
				}
			}
		}

		// Is the "if-Unmodified-Since" rule followed or not?
		if ((result == null) && (getUnmodifiedSince() != null)) {
			Date unModifiedSince = getUnmodifiedSince();
			boolean isUnModifiedSince = ((unModifiedSince == null) || (modificationDate == null)
					|| !DateUtils.before(modificationDate, unModifiedSince));

			if (!isUnModifiedSince) {
				result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
			}
		}

		return result;
	}

	/**
	 * Returns the conditional status of a variant using a given method.
	 * 
	 * @param method             The request method.
	 * @param representationInfo The representation information that will be tested.
	 * @return Null if the requested method can be performed, the status of the
	 *         response otherwise.
	 */
	public Status getStatus(Method method, RepresentationInfo representationInfo) {
		return getStatus(method, representationInfo != null,
				(representationInfo == null) ? null : representationInfo.getTag(),
				(representationInfo == null) ? null : representationInfo.getModificationDate());
	}

	/**
	 * Returns the condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Unmodified-Since" header.
	 * 
	 * @return The condition date.
	 */
	public Date getUnmodifiedSince() {
		return this.unmodifiedSince;
	}

	/**
	 * Indicates if there are some conditions set.
	 * 
	 * @return True if there are some conditions set.
	 */
	public boolean hasSome() {
		return (((this.match != null) && !this.match.isEmpty())
				|| ((this.noneMatch != null) && !this.noneMatch.isEmpty()) || (getModifiedSince() != null)
				|| (getUnmodifiedSince() != null));
	}

	/**
	 * Indicates if there are some range conditions set.
	 * 
	 * @return True if there are some range conditions set.
	 */
	public boolean hasSomeRange() {
		return getRangeTag() != null || getRangeDate() != null;
	}

	/**
	 * Sets the modifiable list of tags that must be matched.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Match" header.
	 * 
	 * @param tags The "if-match" condition.
	 */
	public void setMatch(List<Tag> tags) {
		this.match = tags;
	}

	/**
	 * Sets the condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Modified-Since" header.
	 * 
	 * @param date The modification date.
	 */
	public void setModifiedSince(Date date) {
		this.modifiedSince = DateUtils.unmodifiable(date);
	}

	/**
	 * Sets the modifiable list of tags that mustn't match. Creates a new instance
	 * if no one has been set.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-None-Match" header.
	 * 
	 * @param tags The list of tags that mustn't match.
	 */
	public void setNoneMatch(List<Tag> tags) {
		this.noneMatch = tags;
	}

	/**
	 * Sets the range condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Range" header.
	 * 
	 * @param rangeDate The date of the range condition.
	 */
	public void setRangeDate(Date rangeDate) {
		this.rangeDate = rangeDate;
	}

	/**
	 * Sets the range condition based on the entity tag of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Range" header.
	 * 
	 * @param rangeTag The entity tag of the range condition.
	 */
	public void setRangeTag(Tag rangeTag) {
		this.rangeTag = rangeTag;
	}

	/**
	 * Sets the condition based on the modification date of the requested
	 * variant.<br>
	 * <br>
	 * Note that when used with HTTP connectors, this property maps to the
	 * "If-Unmodified-Since" header.
	 * 
	 * @param date The condition date.
	 */
	public void setUnmodifiedSince(Date date) {
		this.unmodifiedSince = DateUtils.unmodifiable(date);
	}

}
