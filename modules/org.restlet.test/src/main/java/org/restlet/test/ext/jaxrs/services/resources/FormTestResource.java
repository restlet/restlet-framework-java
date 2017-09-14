/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.services.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 * @author Stephan Koops
 * @see org.restlet.test.ext.jaxrs.services.tests.MatchedTest
 * @see UriInfo#getMatchedResources()
 * @see UriInfo#getMatchedURIs()
 */
@Path("formTest")
public class FormTestResource {

    @Path("formOnly")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Object formOnly(final MultivaluedMap<String, String> form) {
        return new StreamingOutput() {
            public void write(OutputStream out) throws IOException {
                for (final String key : new TreeSet<String>(form.keySet())) {
                    for (final String value : form.get(key)) {
                        out.write(key.getBytes());
                        out.write(" -> ".getBytes());
                        out.write(value.getBytes());
                        out.write('\n');
                    }
                }
            }
        };
    }

    @Path("paramOnly")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Object paramOnly(@FormParam("a")
    String a, @FormParam("c")
    String c) {
        String result = "a -> " + a + "\n";
        if (c != null) {
            result += "c -> " + c + "\n";
        }
        return result;
    }

    @Path("formAndParam")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Object formAndParam(final MultivaluedMap<String, String> form,
            @FormParam("a")
            final String a) {
        return new StreamingOutput() {
            public void write(OutputStream out) throws IOException {
                out.write("a -> ".getBytes());
                out.write(a.getBytes());
                out.write('\n');
                for (final String key : new TreeSet<String>(form.keySet())) {
                    if (!key.equals("a")) {
                        for (final String value : form.get(key)) {
                            out.write(key.getBytes());
                            out.write(" -> ".getBytes());
                            out.write(value.getBytes());
                            out.write('\n');
                        }
                    }
                }
            }
        };
    }

    @Path("paramAndForm")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Object paramAndForm(@FormParam("a")
    final String a, final MultivaluedMap<String, String> form) {
        return new StreamingOutput() {
            public void write(OutputStream out) throws IOException {
                out.write("a -> ".getBytes());
                out.write(a.getBytes());
                out.write('\n');
                for (final String key : new TreeSet<String>(form.keySet())) {
                    if (!key.equals("a")) {
                        for (final String value : form.get(key)) {
                            out.write(key.getBytes());
                            out.write(" -> ".getBytes());
                            out.write(value.getBytes());
                            out.write('\n');
                        }
                    }
                }
            }
        };
    }

    @Path("checkUnmodifiable")
    @POST
    @Produces("text/plain")
    public Object checkUnmodifiable(@FormParam("a")
    List<String> as) {
        try {
            as.clear();
            throw new WebApplicationException(Response.serverError()
                    .entity("the List must be unmodifiable").build());
        } catch (UnsupportedOperationException uoe) {
            return null;
        }
    }
}
