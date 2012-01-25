/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.ext.oauth.experimental;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Utility for setting up a static HTML page resource. For more complex and
 * dynamic pages FreeMarker is a better choice.
 * 
 * @author Kristoffer Gronowski
 */
public class StaticHtmlPage extends Finder {

    private String page;

    private MediaType type;

    public StaticHtmlPage(String uri) {
        Reference ref = new Reference(uri);
        // TODO could check that it is CLAP and ends .html
        ClientResource local = new ClientResource(ref);
        Representation tmpPage = local.get();

        try {
            page = tmpPage.getText();
        } catch (IOException e) {
            page = e.getLocalizedMessage();
        }

        type = tmpPage.getMediaType();
        tmpPage.release();
        local.release();
    }

    @Override
    public ServerResource find(Request request, Response response) {
        Representation result = new StringRepresentation(page, type);
        // page.setLocationRef(request.getResourceRef());
        return new StaticServerResource(result);
    }
}
