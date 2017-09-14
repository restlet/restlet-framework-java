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

package org.restlet.example.ext.oauth.client;

import org.restlet.ext.oauth.ProtectedClientResource;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class GoogleContactsServerResource extends ServerResource {

    @Get
    public Representation getContacts() {
        Token token = (Token) getRequest().getAttributes().get(
                Token.class.getName());
        if (token == null) {
            return new StringRepresentation("Token not found!");
        }

        ProtectedClientResource contacts = new ProtectedClientResource(
                "https://www.google.com/m8/feeds/contacts/default/full");
        contacts.setUseBodyMethod(false);
        contacts.setToken(token);

        return contacts.get();
    }
}
