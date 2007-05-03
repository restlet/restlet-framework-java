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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.resource.Variant;
import org.restlet.util.DateUtils;

/**
 * Set of conditions applying to a request. This is equivalent to the HTTP
 * conditional headers.
 * 
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24">If-Match</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25">If-Modified-Since</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26">If-None-Match</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28">If-Unmodified-Since</a>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Conditions {
    /** The "if-modified-since" condition */
    private Date modifiedSince;

    /** The "if-unmodified-since" condition */
    private Date unmodifiedSince;

    /** The "if-match" condition */
    private List<Tag> match;

    /** The "if-none-match" condition */
    private List<Tag> noneMatch;

    /**
     * Constructor.
     */
    public Conditions() {
    }

    /**
     * Returns the "if-match" condition.
     * 
     * @return The "if-match" condition.
     */
    public List<Tag> getMatch() {
        return this.match;
    }

    /**
     * Returns the "if-modified-since" condition.
     * 
     * @return The "if-modified-since" condition.
     */
    public Date getModifiedSince() {
        return this.modifiedSince;
    }

    /**
     * Returns the "if-none-match" condition.
     * 
     * @return The "if-none-match" condition.
     */
    public List<Tag> getNoneMatch() {
        return this.noneMatch;
    }

    /**
     * Returns the conditional status of a variant using a given method.
     * 
     * @param method
     *            The request method.
     * @param variant
     *            The representation whose entity tag or date of modification
     *            will be tested
     * @return Null if the requested method can be performed, the status of the
     *         response otherwise.
     */
    public Status getStatus(Method method, Variant variant) {
        Status result = null;

        // Is the "if-Match" rule followed or not?
        if (getMatch() != null && getMatch().size() != 0) {
            boolean matched = false;
            boolean failed = false;

            if (variant != null) {
                // If a tag exists
                if (variant.getTag() != null) {
                    // Check if it matches one of the representations already
                    // cached by the client
                    Tag tag;
                    for (Iterator<Tag> iter = getMatch().iterator(); !matched
                            && iter.hasNext();) {
                        tag = iter.next();
                        matched = tag.equals(variant.getTag(), false);
                    }
                }
            } else {
                // see
                // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24
                // If none of the entity tags match, or if "*" is given and no
                // current entity exists, the server MUST NOT perform the
                // requested method
                failed = getMatch().get(0).equals(Tag.ALL);
            }
            failed = failed || !matched;
            if (failed) {
                result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
            }
        }

        // Is the "if-None-Match" rule followed or not?
        if (result == null && getNoneMatch() != null
                && getNoneMatch().size() != 0) {
            boolean matched = false;
            if (variant != null) {
                // If a tag exists
                if (variant.getTag() != null) {
                    // Check if it matches one of the representations
                    // already cached by the client
                    Tag tag;
                    for (Iterator<Tag> iter = getNoneMatch().iterator(); !matched
                            && iter.hasNext();) {
                        tag = iter.next();
                        matched = tag.equals(variant.getTag(), (Method.GET
                                .equals(method) || Method.HEAD.equals(method)));
                    }
                    if (!matched) {
                        Date modifiedSince = getModifiedSince();
                        matched = ((modifiedSince == null)
                                || (variant.getModificationDate() == null) || DateUtils
                                .after(modifiedSince, variant
                                        .getModificationDate()));
                    }
                }
            } else {
                matched = getNoneMatch().get(0).equals(Tag.ALL);
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
        if (result == null && getModifiedSince() != null) {
            Date modifiedSince = getModifiedSince();
            boolean isModifiedSince = ((modifiedSince == null)
                    || (variant.getModificationDate() == null) || DateUtils
                    .after(modifiedSince, variant.getModificationDate()));
            if (!isModifiedSince) {
                result = Status.REDIRECTION_NOT_MODIFIED;
            }
        }

        // Is the "if-Unmodified-Since" rule followed or not?
        if (result == null && getUnmodifiedSince() != null) {
            Date unModifiedSince = getUnmodifiedSince();
            boolean isUnModifiedSince = ((unModifiedSince == null)
                    || (variant.getModificationDate() == null) || DateUtils
                    .after(variant.getModificationDate(), unModifiedSince));
            if (!isUnModifiedSince) {
                result = Status.CLIENT_ERROR_PRECONDITION_FAILED;
            }
        }

        return result;
    }

    /**
     * Returns the "if-unmodified-since" condition.
     * 
     * @return The "if-unmodified-since" condition.
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
        return ((getMatch() != null && !getMatch().isEmpty())
                || (getNoneMatch() != null && !getNoneMatch().isEmpty())
                || (getModifiedSince() != null) || (getUnmodifiedSince() != null));
    }

    /**
     * Sets the "if-match" condition.
     * 
     * @param tags
     *            The "if-match" condition.
     */
    public void setMatch(List<Tag> tags) {
        this.match = tags;
    }

    /**
     * Sets the "if-modified-since" condition.
     * 
     * @param date
     *            The "if-modified-since" condition.
     */
    public void setModifiedSince(Date date) {
        this.modifiedSince = DateUtils.unmodifiable(date);
    }

    /**
     * Sets the "if-none-match" condition.
     * 
     * @param tags
     *            The "if-none-match" condition.
     */
    public void setNoneMatch(List<Tag> tags) {
        this.noneMatch = tags;
    }

    /**
     * Sets the "if-unmodified-since" condition.
     * 
     * @param date
     *            The "if-unmodified-since" condition.
     */
    public void setUnmodifiedSince(Date date) {
        this.unmodifiedSince = DateUtils.unmodifiable(date);
    }

}
