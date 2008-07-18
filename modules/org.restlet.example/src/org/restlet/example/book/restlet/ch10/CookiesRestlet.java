package org.restlet.example.book.restlet.ch10;

import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;

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
