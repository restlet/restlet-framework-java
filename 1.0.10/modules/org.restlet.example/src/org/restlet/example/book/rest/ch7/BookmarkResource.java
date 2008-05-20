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

package org.restlet.example.book.rest.ch7;

import java.util.Date;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Resource for a user's bookmark.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class BookmarkResource extends UserResource {

    private Bookmark bookmark;

    private String uri;

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public BookmarkResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getUser() != null) {
            this.uri = (String) request.getAttributes().get("URI");
            this.bookmark = getUser().getBookmark(this.uri);

            if (this.bookmark != null) {
                if ((checkAuthorization() != 1) && this.bookmark.isRestrict()) {
                    // Intentionnally hide the bookmark existence
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {
                // Bookmark not found, remove the variant added by the super
                // class (UserResource).
                getVariants().clear();
            }
        } else {
            // User not found
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
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
    public void delete() {
        if ((this.bookmark != null) && (checkAuthorization() == 1)) {
            // Delete the bookmark
            getUser().getBookmarks().remove(this.bookmark);
            getContainer().delete(this.bookmark);
            getContainer().set(getUser());
            getContainer().commit();
            getResponse().setStatus(Status.SUCCESS_OK);
        } else {
            // Intentionnally hide the bookmark existence
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            // Creates a text representation
            StringBuilder sb = new StringBuilder();
            sb.append("----------------\n");
            sb.append("Bookmark details\n");
            sb.append("----------------\n\n");
            sb.append("User:     ").append(this.bookmark.getUser().getName())
                    .append('\n');
            sb.append("URI:      ").append(this.bookmark.getUri()).append('\n');
            sb.append("Short:    ").append(this.bookmark.getShortDescription())
                    .append('\n');
            sb.append("Long:     ").append(this.bookmark.getLongDescription())
                    .append('\n');
            sb.append("Date:     ").append(this.bookmark.getDateTime()).append(
                    '\n');
            sb.append("Restrict: ").append(
                    Boolean.toString(this.bookmark.isRestrict())).append('\n');
            result = new StringRepresentation(sb);
        }

        return result;
    }

    @Override
    public void put(Representation entity) {
        if (checkAuthorization() == 1) {
            if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM, true)) {
                // Parse the entity as a web form
                Form form = new Form(entity);

                // If the bookmark doesn't exist, create it
                if (this.bookmark == null) {
                    this.bookmark = new Bookmark(getUser(), this.uri);
                    getUser().getBookmarks().add(this.bookmark);
                    getResponse().setStatus(Status.SUCCESS_CREATED);
                } else {
                    getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                }

                // Update the bookmark
                this.bookmark.setShortDescription(form
                        .getFirstValue("bookmark[short_description]"));
                this.bookmark.setLongDescription(form
                        .getFirstValue("bookmark[long_description]"));
                this.bookmark.setDateTime(new Date());
                this.bookmark.setRestrict(new Boolean(form
                        .getFirstValue("bookmark[restrict]")));

                // Commit the changes
                getContainer().set(this.bookmark);
                getContainer().set(getUser());
                getContainer().commit();
            }
        } else {
            // Intentionnally hide the bookmark existence
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

}
