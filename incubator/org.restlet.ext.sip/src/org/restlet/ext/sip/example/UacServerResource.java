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

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.sip.SipServerResource;
import org.restlet.ext.sip.SipStatus;

/**
 * Example SIP server resource for the UAC test scenario.
 * 
 * @author Jerome Louvel
 */
public class UacServerResource extends SipServerResource implements UacResource {

    private static boolean TRACE;

    private static long SLEEP_TIME;

    // private static AtomicLong TAG = new AtomicLong(1000);

    public static void main(String[] args) throws Exception {
        Server server = new Server(new Context(), Protocol.SIP,
                UacServerResource.class);
        server.start();

        File file = new File("UacServerResource.properties");
        if (file.exists()) {
            Properties p = new Properties();
            p.load(new FileInputStream(file));

            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                server.getContext().getParameters().add(
                        (String) entry.getKey(), (String) entry.getValue());
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
    }

    public void acknowledge() {
        trace();
    }

    public void start() {
        trace();

        // Indicate successful reception
        Response provisionalResponse = new Response(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_TRYING);
        provisionalResponse.commit();

        sleep();

        // Indicate that the user phone is ringing
        provisionalResponse = new Response(getRequest());
        provisionalResponse.setStatus(SipStatus.INFO_RINGING);
        provisionalResponse.commit();

        sleep();

        // Indicate that the session is progressing
        // provisionalResponse = new Response(getRequest());
        // provisionalResponse.setStatus(SipStatus.INFO_SESSION_PROGRESS);
        // provisionalResponse.commit();
        // 
        // sleep();

        // getRequest().setTo(getTo() + ";tag=restlet" + TAG.incrementAndGet());

        // Set the final response
        setStatus(SipStatus.SUCCESS_OK);
    }

    public void stop() {
        trace();
        setStatus(SipStatus.SUCCESS_OK);
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

    /**
     * Displays info about the current request.
     */
    private void trace() {
        if (TRACE) {
            System.out.println("Method: " + getMethod());
            System.out.println("Call ID: " + getCallId());
            System.out.println("Call Sequence: " + getCallSeq());
            System.out.println("To: " + getTo());
            System.out.println("From: " + getFrom());
            System.out.println("Max Forwards: " + getMaxForwards());
            System.out
                    .println("-------------------------------------------------");
        }
    }
}
