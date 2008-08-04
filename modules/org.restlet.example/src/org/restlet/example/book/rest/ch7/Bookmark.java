/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.example.book.rest.ch7;

import java.util.Date;

/**
 * URI saved and annotated by a user.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Bookmark {

    private User user;

    private String uri;

    private String shortDescription;

    private String longDescription;

    private Date dateTime;

    private boolean restrict;

    public Bookmark(User user, String uri) {
        this.user = user;
        this.uri = uri;
        this.restrict = true;
        this.dateTime = null;
        this.shortDescription = null;
        this.longDescription = null;
    }

    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * @return the longDescription
     */
    public String getLongDescription() {
        return this.longDescription;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return this.shortDescription;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * @return the restrict
     */
    public boolean isRestrict() {
        return this.restrict;
    }

    /**
     * @param dateTime
     *            the dateTime to set
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @param longDescription
     *            the longDescription to set
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * @param restrict
     *            the restrict to set
     */
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    /**
     * @param shortDescription
     *            the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

}
