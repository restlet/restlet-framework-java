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

package com.noelios.restlet.example.book.ch7;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.resource.Resource;

/**
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Bookmark extends Resource {

    private User user;

    private String uri;

    private String shortDescription;

    private String longDescription;

    private Date dateTime;

    private boolean restrict;

    private List<Tag> tags;

    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return this.dateTime;
    }

    /**
     * @param dateTime
     *            the dateTime to set
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the longDescription
     */
    public String getLongDescription() {
        return this.longDescription;
    }

    /**
     * @return the modifiable list of tags
     */
    public List<Tag> getTags() {
        if (this.tags == null)
            this.tags = new ArrayList<Tag>();
        return this.tags;
    }

    /**
     * @param longDescription
     *            the longDescription to set
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * @return the restrict
     */
    public boolean isRestrict() {
        return this.restrict;
    }

    /**
     * @param restrict
     *            the restrict to set
     */
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return this.shortDescription;
    }

    /**
     * @param shortDescription
     *            the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

}
