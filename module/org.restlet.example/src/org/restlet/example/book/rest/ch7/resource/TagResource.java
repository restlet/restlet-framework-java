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

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.example.book.rest.ch7.Application;
import org.restlet.example.book.rest.ch7.domain.Tag;
import org.restlet.example.book.rest.ch7.domain.User;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;

/**
 * Resource for a user's usage of a tag.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TagResource extends ApplicationResource {

    public static Tag findTag(ObjectContainer container, final String tagName) {
        Tag result = null;

        if (tagName != null) {
            // Create the query predicate
            Predicate<Tag> predicate = new Predicate<Tag>() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean match(Tag candidate) {
                    return tagName.equals(candidate.getName());
                }
            };

            // Query the database and get the first result
            List<Tag> tags = container.query(predicate);
            if ((tags != null) && (tags.size() > 0)) {
                result = tags.get(0);
            }
        }

        return result;
    }

    private User user;

    private Tag tag;

    public TagResource(Application application, User user, Tag tag) {
        super(application);
        this.user = user;
        this.tag = tag;
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
    public Response delete() {
        if (this.tag != null) {
            getContainer().delete(this.tag);
            getContainer().commit();
            return new Response(Status.SUCCESS_OK);
        } else {
            return new Response(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            // Creates a text representation
            StringBuilder sb = new StringBuilder();
            sb.append("----------------\n");
            sb.append("User tag details\n");
            sb.append("----------------\n\n");
            sb.append("User name: ").append(this.user.getName()).append('\n');
            sb.append("Tag name:  ").append(this.tag.getName()).append('\n');
            result = new StringRepresentation(sb);
        }

        return result;
    }

}
