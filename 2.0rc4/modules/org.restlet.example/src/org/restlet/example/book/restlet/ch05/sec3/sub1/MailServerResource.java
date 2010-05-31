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
