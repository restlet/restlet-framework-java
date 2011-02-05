/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.example.book.rest.ch7;

import java.util.Date;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Resource for a user's bookmark.
 * 
 * @author Jerome Louvel
 */
public class BookmarkResource extends UserResource {

    private Bookmark bookmark;

    private String uri;

    @Override
    public Representation delete() throws ResourceException {
        if (isModifiable()) {
            if ((this.bookmark != null) && (checkAuthorization() == 1)) {
                // Delete the bookmark
                getUser().getBookmarks().remove(this.bookmark);
                getContainer().delete(this.bookmark);
                getContainer().store(getUser());
                getContainer().commit();
                setStatus(Status.SUCCESS_OK);
            } else {
                // Intentionally hide the bookmark existence
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        } else {
            return super.delete();
        }

        return null;
    }

    @Override
    public void doInit() {
        super.doInit();

        if (getUser() != null) {
            this.uri = (String) getRequestAttributes().get("URI");
            this.bookmark = getUser().getBookmark(this.uri);

            if (this.bookmark != null) {
                if ((checkAuthorization() != 1) && this.bookmark.isRestricting()) {
                    // Intentionally hide the bookmark existence
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {
                // Bookmark not found, remove the variant added by the super
                // class (UserResource).
                getVariants().clear();
            }
        } else {
            // User not found
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            // Creates a text representation
            final StringBuilder sb = new StringBuilder();
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
                    Boolean.toString(this.bookmark.isRestricting())).append('\n');
            result = new StringRepresentation(sb);
        }

        return result;
    }

    @Override
    public Representation put(Representation entity) throws ResourceException {
        if (checkAuthorization() == 1) {
            if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
                    true)) {
                // Parse the entity as a web form
                final Form form = new Form(entity);

                // If the bookmark doesn't exist, create it
                if (this.bookmark == null) {
                    this.bookmark = new Bookmark(getUser(), this.uri);
                    getUser().getBookmarks().add(this.bookmark);
                    setStatus(Status.SUCCESS_CREATED);
                } else {
                    setStatus(Status.SUCCESS_NO_CONTENT);
                }

                // Update the bookmark
                this.bookmark.setShortDescription(form
                        .getFirstValue("bookmark[short_description]"));
                this.bookmark.setLongDescription(form
                        .getFirstValue("bookmark[long_description]"));
                this.bookmark.setDateTime(new Date());
                this.bookmark.setRestricting(new Boolean(form
                        .getFirstValue("bookmark[restrict]")));

                // Commit the changes
                getContainer().store(this.bookmark);
                getContainer().store(getUser());
                getContainer().commit();
            }
        } else {
            // Intentionally hide the bookmark existence
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }

        return null;
    }

}
