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

package org.restlet.example.book.restlet.ch9.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.Feed;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user's feed.
 * 
 */
public class FeedResource extends BaseResource {

    /** The feed represented by this resource. */
    private Feed feed;

    /** The list of mails tagged by this feed. */
    private List<Mail> mails;

    /** The parent mailbox. */
    private Mailbox mailbox;

    public FeedResource(Context context, Request request, Response response) {
        super(context, request, response);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        mailbox = getDAOFactory().getMailboxDAO().getMailboxById(mailboxId);

        if (mailbox != null) {
            String feedId = (String) request.getAttributes().get("feedId");
            feed = getDAOFactory().getFeedDAO().getFeedById(feedId);

            if (feed != null) {
                mails = new ArrayList<Mail>();
                if (feed.getTags() != null) {
                    for (Mail mail : mailbox.getMails()) {
                        if (mail.getTags() != null
                                && mail.getTags().containsAll(feed.getTags())) {
                            mails.add(mail);
                        }
                    }
                }
                getVariants().add(new Variant(MediaType.TEXT_HTML));
                getVariants().add(new Variant(MediaType.APPLICATION_ATOM_XML));
            }
        }

    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        getDAOFactory().getMailboxDAO().deleteFeed(mailbox, feed);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", mailbox);
        dataModel.put("feed", feed);
        dataModel.put("mails", mails);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        Representation representation = null;
        MediaType mediaType = variant.getMediaType();
        if (MediaType.TEXT_HTML.equals(mediaType)) {
            StringBuilder builder = new StringBuilder();
            builder.append("<link ");
            builder.append("rel=\"alternate\" ");
            builder.append("type=\"application/rss+xml\" ");
            builder.append("href=\"");
            builder.append(getRequest().getResourceRef());
            builder.append("\" ");
            builder.append("title=\"Test feed\"");
            builder.append("/>");
            dataModel.put("feedHeader", builder.toString());

            representation = new TemplateRepresentation("feed.html",
                    getFmcConfiguration(), dataModel, variant.getMediaType());
        } else if (MediaType.APPLICATION_ATOM_XML.equals(mediaType)) {

        }

        return representation;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        feed.setNickname(form.getFirstValue("nickname"));
        if (form.getFirstValue("tags") != null) {
            feed.setTags(new ArrayList<String>(Arrays.asList(form
                    .getFirstValue("tags").split(" "))));
        } else {
            feed.setTags(null);
        }

        getDAOFactory().getMailboxDAO().updateFeed(mailbox, feed);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
