/*
 * Copyright 2005-2006 Noelios Consulting.
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

import org.restlet.resource.Representation;
import org.restlet.util.DateUtils;

/**
 * Set of conditions applying to a request. This is equivalent to the HTTP
 * conditional headers.
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
     * Returns the "if-unmodified-since" condition.
     * 
     * @return The "if-unmodified-since" condition.
     */
    public Date getUnmodifiedSince() {
        return this.unmodifiedSince;
    }

    /**
     * Indicates if a representation should be sent as a response entity.<br/>
     * 
     * @param representation
     *            The representation to set.
     * @deprecated Must not be used.
     */
    public boolean isModified(Representation representation) {
        // Indicate if we must send the representation to the client
        boolean send = true;

        // Check the tag conditions
        if ((getNoneMatch() != null) && (getNoneMatch().size() > 0)) {
            boolean matched = false;

            // If a tag exists
            if (representation.getTag() != null) {
                // Check if it matches one of the representations already cached
                // by the client
                Tag tag;
                for (Iterator<Tag> iter = getNoneMatch().iterator(); !matched
                        && iter.hasNext();) {
                    tag = iter.next();
                    matched = tag.equals(representation.getTag())
                            || tag.equals(Tag.ALL);
                }
            }

            send = !matched;
        } else {
            // Was the representation modified since the last client call?
            Date modifiedSince = getModifiedSince();
            send = ((modifiedSince == null)
                    || (representation.getModificationDate() == null) || DateUtils
                    .after(modifiedSince, representation.getModificationDate()));
        }

        return send;
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
