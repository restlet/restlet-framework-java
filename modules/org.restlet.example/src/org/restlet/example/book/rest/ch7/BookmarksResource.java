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

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Resource for a user's list of bookmarks.
 * 
 * @author Jerome Louvel
 */
public class BookmarksResource extends UserResource {

    @Override
    public void doInit() {
        super.doInit();
        getVariants().clear();
        if (getUser() != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
        }
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            int code = checkAuthorization();
            ReferenceList rl = new ReferenceList();

            // Copy the bookmark URIs into a reference list. Make sure that we
            // only expose public bookmarks if the client isn't the owner.
            for (Bookmark bookmark : getUser().getBookmarks()) {
                if (!bookmark.isRestricting() || (code == 1)) {
                    rl.add(bookmark.getUri());
                }
            }

            result = rl.getWebRepresentation();
        }

        return result;
    }

    @Override
    public Representation handle() {
        Representation result = null;

        // Make sure that the URI ends with a "/" without changing the query.
        // This is helpful when exposing the list of relative references of the
        // bookmarks.
        Reference ref = getRequest().getResourceRef();
        if (!ref.getPath().endsWith("/")) {
            ref.setPath(ref.getPath() + "/");
            redirectPermanent(ref);
        } else {
            result = super.handle();
        }

        return result;
    }

}
