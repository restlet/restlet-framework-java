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

package org.restlet.test.ext.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.restlet.Client;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.ext.oauth.Flow;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.ext.oauth.app.SingletonStore;

public class MultipleUserAuthorizationServerTestCase extends OAuthHttpTestBase {

    public MultipleUserAuthorizationServerTestCase() {
        this(false);
    }

    public MultipleUserAuthorizationServerTestCase(boolean https) {
        super(true, https, ClientConnector.HTTP_CLIENT, ServerConnector.JETTY);
        Engine.setLogLevel(Level.WARNING);
    }

    public void testMultipleServerRequests() throws Exception {
        int numThreads = 10;
        int numCalls = 50;
        int totCalls = (numThreads * numCalls);
        List<Callable<Boolean>> calls = new ArrayList<Callable<Boolean>>(
                totCalls);
        ExecutorService es = Executors.newFixedThreadPool(numThreads);
        Client c = this.createClient();
        Random r = new Random();
        for (int i = 0; i < totCalls; i++) {
            calls.add(new OAuthRequest(c, r.nextInt(5) + 1));
        }
        long l = System.currentTimeMillis();
        es.invokeAll(calls);
        es.shutdown();
        es.awaitTermination(30, TimeUnit.SECONDS);
        long tot = System.currentTimeMillis() - l;
        if (!es.isTerminated()) {
            Logger.getAnonymousLogger().warning(
                    "All calls threads did not execute within 30 seconds");
            es.shutdownNow();
        }
        Assert.assertEquals(totCalls, SingletonStore.I().getCallbacks());
        Assert.assertEquals(0, SingletonStore.I().getErrors());
        int totReq = totCalls * 2;
        double avg = (double) tot / (double) totReq;
        Logger.getAnonymousLogger().warning(
                "Executed " + totReq + " in " + tot + " millis (" + avg
                        + " average/request)");
    }

    private class OAuthRequest implements Callable<Boolean> {
        Client callClient;

        int callUser;

        public OAuthRequest(Client client, int user) {
            callClient = client;
            callUser = user;
        }

        public Boolean call() throws Exception {
            // System.out.println(""+Thread.currentThread().getName());
            OAuthParameters params = new OAuthParameters("client1234",
                    "secret1234", getProt() + "://localhost:" + oauthServerPort
                            + "/oauth/", Scopes.toRoles("foo bar"));
            OAuthUser user = Flow.PASSWORD.execute(params, null, null, "user"
                    + callUser, "pass" + callUser, null, callClient);
            if (user == null) {
                SingletonStore.I().addError();
                SingletonStore.I().addRequest();
                return false;
            }
            Reference ref = new Reference(getProt() + "://localhost:"
                    + serverPort + "/server/scoped/user" + callUser);
            ref.addQueryParameter("oauth_token", user.getAccessToken());
            ClientResource cr = new ClientResource(ref);
            cr.setNext(callClient);
            Representation r = cr.get();
            if (r == null) {
                SingletonStore.I().addError();
                SingletonStore.I().addRequest();
                cr.release();
                return false;
            }
            try {
                String text = r.getText();
                if (!text.endsWith("user" + callUser)) {
                    SingletonStore.I().addRequest();
                    SingletonStore.I().addError();
                    return false;
                }
            } catch (Exception e) {
                SingletonStore.I().addError();
                SingletonStore.I().addRequest();
                return false;
            }
            SingletonStore.I().addRequest();
            r.release();
            cr.release();
            return true;
        }
    }
}
