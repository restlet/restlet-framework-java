/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.example.book.restlet.ch9.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Feed seen as a list of mails having their tags in a defined set.
 * 
 */
public class Feed extends BaseObject {
    /** List of mails of the feed. */
    private List<Mail> mails;

    /** Set of tags of the feed. */
    private List<String> tags;

    public Feed() {
        super();
        this.mails = new ArrayList<Mail>();
        this.tags = new ArrayList<String>();
    }

    public List<Mail> getMails() {
        return mails;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
