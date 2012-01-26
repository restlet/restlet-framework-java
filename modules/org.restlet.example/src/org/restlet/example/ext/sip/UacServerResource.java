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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.sip.SipResponse;
import org.restlet.ext.sip.SipServerResource;
import org.restlet.ext.sip.SipStatus;

/**
 * Example SIP server resource for the UAC test scenario.
 * 
 * @author Jerome Louvel
 */
public class UacServerResource extends SipServerResource implements UacResource {

    private static long SLEEP_TIME;

    private static boolean TRACE;

    public static void main(String[] args) throws Exception {
        // [ifdef jse] instruction
        Engine.setLogLevel(Level.FINE);
        Server server = null;

        if (args.length == 1) {
            server = new Server(new Context(), Protocol.SIP,
                    Integer.parseInt(args[0]), UacServerResource.class);
        } else {
            server = new Server(new Context(), Protocol.SIP,
                    UacServerResource.class);
        }

        ClassLoader cl = UacServerResource.class.getClassLoader();
        InputStream is = cl.getResourceAsStream("UacServerResource.properties");

        if (is == null) {
            is = cl.getResourceAsStream("org/restlet/example/ext/sip/UacServerResource.properties");
        }

        if (is != null) {
            Properties p = new Properties();
            p.load(is);

            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                server.getContext()
                        .getParameters()
                        .add((String) entry.getKey(), (String) entry.getValue());
            }

            // Sets the sleep time of this resource
            String str = p.getProperty("sleepTime", "100");

            try {
                SLEEP_TIME = Integer.parseInt(str);
            } catch (Throwable e) {
            }

            str = p.getProperty("trace", "false");

            try {
                TRACE = Boolean.parseBoolean(str);
            } catch (Throwable e) {
            }
        }

        server.start();
    }

    private static AtomicLong TAG = new AtomicLong(1000);

    public void acknowledge() {
        trace();
    }

    /**
     * Makes the current thread sleep.
     */
    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        trace();

        // Indicate successful reception
        SipResponse provisionalResponse = new SipResponse(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_TRYING);
        provisionalResponse.commit();

        sleep();

        // Indicate that the user phone is ringing
        provisionalResponse = new SipResponse(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_RINGING);
        provisionalResponse.commit();

        sleep();

        // Indicate that the session is progressing
        provisionalResponse = new SipResponse(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_SESSION_PROGRESS);
        provisionalResponse.commit();

        sleep();
        if (getTo() != null) {
            getTo().getParameters().add("tag",
                    "restlet" + TAG.incrementAndGet());
        }

        // Send a first final response
        provisionalResponse = new SipResponse(getRequest());
        provisionalResponse.setStatus(SipStatus.SUCCESS_OK);
        provisionalResponse.commit();

        sleep();

        // Set the final response
        setStatus(SipStatus.SUCCESS_OK);
    }

    public void stop() {
        trace();
        setStatus(SipStatus.SUCCESS_OK);
    }

    /**
     * Displays info about the current request.
     */
    private void trace() {
        if (TRACE) {
            System.out.println("--------------start trace--------------------");
            System.out.println("Method: " + getMethod());
            System.out.println("Call ID: " + getCallId());
            System.out.println("Call Sequence: " + getCommandSequence());
            System.out.println("To: " + getTo());
            System.out.println("From: " + getFrom());
            System.out.println("Max Forwards: " + getMaxForwards());
            System.out.println("---------------------------------------------");
        }
    }
}
