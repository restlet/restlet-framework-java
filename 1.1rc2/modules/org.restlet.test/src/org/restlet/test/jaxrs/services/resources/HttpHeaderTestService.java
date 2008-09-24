/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.jaxrs.services.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.restlet.test.jaxrs.services.tests.HttpHeaderTest;

/**
 * Service to test, if the headers are read correct.
 * 
 * @author Stephan Koops
 * @see HttpHeaderTest
 */
@Path("/skjflsfh")
public class HttpHeaderTestService {

    public static final String TEST_HEADER_NAME = "testHeader";

    @GET
    @Path("accMediaTypes")
    @Produces("text/plain")
    public String getAccMediaTypes(@Context HttpHeaders headers) {
        final List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        return mediaTypes.toString();
    }

    @GET
    @Path("cookies/{cookieName}")
    @Produces("text/plain")
    public String getCookies(@Context HttpHeaders headers,
            @PathParam("cookieName") String cookieName) {
        final Map<String, Cookie> cookies = headers.getCookies();
        try {
            cookies.put("notAllowed", new Cookie("notAllowed", "value"));
            throw new WebApplicationException(Response.serverError().entity(
                    "could add cookie notAllowed").build());
        } catch (final UnsupportedOperationException uoe) {
            // not allowed
        }
        try {
            cookies.put("xyz", new Cookie("notAllowed", "value"));
            throw new WebApplicationException(Response.serverError().entity(
                    "could add xyz").build());
        } catch (final UnsupportedOperationException uoe) {
            // not allowed
        }
        final Cookie cookie = cookies.get(cookieName);
        if (cookie == null) {
            return null;
        }
        return cookie.toString();
    }

    @GET
    @Path("header/{headername}")
    @Produces("text/plain")
    public String getHeader(@Context HttpHeaders headers,
            @PathParam("headername") String headername) {
        final MultivaluedMap<String, String> requestHeaders = headers
                .getRequestHeaders();
        final String headerValue = requestHeaders.getFirst(headername);
        return headerValue;
    }

    /**
     * @param hostLower
     * @param hostUpper
     * @param hostMixed
     * @return returns internal server error if the host variables does not have
     *         all the same value.
     */
    @GET
    @Path("header2")
    @Produces("text/plain")
    public Object getHeader2(@HeaderParam("host") String hostLower,
            @HeaderParam("HOST") String hostUpper,
            @HeaderParam("Host") String hostMixed) {
        if (hostLower.equals(hostUpper) && hostLower.equals(hostMixed)) {
            return hostMixed;
        }
        final String hosts = "mixed: " + hostMixed + "\nupper: " + hostUpper
                + "\n lower: " + hostLower;
        return Response.serverError().entity(hosts).build();
    }

    @GET
    @Path("HeaderParam")
    @Produces("text/plain")
    public String getHeaderParam(
            @HeaderParam(TEST_HEADER_NAME) String testHeaderValue) {
        return testHeaderValue;
    }

    @GET
    @Path("headerWithDefault")
    @Produces("text/plain")
    public String getHeaderWithDefault(
            @HeaderParam(TEST_HEADER_NAME) @DefaultValue("default") String testHeader) {
        return testHeader;
    }

    @POST
    @Path("language")
    @Produces( { "text/plain", "text/html" })
    public String getLanguage(@Context HttpHeaders headers) {
        return headers.getLanguage().toString();
    }

    @GET
    @Produces("text/plain")
    public String getPlain() {
        return "media type text/plain is supported\n";
    }

    @GET
    @Produces( { "text/xml", MediaType.APPLICATION_XML })
    public String getXML() {
        return "<text>the media types text/xml and application/xml are supported</text>\n";
    }
}