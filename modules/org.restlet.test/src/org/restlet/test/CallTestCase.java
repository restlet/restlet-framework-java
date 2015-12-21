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

package org.restlet.test;

import java.util.Arrays;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.adapter.Call;

/**
 * Test {@link org.restlet.engine.adapter.Call}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com)
 */
public class CallTestCase extends RestletTestCase {
    /**
     * Returns a connector call.
     * 
     * @return A connector call instance.
     */
    protected Call getCall() {
        return new Call() {

            @Override
            protected boolean isClientKeepAlive() {
                return false;
            }

            @Override
            protected boolean isServerKeepAlive() {
                return false;
            }

        };
    }

    /**
     * Returns a reference with the specified URI.
     * 
     * @param uri
     *            The URI.
     * @return Reference instance.
     */
    protected Reference getReference(String uri) {
        return new Reference(uri);
    }

    /**
     * Returns a request.
     * 
     * @return Request instance.
     */
    protected Request getRequest() {
        return new Request();
    }

    /**
     * Returns a response.
     * 
     * @param request
     *            The associated request.
     * @return Response instance.
     */
    protected Response getResponse(Request request) {
        return new Response(request);
    }

    /**
     * Tests context's base reference getting/setting.
     */
    public void testBaseRef() throws Exception {
        final Request request = getRequest();
        final String resourceRefURI = "http://restlet.org/path/to/resource";
        final Reference resourceRef = getReference(resourceRefURI);
        request.setResourceRef(resourceRefURI);
        assertEquals(resourceRef, request.getResourceRef());
        String uri = "http://restlet.org/path";
        Reference reference = getReference(uri);
        request.getResourceRef().setBaseRef(uri);
        assertEquals(uri, request.getResourceRef().getBaseRef().toString());
        assertEquals(reference, request.getResourceRef().getBaseRef());
        uri = "http://restlet.org/path/to";
        reference = getReference(uri);
        request.getResourceRef().setBaseRef(uri);
        assertEquals(uri, request.getResourceRef().getBaseRef().toString());
        assertEquals(reference, request.getResourceRef().getBaseRef());
    }

    /**
     * Tests client address getting/setting.
     */
    public void testClientAddress() throws Exception {
        final ClientInfo client = getRequest().getClientInfo();
        String address = "127.0.0.1";
        client.setAddress(address);
        assertEquals(address, client.getAddress());
        assertEquals(0, client.getForwardedAddresses().size());
    }

    /**
     * Tests client agent getting/setting.
     */
    public void testClientAgent() throws Exception {
        final ClientInfo client = getRequest().getClientInfo();
        String name = "Restlet";
        client.setAgent(name);
        assertEquals(name, client.getAgent());
        name = "Restlet Client";
        client.setAgent(name);
        assertEquals(name, client.getAgent());
    }

    /**
     * Tests client addresses getting/setting.
     */
    public void testClientForwardedAddresses() throws Exception {
        final ClientInfo client = getRequest().getClientInfo();
        String firstAddress = "127.0.0.1";
        final String secondAddress = "192.168.99.10";
        List<String> addresses = Arrays.asList(new String[] { firstAddress,
                secondAddress });
        client.getForwardedAddresses().addAll(addresses);
        assertEquals(addresses, client.getForwardedAddresses());
        client.getForwardedAddresses().clear();
        client.getForwardedAddresses().addAll(addresses);
        assertEquals(addresses, client.getForwardedAddresses());
    }

    /**
     * Tests method getting/setting.
     */
    public void testMethod() throws Exception {
        final Request request = getRequest();
        request.setMethod(Method.GET);
        assertEquals(Method.GET, request.getMethod());
        request.setMethod(Method.POST);
        assertEquals(Method.POST, request.getMethod());
    }

    /**
     * Tests redirection reference getting/setting.
     */
    public void testRedirectionRef() throws Exception {
        final Request request = getRequest();
        final Response response = getResponse(request);
        String uri = "http://restlet.org/";
        Reference reference = getReference(uri);
        response.setLocationRef(uri);
        assertEquals(reference, response.getLocationRef());
        uri = "http://restlet.org/something";
        reference = getReference(uri);
        response.setLocationRef(reference);
        assertEquals(reference, response.getLocationRef());
    }

    /**
     * Tests referrer reference getting/setting.
     */
    public void testReferrerRef() throws Exception {
        final Request request = getRequest();
        String uri = "http://restlet.org/";
        Reference reference = getReference(uri);
        request.setReferrerRef(uri);
        assertEquals(reference, request.getReferrerRef());
        uri = "http://restlet.org/something";
        reference = getReference(uri);
        request.setReferrerRef(reference);
        assertEquals(reference, request.getReferrerRef());
    }

    /**
     * Tests resource reference getting/setting.
     */
    public void testResourceRef() throws Exception {
        final Request request = getRequest();
        String uri = "http://restlet.org/";
        Reference reference = getReference(uri);
        request.setResourceRef(uri);
        assertEquals(reference, request.getResourceRef());
        uri = "http://restlet.org/something";
        reference = getReference(uri);
        request.setResourceRef(reference);
        assertEquals(reference, request.getResourceRef());
    }

    /**
     * Tests server address getting/setting.
     */
    public void testServerAddress() throws Exception {
        final Request request = getRequest();
        final Response response = getResponse(request);
        String address = "127.0.0.1";
        response.getServerInfo().setAddress(address);
        assertEquals(address, response.getServerInfo().getAddress());
        address = "192.168.99.10";
        response.getServerInfo().setAddress(address);
        assertEquals(address, response.getServerInfo().getAddress());
    }

    /**
     * Tests server agent getting/setting.
     */
    public void testServerAgent() throws Exception {
        final Request request = getRequest();
        final Response response = getResponse(request);
        String name = "Restlet";
        response.getServerInfo().setAgent(name);
        assertEquals(name, response.getServerInfo().getAgent());
        name = "Restlet Server";
        response.getServerInfo().setAgent(name);
        assertEquals(name, response.getServerInfo().getAgent());
    }

    /**
     * Tests status getting/setting.
     */
    public void testStatus() throws Exception {
        final Request request = getRequest();
        final Response response = getResponse(request);
        response.setStatus(Status.SUCCESS_OK);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
    }

}
