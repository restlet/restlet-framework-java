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

package org.restlet.ext.sip;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

/**
 * SIP server resource handling a received SIP transaction.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SipServerResource extends ServerResource {

    /**
     * Returns the request's call ID.
     * 
     * @return The request's call ID.
     */
    public String getCallId() {
        return getRequest().getCallId();
    }

    /**
     * Returns the request's command sequence.
     * 
     * @return The request's command sequence.
     */
    public String getCommandSequence() {
        return getRequest().getCommandSequence();
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
     * Returns the request recipient's address.
     * 
     * @return The request recipient's address.
     */
    public Address getTo() {
        return getRequest().getTo();
    }

    @Override
    public void init(Context context, Request request, Response response) {
        try {
            SipResponse sipResponse = (SipResponse) response;
            SipRequest sipRequest = (SipRequest) request;

            sipResponse.setCallId(sipRequest.getCallId());
            sipResponse.setCommandSequence(sipRequest.getCommandSequence());

            if (sipRequest.getFrom() != null) {
                sipResponse.setFrom((Address) sipRequest.getFrom().clone());
            }

            if (sipRequest.getTo() != null) {
                sipResponse.setTo((Address) sipRequest.getTo().clone());
            }
        } catch (CloneNotSupportedException e) {
            doCatch(e);
        }

        super.init(context, request, response);
    }

}
