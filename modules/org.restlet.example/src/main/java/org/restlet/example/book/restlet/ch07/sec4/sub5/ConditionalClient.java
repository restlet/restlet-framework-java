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

package org.restlet.example.book.restlet.ch07.sec4.sub5;

import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Client issuing conditional requests.
 */
public class ConditionalClient {

    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource("http://localhost:8111/");

        // Get a representation
        Representation rep = resource.get();
        System.out.println(resource.getStatus());

        // Get an updated representation, if modified
        resource.getConditions().setModifiedSince(rep.getModificationDate());
        rep = resource.get();
        System.out.println(resource.getStatus());

        // Get an updated representation, if tag changed
        resource.getConditions().setModifiedSince(null);
        resource.getConditions().getNoneMatch().add(new Tag("xyz123"));
        rep = resource.get();
        System.out.println(resource.getStatus());

        // Put a new representation if tag has not changed
        resource.getConditions().getNoneMatch().clear();
        resource.getConditions().getMatch().add(rep.getTag());
        resource.put(rep);
        System.out.println(resource.getStatus());

        // Put a new representation when a different tag
        resource.getConditions().getMatch().clear();
        resource.getConditions().getMatch().add(new Tag("abcd7890"));
        resource.put(rep);
        System.out.println(resource.getStatus());
    }
}
