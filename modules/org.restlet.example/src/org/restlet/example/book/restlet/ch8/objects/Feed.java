/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Feed seen as a list of mails having their tags in a defined set.
 */
public class Feed extends BaseObject {

    /** List of mails of the feed. */
    private List<Mail> mails;

    /** Nickname of the feed. */
    private String nickname;

    /** Set of tags of the feed. */
    private List<String> tags;

    public Feed() {
        super();
        this.mails = new ArrayList<Mail>();
        this.tags = new ArrayList<String>();
    }

    public List<Mail> getMails() {
        return this.mails;
    }

    public String getNickname() {
        return this.nickname;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
