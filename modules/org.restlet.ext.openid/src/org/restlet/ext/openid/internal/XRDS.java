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

package org.restlet.ext.openid.internal;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.util.Series;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Describes an eXtensible Resource Descriptor Sequence (XRDS).
 * 
 * @author Martin Svensson
 */
public class XRDS {

    private static final String Type = "Type";

    public static final String TYPE_RETURN_TO = "http://specs.openid.net/auth/2.0/return_to";

    public static final String TYPE_SERVER = "http://specs.openid.net/auth/2.0/server";

    public static final String TYPE_SIGNON = "http://specs.openid.net/auth/2.0/signon";

    private static final String URI = "URI";

    private static final String XRD = "XRD";

    public static final MediaType XRDS = new MediaType("application/xrds+xml",
            "XRDS Document");

    private static Element createRootAndXrd(DomRepresentation dr)
            throws Exception {
        dr.setIndenting(true);
        Document d = dr.getDocument();
        Element root = d.createElement("xrds:XRDS");
        root.setAttribute("xmlns:xrds", "xri://$xrds");
        root.setAttribute("xmlns:openid", "http://openid.net/xmlns/1.0");
        root.setAttribute("xmlns", "xri://$xrd*($v*2.0)");
        d.appendChild(root);
        Element xrd = d.createElement(XRD);
        root.appendChild(xrd);
        return xrd;
    }

    private static Element createService(Document d, Element parent,
            int priority) {
        Element service = d.createElement("Service");
        service.setAttribute("priority", "" + priority);
        parent.appendChild(service);
        return service;
    }

    private static void insert(Document d, Element parent, String node,
            String text) {
        Element elem = d.createElement(node);
        if (text != null)
            elem.appendChild(d.createTextNode(text));
        parent.appendChild(elem);
    }

    public static DomRepresentation returnToXrds(String returnTo)
            throws Exception {
        DomRepresentation dr = new DomRepresentation(XRDS);
        Element xrd = createRootAndXrd(dr);
        Element service = createService(dr.getDocument(), xrd, 0);
        insert(dr.getDocument(), service, Type, TYPE_RETURN_TO);
        insert(dr.getDocument(), service, URI, returnTo);
        return dr;
    }

    public static DomRepresentation serverSignon(String serverURI)
            throws Exception {
        DomRepresentation dr = new DomRepresentation(XRDS);
        Element xrd = createRootAndXrd(dr);
        Element service = createService(dr.getDocument(), xrd, 0);
        insert(dr.getDocument(), service, Type, TYPE_SIGNON);
        insert(dr.getDocument(), service, URI, serverURI);
        return dr;
    }

    public static DomRepresentation serverXrds(String serverURI, boolean ax)
            throws Exception {
        DomRepresentation dr = new DomRepresentation(XRDS);
        Element xrd = createRootAndXrd(dr);
        Element service = createService(dr.getDocument(), xrd, 0);
        insert(dr.getDocument(), service, Type, TYPE_SERVER);
        if (ax)
            insert(dr.getDocument(), service, Type,
                    "http://openid.net/srv/ax/1.0");
        insert(dr.getDocument(), service, URI, serverURI);
        return dr;
    }

    public static void setXRDSHeader(Response resp, String xrdsLocation) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) resp.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        if (headers == null) {
            headers = new Series<Header>(Header.class);
            resp.getAttributes()
                    .put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
        }
        headers.add(new Header("X-XRDS-Location", xrdsLocation));
    }

}
