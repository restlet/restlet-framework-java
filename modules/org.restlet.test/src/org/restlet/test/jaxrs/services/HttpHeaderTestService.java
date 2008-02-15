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

package org.restlet.test.jaxrs.services;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Service to test, if the headers are read correct; this class is not used yet.
 * @author Stephan Koops
 * 
 */
@Path("/skjflsfh")
public class HttpHeaderTestService {
    
    public static final String TEST_HEADER_NAME = "testHeader";
    
    /**
     * @return
     */
    @GET
    @Path("HeaderParam")
    @ProduceMime("text/plain")
    public String getHeaderParam(@HeaderParam(TEST_HEADER_NAME) String testHeaderValue) {
        return testHeaderValue;
    }
    
    @GET
    @Path("accMediaTypes")
    @ProduceMime("text/plain")
    public String getAccMediaTypes(@Context HttpHeaders headers) {
        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        return mediaTypes.toString();
    } 

    @GET
    @Path("cookies/{cookieName}")
    @ProduceMime("text/plain")
    public String getMediaTypes(@Context HttpHeaders headers, @PathParam("cookieName") String cookieName) {
        Map<String, Cookie> cookies = headers.getCookies();
        try
        {
            cookies.put("notAllowed", new Cookie("notAllowed", "value"));
            throw new WebApplicationException(Response.serverError().entity("could add cookie notAllowed").build());
        }
        catch(UnsupportedOperationException uoe)
        {
            // not allowed
        }
        try
        {
            cookies.put("xyz", new Cookie("notAllowed", "value"));
            throw new WebApplicationException(Response.serverError().entity("could add xyz").build());
        }
        catch(UnsupportedOperationException uoe)
        {
            // not allowed
        }
        Cookie cookie = cookies.get(cookieName);
        if(cookie == null)
            return null;
        return cookie.toString();
    }

    @POST
    @Path("language")
    @ProduceMime("text/plain")
    public String getLanguage(@Context HttpHeaders headers) {
        return headers.getLanguage();
    } 

    @GET
    @Path("header/{headername}")
    @ProduceMime("text/plain")
    public String getHeader(@Context HttpHeaders headers, @PathParam("headername") String headername) {
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        String headerValue = requestHeaders.getFirst(headername);
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
    @ProduceMime("text/plain")
    public Object getHeader2(@HeaderParam("host") String hostLower,
                             @HeaderParam("HOST") String hostUpper, 
                             @HeaderParam("Host") String hostMixed)
    {
        if(hostLower.equals(hostUpper) && hostLower.equals(hostMixed))
            return hostMixed;
        String hosts = "mixed: "+hostMixed+"\nupper: "+hostUpper+"\n lower: "+hostLower;
        return Response.serverError().entity(hosts).build();
    }

    @GET
    @Path("headerWithDefault")
    @ProduceMime("text/plain")
    public String getHeaderWithDefault(@HeaderParam(TEST_HEADER_NAME) @DefaultValue("default") String testHeader)
    {
        return testHeader;
    }
}