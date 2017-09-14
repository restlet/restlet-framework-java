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

package org.restlet.example.book.restlet.ch04.sec3.sub1;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages JSON.org extension.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toJson() throws JSONException {
        // Create a JSON object structure similar to a map
        JSONObject mailElt = new JSONObject();
        mailElt.put("status", "received");
        mailElt.put("subject", "Message to self");
        mailElt.put("content", "Doh!");
        mailElt.put("accountRef", new Reference(getReference(), "..")
                .getTargetRef().toString());
        return new JsonRepresentation(mailElt);
    }

    @Put
    public void store(JsonRepresentation mailRep) throws JSONException {
        // Parse the JSON representation to get the mail properties
        JSONObject mailElt = mailRep.getJsonObject();

        // Output the JSON element values
        System.out.println("Status: " + mailElt.getString("status"));
        System.out.println("Subject: " + mailElt.getString("subject"));
        System.out.println("Content: " + mailElt.getString("content"));
        System.out.println("Account URI: " + mailElt.getString("accountRef"));
    }
}
