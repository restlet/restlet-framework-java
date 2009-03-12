/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.example.book.restlet.ch10;

import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

/**
 * 
 */
public class CookiesRestlet extends Restlet {

    @Override
    public void handle(Request request, Response response) {

        for (final Preference<MediaType> preference : request.getClientInfo()
                .getAcceptedMediaTypes()) {
            System.out.println(preference.getMetadata());
            System.out.println(preference.getQuality());
        }

        final StringBuilder resp = new StringBuilder();

        // Get cookies sent by the client
        if (!request.getCookies().isEmpty()) {
            resp.append("Cookies sent by client: ");
            for (final Cookie cookie : request.getCookies()) {
                resp.append("[").append(cookie.getName());
                resp.append("/");
                resp.append(cookie.getValue()).append("]");
            }
        } else {
            resp.append("No Cookies sent by client.");
            // Create a new cookie with the IP address of the client.
            final CookieSetting cookieSetting = new CookieSetting(0, "IP",
                    request.getClientInfo().getAddress());
            // Ask client to detroy the cookie at the end of the session
            cookieSetting.setMaxAge(-1);

            // Add the cookie to the list of cookies of the response
            response.getCookieSettings().add(cookieSetting);
        }
        response.setEntity(new StringRepresentation(resp.toString()));
        response.setStatus(Status.SUCCESS_OK);
    }

}
