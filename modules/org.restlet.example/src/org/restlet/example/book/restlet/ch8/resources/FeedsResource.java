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

package org.restlet.example.book.restlet.ch8.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Feed;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a list of feeds.
 */
public class FeedsResource extends BaseResource {

    /** The parent mailbox. */
    private Mailbox mailbox;

    /** The list of feeds. */
    private List<Feed> feeds;

    public FeedsResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            // Get the parent mailbox thanks to its ID taken from the resource's
            // URI.
            final String mailboxId = Reference.decode((String) request
                    .getAttributes().get("mailboxId"));
            this.mailbox = getObjectsFacade().getMailboxById(mailboxId);

            if (this.mailbox != null) {
                this.feeds = this.mailbox.getFeeds();
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        } else {
            // Anonymous access.
            setModifiable(false);
        }
    }

    /**
     * Accept the representation of a new feed, and create it.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        Feed feed = new Feed();
        feed.setNickname(form.getFirstValue("nickname"));
        feed.setTags(Arrays.asList(form.getFirstValue("tags").split(" ")));
        feed = getObjectsFacade().createFeed(this.mailbox, feed);

        getResponse().redirectSeeOther(
                getChildReference(getRequest().getResourceRef(), feed.getId()));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", this.mailbox);
        dataModel.put("feeds", this.feeds);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("feeds.html", dataModel);
    }

}
