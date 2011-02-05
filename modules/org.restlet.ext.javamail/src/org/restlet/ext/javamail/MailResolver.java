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

package org.restlet.ext.javamail;

import java.util.HashMap;
import java.util.Map;

import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Resolver;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Basic resolver that exposes several data from a given mail. Here is a list of
 * the keys available from this resolver and their corresponding value.<br>
 * 
 * <table>
 * <tr>
 * <th>Key</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>mailId</td>
 * <td>Mail identifier</li>
 * </tr>
 * <tr>
 * <td>from</td>
 * <td>Sender</li>
 * </tr>
 * <tr>
 * <td>recipients</td>
 * <td>Recipients (comma separated string)</li>
 * </tr>
 * <tr>
 * <td>subject</td>
 * <td>Subject</li>
 * </tr>
 * <tr>
 * <td>message</td>
 * <td>Mail text part</li>
 * </tr>
 * </table>
 */
public class MailResolver extends Resolver<String> {
    /** Mail identifier. */
    private final String identifier;

    /** The variables to use when formatting. */
    private Map<String, Object> map;

    /**
     * Constructor.
     * 
     * @param identifier
     *            Identifier of the mail.
     * 
     */
    public MailResolver(String identifier) {
        this(identifier, null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            Identifier of the mail.
     * @param mail
     *            The mail.
     * 
     */
    public MailResolver(String identifier, Representation mail) {
        this.identifier = identifier;

        if (mail != null) {
            this.map = new HashMap<String, Object>();
            DomRepresentation rep = null;
            if (mail instanceof DomRepresentation) {
                rep = (DomRepresentation) mail;
            } else {
                rep = new DomRepresentation(mail);
            }

            Node node = rep.getNode("/email/head/from/text()");
            if (node != null) {
                add("from", node.getNodeValue());
            }
            final NodeList nodes = rep.getNodes("/email/head/to/text()");
            if ((nodes != null) && (nodes.getLength() > 0)) {
                final StringBuilder builder = new StringBuilder(nodes.item(0)
                        .getNodeValue());
                for (int i = 1; i < nodes.getLength(); i++) {
                    builder.append(", ").append(nodes.item(i).getNodeValue());
                }
                add("recipients", builder.toString());
            }
            node = rep.getNode("/email/head/subject/text()");
            if (node != null) {
                add("subject", node.getNodeValue());
            }
            node = rep.getNode("/email/body/text()");
            if (node != null) {
                add("message", node.getNodeValue());
            }
        }
    }

    /**
     * Add a value.
     * 
     * @param key
     *            The key of the value.
     * @param value
     *            The value.
     */
    public void add(String key, String value) {
        this.map.put(key, value);
    }

    @Override
    public String resolve(String name) {
        String result = null;

        if ("mailId".equals(name)) {
            result = this.identifier;
        } else {
            final Object value = this.map.get(name);
            result = (value == null) ? null : value.toString();
        }

        return result;
    }

}
