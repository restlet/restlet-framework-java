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

package org.restlet.ext.sip;

import java.security.Principal;

import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.engine.http.connector.ConnectedRequest;
import org.restlet.engine.http.connector.ServerConnection;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

public class SipRequest extends ConnectedRequest {

    private volatile String callId;

    private volatile String callSeq;

    private volatile String from;

    private volatile String to;

    private volatile String via;

    public SipRequest(Context context, ServerConnection connection,
            String methodName, String resourceUri, String version,
            Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        super(context, connection, methodName, resourceUri, version, headers,
                entity, confidential, userPrincipal);

        // Set the "via" property
        String viaHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_VIA);
        if (viaHeader != null) {
            setVia(viaHeader);
        }

        // Set the "callId" property
        String callIdHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_ID);
        if (callIdHeader != null) {
            setCallId(callIdHeader);
        }

        // Set the "callSeq" property
        String callSeqHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_SEQ);
        if (callSeqHeader != null) {
            setCallSeq(callSeqHeader);
        }

        // Set the "to" property
        String toHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_TO);
        if (toHeader != null) {
            setTo(toHeader);
        }

        // Set the "from" property
        String fromHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_FROM);
        if (fromHeader != null) {
            setFrom(fromHeader);
        }

    }

    public String getCallId() {
        return callId;
    }

    public String getCallSeq() {
        return callSeq;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getVia() {
        return via;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setCallSeq(String callSeq) {
        this.callSeq = callSeq;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setVia(String via) {
        this.via = via;
    }

}
