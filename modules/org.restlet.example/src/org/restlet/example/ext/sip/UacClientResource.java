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

package org.restlet.example.ext.sip;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.sip.Address;
import org.restlet.ext.sip.SipClientResource;

/**
 * Example SIP client resource for the UAC test scenario.
 * 
 * @author Jerome Louvel
 */
public class UacClientResource implements UacResource {

    public static void main(String[] args) {
        // [ifdef jse] instruction
        Engine.setLogLevel(Level.FINE);
        UacClientResource cr = new UacClientResource("sip:bob@locahost");
        cr.start();
        sleep();
        cr.acknowledge();
        sleep();
        cr.stop();
    }

    /** The internal client resource. */
    private SipClientResource clientResource;

    /** The internal client resource proxy. */
    private UacResource proxy;

    /**
     * Constructor.
     * 
     * @param uri
     *            Target resource URI.
     */
    public UacClientResource(String uri) {
        this.clientResource = new SipClientResource(uri);
        this.clientResource.setCallId("a84b4c76e66710@pc33.atlanta.com");
        this.clientResource.setCommandSequence("314159");
        this.clientResource.setFrom(new Address("sip:alice@atlanta.com",
                "Alice"));
        this.clientResource.setTo(new Address("sip:bob@biloxi.com", "Bob"));

        Client client = new Client(new Context(), Protocol.SIP);
        client.getContext().getParameters().add("minThreads", "1");
        client.getContext().getParameters().add("tracing", "true");
        client.getContext().getParameters().add("proxyHost", "localhost");
        client.getContext().getParameters().add("proxyPort", "5060");
        this.clientResource.setNext(client);
        this.proxy = this.clientResource.wrap(UacResource.class);
    }

    public void acknowledge() {
        this.proxy.acknowledge();
        System.out.println("acknowledge\n");
        try {
            if (this.clientResource.getResponseEntity() != null) {
                this.clientResource.getResponseEntity().exhaust();
            }
        } catch (Exception e) {
            System.out.println("acknowledge " + e.getMessage());
        }
    }

    /**
     * Makes the current thread sleep.
     */
    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.proxy.start();
        System.out.println("start\n");
        try {
            if (this.clientResource.getResponseEntity() != null) {
                this.clientResource.getResponseEntity().exhaust();
            }
        } catch (Exception e) {
            System.out.println("start " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        this.proxy.stop();
        System.out.println("stop\n");
        try {
            if (this.clientResource.getResponseEntity() != null) {
                this.clientResource.getResponseEntity().exhaust();
            }
        } catch (Exception e) {
            System.out.println("stop " + e.getMessage());
            e.printStackTrace();
        }
    }

}
