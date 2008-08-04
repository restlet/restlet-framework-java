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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        final String getterName = "get"
                + attribute.substring(0, 1).toUpperCase()
                + attribute.substring(1);
        Method subMethod;
        try {
            subMethod = uriInfo.getClass().getMethod(getterName);
        } catch (final SecurityException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (final NoSuchMethodException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        try {
            return subMethod.invoke(uriInfo);
        } catch (final IllegalArgumentException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (final IllegalAccessException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (final InvocationTargetException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Context
    private UriInfo mainUriInfo;

    @GET
    @Produces("text/plain")
    public String get(@Context UriInfo uriInfo) {
        final int uriSize = uriInfo.getAncestorResourceURIs().size();
        final int resourcesSize = uriInfo.getAncestorResources().size();
        return uriSize + "\n" + resourcesSize;
    }

    @GET
    @Produces("text/plain")
    @Path("resourceClassNames")
    public String getResources(@Context UriInfo uriInfo) {
        final StringBuilder stb = new StringBuilder();
        final List<Object> resources = uriInfo.getAncestorResources();
        stb.append(resources.size());
        for (final Object resource : resources) {
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
        final AncestorTestService sub = new AncestorTestService();
        sub.mainUriInfo = this.mainUriInfo;
        return sub;
    }

    @GET
    @Produces("text/plain")
    @Path("uriInfo/{attribute}")
    public String getUriInfoAttribute(@Context UriInfo subUriInfo,
            @PathParam("attribute") String attribute) {
        final Object mainAttrValue = getAttribute(this.mainUriInfo, attribute);
        final Object subAttrValue = getAttribute(subUriInfo, attribute);
        return mainAttrValue + "\n" + subAttrValue;
    }

    @GET
    @Produces("text/plain")
    @Path("uris")
    public String getUris(@Context UriInfo uriInfo) {
        final StringBuilder stb = new StringBuilder();
        final List<String> uris = uriInfo.getAncestorResourceURIs();
        stb.append(uris.size());
        for (final String uri : uris) {
            stb.append('\n');
            stb.append(uri);
        }
        return stb.toString();
    }
}