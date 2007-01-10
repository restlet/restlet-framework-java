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

package org.restlet.example.book.rest.ch7.handler;

import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.rest.ch7.domain.Bookmark;
import org.restlet.example.book.rest.ch7.domain.User;
import org.restlet.example.book.rest.ch7.resource.BookmarkResource;
import org.restlet.example.book.rest.ch7.resource.UserResource;
import org.restlet.resource.Resource;

/**
 * Handler of bookmark resources.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class BookmarkHandler extends Handler {

    @Override
    public Resource findTarget(final Request request, Response response) {
        Resource result = null;

        // Find the user owning the bookmark
        String userName = (String) request.getAttributes().get("username");
        User user = UserResource.findUser(userName);

        if (user != null) {
            // Find the bookmark
            String uri = (String) request.getAttributes().get("URI");
            Bookmark bookmark = user.getBookmark(uri);
            result = (bookmark == null) ? null : new BookmarkResource(bookmark);
        }

        return result;
    }
}
