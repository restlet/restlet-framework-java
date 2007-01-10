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

package org.restlet.example.book.rest.ch7.resource;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.example.book.rest.ch7.Application;
import org.restlet.example.book.rest.ch7.domain.Bookmark;
import org.restlet.example.book.rest.ch7.domain.Tag;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Result;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * @author Jerome Louvel (contact@noelios.com)
 */
public class BookmarkResource extends Resource {

    private Bookmark bookmark;

    public BookmarkResource(Bookmark bookmark) {
        this.bookmark = bookmark;
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
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
    public Result delete() {
        if (this.bookmark != null) {
            Application.CONTAINER.delete(this.bookmark);
            Application.CONTAINER.commit();
            return new Result(Status.SUCCESS_OK);
        } else {
            return new Result(Status.SERVER_ERROR_INTERNAL);
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
            sb.append("User:  ").append(this.bookmark.getUser().getName())
                    .append('\n');
            sb.append("URI:   ").append(this.bookmark.getUri()).append('\n');
            sb.append("Short: ").append(this.bookmark.getShortDescription())
                    .append('\n');
            sb.append("Long:  ").append(this.bookmark.getLongDescription())
                    .append('\n');
            sb.append("Date:  ").append(this.bookmark.getDateTime()).append(
                    '\n');
            sb.append("Restrict:  ").append(
                    Boolean.toString(this.bookmark.isRestrict())).append('\n');
            sb.append("Tags:  ");
            for (Tag tag : this.bookmark.getTags()) {
                sb.append(tag.getName()).append(' ');
            }
            sb.append('\n');

            result = new StringRepresentation(sb);
        }

        return result;
    }

    @Override
    public Result put(Representation entity) {
        return null;
    }

}
