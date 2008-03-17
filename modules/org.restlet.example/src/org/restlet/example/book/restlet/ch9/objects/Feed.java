package org.restlet.example.book.restlet.ch9.objects;

import java.util.List;
import java.util.Set;

/**
 * Feed seen as a list of mails having their tags in a defined set.
 */
public class Feed {
    /** List of mails of the feed. */
    private List<Mail> mails;

    /** Set of tags of the feed. */
    private Set<String> tags;

    public List<Mail> getMails() {
        return mails;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}
