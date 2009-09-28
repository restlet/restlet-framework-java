/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch8.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Feed;
import org.restlet.example.book.restlet.ch8.objects.Mail;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.ext.atom.Category;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Generator;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Person;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.atom.Text;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.noelios.restlet.Engine;

/**
 * Resource for a user's feed.
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

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            // Get the feed and its parent mailbox thanks to their IDs taken
            // from the resource's URI.
            final String mailboxId = Reference.decode((String) request
                    .getAttributes().get("mailboxId"));
            this.mailbox = getObjectsFacade().getMailboxById(mailboxId);

            if (this.mailbox != null) {
                final String feedId = (String) request.getAttributes().get(
                        "feedId");
                this.feed = getObjectsFacade().getFeedById(feedId);

                if (this.feed != null) {
                    // Look for the list of tagged mails.
                    this.mails = new ArrayList<Mail>();
                    if (this.feed.getTags() != null) {
                        for (final Mail mail : this.mailbox.getMails()) {
                            if ((mail.getTags() != null)
                                    && mail.getTags().containsAll(
                                            this.feed.getTags())) {
                                this.mails.add(mail);
                            }
                        }
                    }

                    // This resource supports two kinds of representations.
                    getVariants().add(
                            new Variant(MediaType.APPLICATION_ATOM_XML));
                    getVariants().add(new Variant(MediaType.TEXT_HTML));
                }
            }
        } else {
            // Anonymous access.
            setModifiable(false);
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

    /**
     * Remove this resource.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        getObjectsFacade().deleteFeed(this.mailbox, this.feed);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Generate the HTML and ATOM representations of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        final MediaType mediaType = variant.getMediaType();

        if (MediaType.TEXT_HTML.equals(mediaType)) {
            final Map<String, Object> dataModel = new TreeMap<String, Object>();
            dataModel.put("currentUser", getCurrentUser());
            dataModel.put("mailbox", this.mailbox);
            dataModel.put("feed", this.feed);
            dataModel.put("mails", this.mails);
            dataModel.put("resourceRef", getRequest().getResourceRef());
            dataModel.put("rootRef", getRequest().getRootRef());

            final StringBuilder builder = new StringBuilder();
            builder.append("<link ");
            builder.append("rel=\"alternate\" ");
            builder.append("type=\"application/atom+xml\" ");
            builder.append("href=\"");
            builder.append(getRequest().getResourceRef().toString(false, false)
                    + "?media=xml");
            builder.append("\" ");
            builder.append("title=\"feed\"");
            builder.append("/>");
            dataModel.put("feedHeaderContent", builder.toString());

            representation = getHTMLTemplateRepresentation("feed.html",
                    dataModel);
        } else if (MediaType.APPLICATION_ATOM_XML.equals(mediaType)) {
            final org.restlet.ext.atom.Feed atomFeed = new org.restlet.ext.atom.Feed();

            /** The author of the feed. */
            final Person currentAuthor = new Person();
            // currentAuthor.setEmail(email); Use the Uri instead
            currentAuthor.setName(this.mailbox.getSenderName());
            currentAuthor.setUri(new Reference(getRequest().getRootRef()
                    .toString()
                    + "/mailboxes/"
                    + Reference.encode(this.mailbox.getId(),
                            CharacterSet.US_ASCII)));
            atomFeed.getAuthors().add(currentAuthor);

            /** The categories associated with the feed. */
            final StringBuilder titleBuilder = new StringBuilder("Feed");
            for (final String tag : this.feed.getTags()) {
                final Category category = new Category();
                category.setLabel(tag);
                category.setTerm(tag);
                atomFeed.getCategories().add(category);

                titleBuilder.append(" ").append(tag);
            }

            /** The agent used to generate a feed. */
            final Generator generator = new Generator();
            generator.setName("Atom extension for Restlet.");
            generator.setUri(new Reference("http://restlet.org"));
            generator.setVersion(Engine.VERSION);
            atomFeed.setGenerator(generator);

            /** Image that provides iconic visual identification for a feed. */
            // Reference icon;
            /** Permanent, universally unique identifier for the feed. */
            atomFeed.setId(getRequest().getRootRef().toString() + "/mailboxes/"
                    + this.mailbox.getId() + "/feeds/" + this.feed.getId());

            /** The references from the feed to Web resources. */
            Link link = new Link();
            link.setHref(new Reference(getRequest().getRootRef().toString()
                    + "/mailboxes/" + this.mailbox.getId() + "/feeds/"
                    + this.feed.getId()));
            link.setRel(Relation.ALTERNATE);
            link.setTitle(titleBuilder.toString());
            link.setType(MediaType.TEXT_HTML);
            atomFeed.getLinks().add(link);

            link = new Link();
            link.setHref(getRequest().getResourceRef());
            link.setRel(Relation.SELF);
            link.setTitle(titleBuilder.toString());
            link.setType(mediaType);
            atomFeed.getLinks().add(link);

            /** Image that provides visual identification for a feed. */
            // Reference logo;
            /** Information about rights held in and over an entry. */
            // Text rights;
            /** Short summary, abstract, or excerpt of an entry. */
            // Text subtitle;
            /** The human-readable title for the entry. */
            atomFeed.setTitle(new Text(MediaType.TEXT_PLAIN, titleBuilder
                    .toString()));

            /**
             * Most recent moment when the entry was modified in a significant
             * way.
             */
            atomFeed.setUpdated(new Date());

            /**
             * Individual entries, acting as a components for associated
             * metadata and data.
             */
            for (final Mail mail : this.mails) {
                final Entry entry = new Entry();

                final Person author = new Person();
                // author.setEmail(email); Use the Uri instead
                author.setName(mail.getSender().getName());
                author.setUri(new Reference(mail.getSender().getMailAddress()));
                entry.getAuthors().add(author);

                /** The categories associated with the entry. */
                final StringBuilder entryTitleBuilder = new StringBuilder(
                        "Feed");
                for (final String tag : mail.getTags()) {
                    final Category category = new Category();
                    category.setLabel(tag);
                    category.setTerm(tag);
                    entry.getCategories().add(category);

                    entryTitleBuilder.append(" ").append(tag);
                }

                /** Contains or links to the content of the entry. */
                final Content content = new Content();
                content.setInlineContent(new StringRepresentation(mail
                        .getMessage(), MediaType.TEXT_PLAIN));
                entry.setContent(content);

                /** The contributors to the entry. */
                for (final Contact recipient : mail.getRecipients()) {
                    final Person contributor = new Person();
                    // contributor.setEmail(email); Use the Uri instead
                    contributor.setName(recipient.getName());
                    contributor
                            .setUri(new Reference(recipient.getMailAddress()));
                    entry.getContributors().add(contributor);
                }

                /** Permanent, universally unique identifier for the entry. */
                entry.setId(getRequest().getRootRef().toString()
                        + "/mailboxes/" + this.mailbox.getId() + "/mails"
                        + mail.getId());

                /** The references from the entry to Web resources. */
                Link entryLink = new Link();
                entryLink.setHref(new Reference(getRequest().getRootRef()
                        .toString()
                        + "/mailboxes/"
                        + this.mailbox.getId()
                        + "/mails"
                        + mail.getId()));
                entryLink.setRel(Relation.ALTERNATE);
                entryLink.setTitle(entryTitleBuilder.toString());
                entryLink.setType(MediaType.TEXT_HTML);
                entry.getLinks().add(entryLink);

                entryLink = new Link();
                entryLink.setHref(new Reference(getRequest().getRootRef()
                        .toString()
                        + "/mailboxes/"
                        + this.mailbox.getId()
                        + "/mails"
                        + mail.getId()));
                entryLink.setRel(Relation.SELF);
                entryLink.setTitle(entryTitleBuilder.toString());
                entryLink.setType(mediaType);
                entry.getLinks().add(entryLink);
                /**
                 * Moment associated with an event early in the life cycle of
                 * the entry.
                 */
                entry.setPublished(mail.getSendingDate());

                /** Information about rights held in and over an entry. */
                // Text rights;
                /** Short summary, abstract, or excerpt of the entry. */
                if (mail.getMessage().length() > 100) {
                    entry
                            .setSummary(mail.getMessage().substring(0, 97)
                                    + "...");
                } else {
                    entry.setSummary(mail.getMessage());
                }

                /** The human-readable title for the entry. */
                entry
                        .setTitle(new Text(MediaType.TEXT_PLAIN, mail
                                .getSubject()));

                /**
                 * Most recent moment when the entry was modified in a
                 * significant way.
                 */
                entry.setUpdated(mail.getSendingDate());

                atomFeed.getEntries().add(entry);

            }
            representation = atomFeed;
            representation.setMediaType(mediaType);

        }

        return representation;
    }

    /**
     * Update the underlying feed according to the given representation.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        this.feed.setNickname(form.getFirstValue("nickname"));
        if (form.getFirstValue("tags") != null) {
            this.feed.setTags(new ArrayList<String>(Arrays.asList(form
                    .getFirstValue("tags").split(" "))));
        } else {
            this.feed.setTags(null);
        }

        getObjectsFacade().updateFeed(this.mailbox, this.feed);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
