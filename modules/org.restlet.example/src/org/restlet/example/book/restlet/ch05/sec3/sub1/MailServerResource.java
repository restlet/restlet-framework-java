/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.example.book.restlet.ch05.sec3.sub1;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages JSON.org API.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        // Create a JSON object structure similar to a map
        JSONObject mailElt = new JSONObject();

        try {
            mailElt.put("status", "received");
            mailElt.put("subject", "Message to self");
            mailElt.put("content", "Doh!");
            mailElt.put("accountRef", new Reference(getReference(), "..")
                    .getTargetRef().toString());
        } catch (JSONException e) {
            throw new ResourceException(e);
        }

        return new JsonRepresentation(mailElt);
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        try {
            // Parse the JSON representation to get the mail properties
            JsonRepresentation mailRep = new JsonRepresentation(representation);
            JSONObject mailElt = mailRep.getJsonObject();

            // Output the JSON element values
            System.out.println("Status: " + mailElt.getString("status"));
            System.out.println("Subject: " + mailElt.getString("subject"));
            System.out.println("Content: " + mailElt.getString("content"));
            System.out.println("Account URI: "
                    + mailElt.getString("accountRef"));
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        return null;
    }
}
