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
package org.restlet.example.book.restlet.ch06;

import java.security.Principal;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.security.Role;

/**
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 * 
 */
public class EchoPrincipalsResource extends ServerResource {
    @Get("txt")
    public Representation echoPrincipals() throws ResourceException {
        StringBuilder sb = new StringBuilder("* User: ");
        sb.append(getClientInfo().getUser());
        sb.append("\n");

        sb.append("* Roles: \n");
        for (Role role : getClientInfo().getRoles()) {
            sb.append("  - ");
            sb.append(role.getName());
            sb.append("\n");
        }

        sb.append("* Principals: \n");
        for (Principal principal : getClientInfo().getPrincipals()) {
            sb.append("  - ");
            sb.append(principal.getName());
            sb.append(" (");
            sb.append(principal.getClass());
            sb.append(")\n");
        }

        Representation rep = new StringRepresentation(sb, MediaType.TEXT_PLAIN,
                Language.ALL, CharacterSet.UTF_8);
        return rep;
    }
}
