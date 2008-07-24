/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jxta;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import net.jxta.socket.JxtaSocket;

import org.restlet.data.Request;

import com.noelios.restlet.http.StreamClientCall;

/**
 * 
 * @author Jerome Louvel
 */
public class JxtaClientCall extends StreamClientCall {

    public JxtaClientCall(JxtaClientHelper helper, Request request) {
        super(helper, request);
    }

    @Override
    public Socket createSocket(String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        return new JxtaSocket(getHelper().getPeerGroup(), getHelper()
                .getPipeAdvertisement());
    }

    @Override
    public JxtaClientHelper getHelper() {
        return (JxtaClientHelper) super.getHelper();
    }

}
