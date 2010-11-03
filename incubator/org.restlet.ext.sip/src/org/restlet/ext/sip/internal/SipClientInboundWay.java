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

package org.restlet.ext.sip.internal;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.connector.ClientInboundWay;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.sip.SipResponse;
import org.restlet.ext.sip.SipStatus;
import org.restlet.util.Series;

/**
 * SIP client inbound way.
 * 
 * @author Thierry Boileau
 */
public class SipClientInboundWay extends ClientInboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @throws IOException
     */
    public SipClientInboundWay(Connection<Client> connection) {
        super(connection);
    }

    @Override
    protected void copyResponseTransportHeaders(Series<Parameter> headers,
            Response response) {
        SipResponse sr = (SipResponse) response;

        for (Parameter header : headers) {
            if (header.getName().equalsIgnoreCase(HeaderConstants.HEADER_VIA)) {
                SipRecipientInfoReader.addValues(header, sr
                        .getSipRecipientsInfo());
            }
        }

        // Don't let the parent code handle the VIA header
        // according to HTTP syntax.
        headers.removeAll(HeaderConstants.HEADER_VIA, true);
        super.copyResponseTransportHeaders(headers, response);
    }

    @Override
    protected Status createStatus(int code) {
        return SipStatus.valueOf(code);
    }
}
