/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.sip.example;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.sip.Ack;
import org.restlet.ext.sip.Bye;
import org.restlet.ext.sip.Invite;
import org.restlet.ext.sip.SipServerResource;
import org.restlet.ext.sip.SipStatus;

/**
 * Example SIP server resource for the UAC test scenario.
 * 
 * @author Jerome Louvel
 */
public class UacServerResource extends SipServerResource {

    public static void main(String[] args) throws Exception {
        Server server = new Server(Protocol.SIP, UacServerResource.class);
        server.start();
    }

    @Ack
    public void acknowledge() {
        trace();
    }

    @Invite
    public void start() {
        trace();

        // Indicate successful reception
        Response provisionalResponse = new Response(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_TRYING);
        provisionalResponse.commit();

        // Indicate that the user phone is ringing
        provisionalResponse = new Response(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_RINGING);
        provisionalResponse.commit();

        // Set the final response
        setStatus(SipStatus.SUCCESS_OK);
    }

    @Bye
    public void stop() {
        trace();
        setStatus(SipStatus.SUCCESS_OK);
    }

    /**
     * Displays info about the current request.
     * 
     */
    private void trace() {
        System.out.println("Method: " + getMethod());
        System.out.println("Call ID: " + getCallId());
        System.out.println("Call Sequence: " + getCallSeq());
        System.out.println("To: " + getTo());
        System.out.println("From: " + getFrom());
        System.out.println("Max Forwards: " + getMaxForwards());
        System.out.println("-------------------------------------------------");
    }
}
