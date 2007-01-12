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
import org.restlet.data.ReferenceList;
import org.restlet.example.book.rest.ch7.Application;
import org.restlet.example.book.rest.ch7.domain.Bookmark;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Resource for a user's list of bookmarks.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class BookmarksResource extends UserResource {

    public BookmarksResource(Application application, String userName,
            String login, String password) {
        super(application, userName, login, password);
        
        
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            ReferenceList rl = new ReferenceList();

            // Copy the bookmark URIs into a reference list
            for (Bookmark bookmark : getUser().getBookmarks()) {
                rl.add(bookmark.getUri());
            }

            result = rl.getWebRepresentation();
        }

        return result;
    }

}
