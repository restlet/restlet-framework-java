/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.RepresentationInfo;

/**
 * Set of conditions applying to a request. This is an equivalent to the HTTP conditional headers.
 *
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24" >If-Match</a>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25"
 *     >If-Modified-Since</a>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26"
 *     >If-None-Match</a>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.27" >If-Range</a>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28"
 *     >If-Unmodified-Since</a>
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

    /** Constructor. */
    public Conditions() {
        // Nothing to do
    }

    /**
     * Returns the modifiable list of tags that must be matched. Creates a new instance if no one
     * has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Match" header.
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
                    this.match = m = new ArrayList<>();
                }
            }
        }
        return m;
    }

    /**
     * Returns the condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Modified-Since"
     * header.
     *
     * @return The condition date.
     */
    public Date getModifiedSince() {
        return this.modifiedSince;
    }

    /**
     * Returns the modifiable list of tags that mustn't match. Creates a new instance if no one has
     * been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-None-Match" header.
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
                    this.noneMatch = n = new ArrayList<>();
                }
            }
        }
        return n;
    }

    /**
     * Returns the range condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Range" header.
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
        return getRangeStatus(
                (representationInfo == null) ? null : representationInfo.getTag(),
                (representationInfo == null) ? null : representationInfo.getModificationDate());
    }

    /**
     * Returns the range conditional status of an entity.
     *
     * @param tag The tag of the entity.
     * @param modificationDate The modification date of the entity.
     * @return The status of the response.
     */
    public Status getRangeStatus(Tag tag, Date modificationDate) {
        Status result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
        if (getRangeTag() != null) {
            boolean all = getRangeTag().equals(Tag.ALL);

            // If a tag exists
            if (tag != null && (all || getRangeTag().equals(tag))) {
                result = Status.SUCCESS_OK;
            }

        } else if (getRangeDate() != null && getRangeDate().equals(modificationDate)) {
            result = Status.SUCCESS_OK;
        }

        return result;
    }

    /**
     * Returns the range condition based on the entity tag of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Range" header.
     *
     * @return The range entity tag.
     */
    public Tag getRangeTag() {
        return rangeTag;
    }

    /**
     * Returns the conditional status of a variant using a given method.
     *
     * @param method The request method.
     * @param entityExists Indicates if the entity exists.
     * @param tag The tag.
     * @param modificationDate The modification date.
     * @return Null if the requested method can be performed, the status of the response otherwise.
     */
    public Status getStatus(Method method, boolean entityExists, Tag tag, Date modificationDate) {
        Status result = null;

        // Is the "if-Match" rule followed or not?
        if ((this.match != null) && !this.match.isEmpty()) {
            result = getIfMatchStatus(entityExists, tag);
        }

        // Is the "if-None-Match" rule followed or not?
        if ((result == null) && (this.noneMatch != null) && !this.noneMatch.isEmpty()) {
            result = getIfNoneMatchStatus(method, entityExists, tag, modificationDate);
        }

        // Is the "if-Modified-Since" rule followed or not?
        if ((result == null)
                && (getModifiedSince() != null)
                && isNotModifiedSince(modificationDate)) {
            result = getIfModifiedSinceStatus(method);
        }

        // Is the "if-Unmodified-Since" rule followed or not?
        if ((result == null) && (getUnmodifiedSince() != null)) {
            result = getIfUnmodifiedSinceStatus(modificationDate);
        }

        return result;
    }

    private Status getIfUnmodifiedSinceStatus(final Date modificationDate) {
        final Date unModifiedSince = getUnmodifiedSince();
        boolean isUnModifiedSince =
                ((unModifiedSince == null)
                        || (modificationDate == null)
                        || !DateUtils.before(modificationDate, unModifiedSince));

        if (!isUnModifiedSince) {
            return Status.CLIENT_ERROR_PRECONDITION_FAILED;
        }
        return null;
    }

    private static Status getIfModifiedSinceStatus(final Method method) {
        final Status result;
        if (Method.GET.equals(method) || Method.HEAD.equals(method)) {
            result = Status.REDIRECTION_NOT_MODIFIED;
        } else {
            result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
        }
        return result;
    }

    private Status getIfNoneMatchStatus(
            final Method method,
            final boolean entityExists,
            final Tag tag,
            final Date modificationDate) {
        boolean matched = false;

        final boolean isGetOrHeadMethod = Method.GET.equals(method) || Method.HEAD.equals(method);
        if (entityExists) {
            if (tag == null) {
                matched = isAll(getNoneMatch());
            } else { // Check if the tag matches one of the representations already cached by the
                // client
                Tag noneMatchTag;

                for (Iterator<Tag> iter = getNoneMatch().iterator(); !matched && iter.hasNext(); ) {
                    noneMatchTag = iter.next();
                    matched = noneMatchTag.equals(tag, isGetOrHeadMethod);
                }

                // The current representation matches one of those already
                // cached by the client
                if (matched) {
                    // Check if the current representation has been updated
                    // since the "if-modified-since" date. In this case, the
                    // rule is followed.
                    matched = isNotModifiedSince(modificationDate);
                }
            }
        }

        if (!matched) {
            return null;
        }

        if (isGetOrHeadMethod) {
            return Status.REDIRECTION_NOT_MODIFIED;
        } else {
            return Status.CLIENT_ERROR_PRECONDITION_FAILED;
        }
    }

    private Status getIfMatchStatus(final boolean entityExists, final Tag tag) {
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

                for (Iterator<Tag> iter = getMatch().iterator(); !matched && iter.hasNext(); ) {
                    matchTag = iter.next();
                    matched = matchTag.equals(tag, false);
                }
            } else {
                matched = all;
            }
        } else {
            // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24
            // If none of the entity tags match, or if "*" is given and no
            // current entity exists, the server MUST NOT perform the requested method
            failed = all;
            statusMessage = "A non existing resource can't match any tag.";
        }

        failed = failed || !matched;

        if (failed) {
            if (statusMessage != null) {
                return new Status(Status.CLIENT_ERROR_PRECONDITION_FAILED, statusMessage);
            }
            return Status.CLIENT_ERROR_PRECONDITION_FAILED;
        }
        return null;
    }

    private boolean isAll(List<Tag> tags) {
        return !tags.isEmpty() && Tag.ALL.equals(tags.getFirst());
    }

    private boolean isNotModifiedSince(final Date modificationDate) {
        Date since = getModifiedSince();
        return (since == null)
                || (!DateUtils.after(new Date(), since)
                        && (modificationDate != null)
                        && !DateUtils.after(since, modificationDate));
    }

    /**
     * Returns the conditional status of a variant using a given method.
     *
     * @param method The request method.
     * @param representationInfo The representation information that will be tested.
     * @return Null if the requested method can be performed, the status of the response otherwise.
     */
    public Status getStatus(Method method, RepresentationInfo representationInfo) {
        return getStatus(
                method,
                representationInfo != null,
                (representationInfo == null) ? null : representationInfo.getTag(),
                (representationInfo == null) ? null : representationInfo.getModificationDate());
    }

    /**
     * Returns the condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Unmodified-Since"
     * header.
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
                || ((this.noneMatch != null) && !this.noneMatch.isEmpty())
                || (getModifiedSince() != null)
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
     * Note that when used with HTTP connectors, this property maps to the "If-Match" header.
     *
     * @param tags The "if-match" condition.
     */
    public void setMatch(List<Tag> tags) {
        this.match = tags;
    }

    /**
     * Sets the condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Modified-Since"
     * header.
     *
     * @param date The modification date.
     */
    public void setModifiedSince(Date date) {
        this.modifiedSince = DateUtils.unmodifiable(date);
    }

    /**
     * Sets the modifiable list of tags that mustn't match. Creates a new instance if no one has
     * been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-None-Match" header.
     *
     * @param tags The list of tags that mustn't match.
     */
    public void setNoneMatch(List<Tag> tags) {
        this.noneMatch = tags;
    }

    /**
     * Sets the range condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Range" header.
     *
     * @param rangeDate The date of the range condition.
     */
    public void setRangeDate(Date rangeDate) {
        this.rangeDate = rangeDate;
    }

    /**
     * Sets the range condition based on the entity tag of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Range" header.
     *
     * @param rangeTag The entity tag of the range condition.
     */
    public void setRangeTag(Tag rangeTag) {
        this.rangeTag = rangeTag;
    }

    /**
     * Sets the condition based on the modification date of the requested variant.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "If-Unmodified-Since"
     * header.
     *
     * @param date The condition date.
     */
    public void setUnmodifiedSince(Date date) {
        this.unmodifiedSince = DateUtils.unmodifiable(date);
    }
}
