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

package org.restlet.ext.openid;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class.
 * 
 * @author Martin Svensson
 */
public class OpenIdFormForwarder {

    /**
     * Helper class to programmatically handle the OpenID 2.0 HTML Form
     * redirection. The class can be added and if needed it will intercept and
     * perform the post on behalf of the end user. In normal operation a browser
     * would automatically post the form.
     * 
     * There is no harm in having the forwarder in place even if there is no
     * post there then the code would be skipped.
     * 
     * @param input
     *            - html form to post
     * @param resource
     *            - existing or null
     * @return response from OpenID OP
     * @throws IOException
     *             - on failed html xml parsing.
     */
    public static Representation handleFormRedirect(Representation input,
            ClientResource resource) throws IOException {
        Representation output = input;
        /**** COPY COOKIES *******************/
        if (resource != null) {
            for (CookieSetting cs : resource.getCookieSettings()) {
                resource.getCookies().add(cs.getName(), cs.getValue());
            }
        }

        if (MediaType.TEXT_HTML.equals(input.getMediaType())
                && input.getSize() != 0) {
            // Check for a form
            DomRepresentation htmlRep = new DomRepresentation(input);

            Node form = htmlRep.getNode("//form");

            if (form != null) { // Check for an on load....
                Node body = htmlRep.getNode("//body");
                NamedNodeMap nnm = body.getAttributes();
                Node onload = nnm.getNamedItem("onload");
                String val = onload.getNodeValue();
                if (val.endsWith(".submit();")) {
                    NamedNodeMap nnm2 = form.getAttributes();
                    String name = nnm2.getNamedItem("name").getNodeValue();
                    String action = nnm2.getNamedItem("action").getNodeValue();
                    String method = nnm2.getNamedItem("method").getNodeValue();
                    Context.getCurrentLogger().fine(
                            "name = " + name + " action = " + action
                                    + " method = " + method);
                    if (name != null && name.length() > 0 && action != null
                            && action.length() > 0 && method != null
                            && method.length() > 0
                            && "post".equalsIgnoreCase(method)) {
                        Form f = new Form();
                        NodeList nl = form.getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++) {
                            Node n = nl.item(i);
                            if ("input".equalsIgnoreCase(n.getNodeName())) {
                                NamedNodeMap nnm3 = n.getAttributes();
                                String key = nnm3.getNamedItem("name")
                                        .getNodeValue();
                                String value = nnm3.getNamedItem("value")
                                        .getNodeValue();
                                if (key != null && key.length() > 0) {
                                    f.add(key, value);
                                }
                            }
                        }
                        // The form is ready to send...
                        Context.getCurrentLogger().fine(
                                " Form size to send = " + f.size());
                        if (resource == null) {
                            resource = new ClientResource(action);
                        }
                        resource.setReference(action);
                        output = resource.post(f.getWebRepresentation());
                    }
                }

            }
        }

        return output;
    }

}
