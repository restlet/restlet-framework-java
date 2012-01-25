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

package org.restlet.ext.sip;

import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;
import org.restlet.util.Series;

/**
 * Describes a SIP intermediary such as a proxy. Used by the SIP "Via" header.
 * 
 * @author Thierry Boileau
 */
public class SipRecipientInfo extends RecipientInfo {

    /** The list of recipient info parameters. */
    private Series<Parameter> parameters;

    /** The transport protocol name. */
    private String transport;

    /**
     * Constructor.
     */
    public SipRecipientInfo() {
        super();
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
    public SipRecipientInfo(Protocol protocol, String name, String agent) {
        super(protocol, name, agent);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The protocol used to communicate with the recipient.
     * @param transport
     *            The transport protocol name.
     * @param name
     *            The host name and port number or a pseudonym.
     * @param agent
     *            The software agent.
     */
    public SipRecipientInfo(Protocol protocol, String transport, String name,
            String agent) {
        super(protocol, name, agent);
        this.transport = transport;
    }

    /**
     * Returns the list of recipient info parameters.
     * 
     * @return The list of recipient info parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Series<Parameter>(Parameter.class);
        }
        return parameters;
    }

    /**
     * Returns the transport protocol name.
     * 
     * @return The transport protocol name.
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Sets the list of recipient info parameters.
     * 
     * @param parameters
     *            The list of recipient info parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the transport protocol name.
     * 
     * @param transport
     *            The transport protocol name.
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

}
