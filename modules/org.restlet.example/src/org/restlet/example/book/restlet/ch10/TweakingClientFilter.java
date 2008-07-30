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

package org.restlet.example.book.restlet.ch10;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 */
public class TweakingClientFilter extends Filter {

    public TweakingClientFilter(Context context, Restlet next) {
        super(context, next);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        final String agent = request.getClientInfo().getAgent();

        if (agent.startsWith("Mozilla/5.0")) {
            // Adjust the client preferences
            final Preference<MediaType> preference = new Preference<MediaType>(
                    MediaType.TEXT_HTML);
            request.getClientInfo().getAcceptedMediaTypes().add(0, preference);
        }

        return super.beforeHandle(request, response);
    }

}
