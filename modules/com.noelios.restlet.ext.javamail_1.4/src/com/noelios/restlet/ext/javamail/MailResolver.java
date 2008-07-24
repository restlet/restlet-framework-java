package com.noelios.restlet.ext.javamail;

import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.Resolver;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Basic resolver that exposes several data from a given mail.<br>
 * 
 * <table>
 * <tr>
 * <th>What?</th>
 * <th>Key</th>
 * </tr>
 * <tr>
 * <td>Mail identifier</li>
 * <td>mailId</td>
 * </tr>
 * <tr>
 * <td>Sender</li>
 * <td>from</td>
 * </tr>
 * <tr>
 * <td>Recipients (comma separated string)</li>
 * <td>recipients</td>
 * </tr>
 * <tr>
 * <td>Subject</li>
 * <td>subject</td>
 * </tr>
 * <tr>
 * <td>Mail text part</li>
 * <td>message</td>
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
