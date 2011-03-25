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

package org.restlet.ext.openid;

import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openid4java.server.ServerManager;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of a XRDS server side resource. It is used for openId
 * discovery both from the consumer and the provider.
 * 
 * @author Kristoffer Gronowski
 */
public class XrdsResource extends ServerResource {
    public static final String TYPE_RETURN_TO = "http://specs.openid.net/auth/2.0/return_to";

    public static final String TYPE_SIGNON = "http://specs.openid.net/auth/2.0/signon";

    public static final String TYPE_SERVER = "http://specs.openid.net/auth/2.0/server";

    @Get("form")
    public Representation represent() throws ParserConfigurationException {
        Document response = null;

        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        ServerManager manager = (ServerManager) attribs.get("openid_manager");
        String opEndpoint = manager.getOPEndpointUrl();

        Form query = getQuery();
        String returnTo = query.getFirstValue("returnTo");
        if (returnTo != null && returnTo.length() > 0) { // OP Server lookup
            response = createDocument(TYPE_RETURN_TO, returnTo, null);
        } else {
            String id = query.getFirstValue("id");
            if (id == null || id.length() == 0) { // OP Server lookup
                response = createDocument(TYPE_SERVER, opEndpoint, null);
            } else { // claimed ID lookup
                StringBuilder localId = new StringBuilder();
                localId.append(opEndpoint);
                localId.append("?id=");
                localId.append(id);
                response = createDocument(TYPE_SIGNON, opEndpoint,
                        localId.toString());
            }
        }

        MediaType xrds = new MediaType("application/xrds+xml");
        return new DomRepresentation(xrds, response);
    }

    private Document createDocument(String type, String uri, String localId)
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().newDocument();
        Element root = doc.createElement("xrds:XRDS");
        root.setAttribute("xmlns:xrds", "xri://$xrds");
        root.setAttribute("xmlns:openid", "http://openid.net/xmlns/1.0");
        root.setAttribute("xmlns", "xri://$xrd*($v*2.0)");
        doc.appendChild(root);

        Element xrd = doc.createElement("XRD");
        root.appendChild(xrd);

        Element service = doc.createElement("Service");
        // service.setAttribute("xmlns", "xri://$xrd*($v*2.0)");
        xrd.appendChild(service);

        Element typeElement = doc.createElement("Type");
        typeElement.appendChild(doc.createTextNode(type));
        service.appendChild(typeElement);

        Element uriElement = doc.createElement("URI");
        uriElement.appendChild(doc.createTextNode(uri));
        service.appendChild(uriElement);

        if (localId != null) {
            Element localIdElement = doc.createElement("LocalID");
            localIdElement.appendChild(doc.createTextNode(localId));
            service.appendChild(localIdElement);
        }
        return doc;
    }

}
