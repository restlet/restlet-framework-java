/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.rest.ch7;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Resource for a user's list of bookmarks.
 * 
 * @author Jerome Louvel
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
