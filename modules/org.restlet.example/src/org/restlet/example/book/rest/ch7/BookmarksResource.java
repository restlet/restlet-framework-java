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

package org.restlet.example.book.rest.ch7;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user's list of bookmarks.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class BookmarksResource extends UserResource {

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
    public BookmarksResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().clear();
        if (getUser() != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
        }
    }

    @Override
    public void handleGet() {
        // Make sure that the Uri ends with a "/" without changing the query.
        // This is helpful when exposing the list of relative references of the
        // bookmarks.
        final Reference ref = getRequest().getResourceRef();
        if (!ref.getPath().endsWith("/")) {
            ref.setPath(ref.getPath() + "/");
            getResponse().redirectPermanent(ref);
        } else {
            super.handleGet();
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            final int code = checkAuthorization();
            final ReferenceList rl = new ReferenceList();

            // Copy the bookmark URIs into a reference list. Make sure that we
            // only expose public bookmarks if the client isn't the owner.
            for (final Bookmark bookmark : getUser().getBookmarks()) {
                if (!bookmark.isRestrict() || (code == 1)) {
                    rl.add(bookmark.getUri());
                }
            }

            result = rl.getWebRepresentation();
        }

        return result;
    }

}
