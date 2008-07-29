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

import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 * @author Stephan Koops
 * @see org.restlet.test.jaxrs.services.tests.AncestorTest
 * @see UriInfo#getAncestorResources()
 * @see UriInfo#getAncestorResourceURIs()
 */
@Path("formTest")
public class FormTestResource {

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("formOnly")
    @POST
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

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("paramOnly")
    @POST
    public Object paramOnly(@FormParam("a") String a,
            @FormParam("c") String c) {
        String result = "a -> " + a + "\n";
        if(c != null) {
            result += "c -> " + c + "\n";
        }
        return result;
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("formAndParam")
    @POST
    public Object formAndParam(final MultivaluedMap<String, String> form,
            @FormParam("a") final String a) {
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

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("paramAndForm")
    @POST
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
}