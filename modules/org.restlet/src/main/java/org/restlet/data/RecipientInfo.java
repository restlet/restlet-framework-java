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

package org.restlet.data;

/**
 * Describes an intermediary via which the call went through. The intermediary
 * is typically a proxy or a cache.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the "Via"
 * header.
 * 
 * @author Jerome Louvel
 */
public class RecipientInfo {

    /** The protocol used to communicate with the recipient. */
    private volatile Protocol protocol;

    /** The optional comment, typically the software agent name. */
    private volatile String comment;

    /** The host name and port number or a pseudonym. */
    private volatile String name;

    /**
     * Default constructor.
     */
    public RecipientInfo() {
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The protocol used to communicate with the recipient.
     * @param name
     *            The host name and port number or a pseudonym.
     * @param agent
     *            The software agent.
     */
    public RecipientInfo(Protocol protocol, String name, String agent) {
        this.protocol = protocol;
        this.name = name;
        this.comment = agent;
    }

    /**
     * Returns the optional comment, typically the software agent name.
     * 
     * @return The optional comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the host name and port number or a pseudonym.
     * 
     * @return The host name and port number or a pseudonym.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the protocol used to communicate with the recipient.
     * 
     * @return The protocol used to communicate with the recipient.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the optional comment, typically the software agent name.
     * 
     * @param comment
     *            The optional comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the host name and port number or a pseudonym.
     * 
     * @param name
     *            The host name and port number or a pseudonym.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the protocol used to communicate with the recipient.
     * 
     * @param protocol
     *            The protocol used to communicate with the recipient.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

}
