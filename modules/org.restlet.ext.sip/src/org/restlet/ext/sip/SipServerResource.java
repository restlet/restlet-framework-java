/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import java.util.List;

import org.restlet.resource.ServerResource;

/**
 * SIP server resource handling a received SIP transaction.
 * 
 * @author Thierry Boileau
 */
public class SipServerResource extends ServerResource {

    /**
     * Returns the request's call id.
     * 
     * @return The request's call id.
     */
    public String getCallId() {
        return getRequest().getCallId();
    }

    /**
     * Returns the request's call sequence.
     * 
     * @return The request's call sequence.
     */
    public String getCallSequence() {
        return getRequest().getCallSequence();
    }

    /**
     * Returns the request initiator's address.
     * 
     * @return The request initiator's address.
     */
    public Address getFrom() {
        return getRequest().getFrom();
    }

    @Override
    public SipRequest getRequest() {
        return (SipRequest) super.getRequest();
    }

    @Override
    public SipResponse getResponse() {
        return (SipResponse) super.getResponse();
    }

    /**
     * Returns the request's list of Via entries.
     * 
     * @return The request's list of Via entries.
     */
    public List<SipRecipientInfo> getSipRequestRecipientsInfo() {
        return getRequest().getSipRecipientsInfo();
    }

    /**
     * Returns the response's list of Via entries.
     * 
     * @return The response's list of Via entries.
     */
    public List<SipRecipientInfo> getSipResponseRecipientsInfo() {
        return getResponse().getSipRecipientsInfo();
    }

    /**
     * Returns the request recipient's address.
     * 
     * @return The request recipient's address.
     */
    public Address getTo() {
        return getRequest().getTo();
    }
}
