/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.test.ext.oauth;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;


import junit.framework.Assert;

import org.hamcrest.Matchers;
import org.restlet.Client;
import org.restlet.Context;

import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.ext.oauth.Flow;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import org.restlet.test.ext.oauth.app.SingletonStore;


import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;

public class MultipleUserAuthorizationServerTestCase
    extends OAuthHttpTestBase{
    
    public MultipleUserAuthorizationServerTestCase(){
        this(false);
    }
    
    public MultipleUserAuthorizationServerTestCase(boolean https){
        super(true, https, ClientConnector.HTTP_CLIENT, ServerConnector.JETTY);
        Engine.setLogLevel(Level.WARNING);
    }

    public void testMultipleServerRequests() throws Exception {
        //Just test something:
        int numThreads = 5;
        int numCalls = 50;
        int totRequests = (numThreads * numCalls) + numThreads;
        Thread[] clients = new Thread[numThreads];
        Context c = new Context();

        Client client = null;

        long l = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            if (i % 25 == 0)
                client = new Client(Protocol.HTTP);
            clients[i] = new ClientCall(numCalls, c, client);
            clients[i].start();
        }
        Awaitility.setDefaultTimeout(Duration.FOREVER);
        Awaitility.await().until(numCalls(), Matchers.equalTo(totRequests));
        long tot = System.currentTimeMillis() - l;
        Engine.getAnonymousLogger().warning("executed in "+tot+" milliseconds"+" "+(tot/(numThreads*numCalls)));
        int errors = SingletonStore.I().getErrors();
        SingletonStore.I().clear();
        Assert.assertEquals(0, errors);
    }

    private Callable<Integer> numCalls() {
        return new Callable<Integer>() {
            public Integer call() throws Exception {
                return SingletonStore.I().getCallbacks();
                //return i;
            }
        };
    }

    class ClientCall extends Thread {

        int numTimes;

        Random r;

        OAuthParameters params;

        Context c;

        Client myClient;

        public ClientCall(int numTimes, Context c, Client client) {
            this.numTimes = numTimes;
            this.c = c;
            if (client != null)
                myClient = client;
            else
                myClient = new Client(Protocol.HTTP);
            r = new Random(System.nanoTime());
            params = new OAuthParameters(
                    "client1234",
                    "secret1234",
                    getProt()
                            + "://localhost:"
                            + oauthServerPort
                            + "/oauth/", Scopes.toRoles("foo bar"));
        }

        @Override
        public void run() {
            for (int i = 0; i < numTimes; i++) {
                // System.out.println(this.getName()+" "+i);
                int u = r.nextInt(5) + 1;
                OAuthUser user = Flow.PASSWORD.execute(params, null, null,
                        "user" + u, "pass" + u, null, myClient);
                
                /*
                 * OAuthUser user = OAuthUtils.passwordFlow(params, "user" + u,
                 * "pass" + u, myClient);
                 */
                if (user == null) {
                    SingletonStore.I().addError();
                    SingletonStore.I().addRequest();
                    continue;
                }

                Reference ref = new Reference(getProt() + "://localhost:"
                        + serverPort + "/server/scoped/user" + u);
                ref.addQueryParameter("oauth_token", user.getAccessToken());
                ClientResource cr = new ClientResource(ref);
                cr.setNext(myClient);
                Representation r = cr.get();
                if (r == null) {
                    SingletonStore.I().addError();
                    SingletonStore.I().addRequest();
                    cr.release();
                    continue;
                }
                try {
                    String text = r.getText();
                    //System.out.println("this is response text: "+text);
                    if (!text.endsWith("user" + u)) {
                        SingletonStore.I().addError();
                    }
                } catch (Exception e) {
                    SingletonStore.I().addError();
                }
                SingletonStore.I().addRequest();
                r.release();
                cr.release();
            }
            SingletonStore.I().addRequest();
        }

    }

}
