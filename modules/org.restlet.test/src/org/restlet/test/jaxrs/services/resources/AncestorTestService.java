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
package org.restlet.test.jaxrs.services.resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

/**
 * @author Stephan Koops
 * @see org.restlet.test.jaxrs.services.tests.AncestorTest
 * @see UriInfo#getAncestorResources()
 * @see UriInfo#getAncestorResourceURIs()
 */
@Path("ancestorTest")
public class AncestorTestService {

    /**
     * @param uriInfo
     * @param attribute
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static Object getAttribute(UriInfo uriInfo, String attribute) {
        String getterName = "get" + attribute.substring(0, 1).toUpperCase()
                + attribute.substring(1);
        Method subMethod;
        try {
            subMethod = uriInfo.getClass().getMethod(getterName);
        } catch (SecurityException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (NoSuchMethodException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        try {
            return subMethod.invoke(uriInfo);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (IllegalAccessException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Context
    private UriInfo mainUriInfo;

    @GET
    @Produces("text/plain")
    public String get(@Context UriInfo uriInfo) {
        int uriSize = uriInfo.getAncestorResourceURIs().size();
        int resourcesSize = uriInfo.getAncestorResources().size();
        return uriSize + "\n" + resourcesSize;
    }

    @GET
    @Produces("text/plain")
    @Path("resourceClassNames")
    public String getResources(@Context UriInfo uriInfo) {
        StringBuilder stb = new StringBuilder();
        List<Object> resources = uriInfo.getAncestorResources();
        stb.append(resources.size());
        for (Object resource : resources) {
            stb.append('\n');
            stb.append(resource.getClass().getName());
        }
        return stb.toString();
    }

    @Path("sameSub")
    public AncestorTestService getSameSub() {
        return getSub();
    }

    @Path("sub")
    public AncestorTestService getSub() {
        AncestorTestService sub = new AncestorTestService();
        sub.mainUriInfo = this.mainUriInfo;
        return sub;
    }

    @GET
    @Produces("text/plain")
    @Path("uriInfo/{attribute}")
    public String getUriInfoAttribute(@Context UriInfo subUriInfo,
            @PathParam("attribute") String attribute) {
        Object mainAttrValue = getAttribute(mainUriInfo, attribute);
        Object subAttrValue = getAttribute(subUriInfo, attribute);
        return mainAttrValue + "\n" + subAttrValue;
    }

    @GET
    @Produces("text/plain")
    @Path("uris")
    public String getUris(@Context UriInfo uriInfo) {
        StringBuilder stb = new StringBuilder();
        List<String> uris = uriInfo.getAncestorResourceURIs();
        stb.append(uris.size());
        for (String uri : uris) {
            stb.append('\n');
            stb.append(uri);
        }
        return stb.toString();
    }
}