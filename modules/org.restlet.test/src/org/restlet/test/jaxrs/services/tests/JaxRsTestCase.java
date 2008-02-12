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

package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.ext.jaxrs.wrappers.ResourceClass;
import org.restlet.resource.Representation;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan
 * 
 */
public abstract class JaxRsTestCase extends TestCase {

    public static final int PORT = 8181;

    /**
     * ServerWrapper to use.
     */
    private static ServerWrapper serverWrapper = new RestletServerWrapper();

    /**
     * @param httpMethod
     * @param klasse
     * @param mediaTypePrefs
     *                Collection with Preference&lt;MediaType&gt; and/or
     *                MediaType.
     * @return
     * @throws IllegalArgumentException
     *                 If an element in the mediaTypes is neither a
     *                 Preference&lt;MediaType&gt; or a MediaType object.
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Method httpMethod, Class<?> klasse,
            Collection mediaTypes) throws IllegalArgumentException {
        return accessServer(httpMethod, klasse, null, mediaTypes, null);
    }

    /**
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param mediaTypes
     * @param challengeResponse
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, Collection mediaTypes,
            ChallengeResponse challengeResponse) {
        Reference reference = createReference(klasse, subPath);
        return accessServer(httpMethod, reference, mediaTypes,
                challengeResponse, null);
    }

    /**
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param conditions
     * @return
     */
    public static Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, Conditions conditions, ClientInfo clientInfo) {
        Reference reference = createReference(klasse, subPath);
        Client client = createClient();
        Request request = new Request(httpMethod, reference);
        if (conditions != null)
            request.setConditions(conditions);
        if (clientInfo != null)
            request.setClientInfo(clientInfo);
        Response response = client.handle(request);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, MediaType mediaType) {
        Collection<MediaType> mediaTypes = null;
        if (mediaType != null)
            mediaTypes = Collections.singleton(mediaType);
        return accessServer(httpMethod, klasse, subPath, mediaTypes, null);
    }

    /**
     * @param httpMethod
     * @param reference
     * @param mediaTypes
     * @param challengeResponse
     * @param entity
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Method httpMethod, Reference reference,
            Collection mediaTypes, ChallengeResponse challengeResponse,
            Representation entity) {
        Client client = createClient();
        Request request = new Request(httpMethod, reference);
        addAcceptedMediaTypes(request, mediaTypes);
        request.setChallengeResponse(challengeResponse);
        request.setEntity(entity);
        Response response = client.handle(request);
        return response;
    }

    /**
     * @param request
     * @param mediaTypes
     */
    @SuppressWarnings("unchecked")
    private static void addAcceptedMediaTypes(Request request,
            Collection mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty())
            return;
        Collection<Preference<MediaType>> mediaTypePrefs = new ArrayList<Preference<MediaType>>(
                mediaTypes.size());
        for (Object mediaType : mediaTypes) {
            if (mediaType instanceof MediaType) {
                mediaTypePrefs.add(new Preference<MediaType>(
                        (MediaType) mediaType));
            } else if (mediaType instanceof Preference) {
                Preference<Metadata> preference = (Preference) mediaType;
                if (preference.getMetadata() instanceof MediaType)
                    mediaTypePrefs.add((Preference) preference);
            } else {
                throw new IllegalArgumentException(
                        "Valid mediaTypes are only Preference<MediaType> or MediaType");
            }
        }
        request.getClientInfo().getAcceptedMediaTypes().addAll(mediaTypePrefs);
    }

    /**
     * check, if the mainType and the subType is equal. The parameters are
     * ignored.
     * 
     * @param expected
     * @param actual
     */
    public static void assertEqualMediaType(MediaType expected, MediaType actual) {
        expected = Converter.getMediaTypeWithoutParams(expected);
        actual = Converter.getMediaTypeWithoutParams(actual);
        assertEquals(expected, actual);
    }

    /**
     * @return
     */
    protected static Client createClient() {
        return new Client(Protocol.HTTP);
    }

    /**
     * @param mediaType
     * @param mediaTypeQuality
     *                default is 1.
     * @return
     */
    public static Collection<Preference<MediaType>> createPrefColl(
            MediaType mediaType, float mediaTypeQuality) {
        if (mediaType == null)
            return Collections.emptyList();
        return Collections.singleton(new Preference<MediaType>(mediaType,
                mediaTypeQuality));
    }

    /**
     * Creates an reference that access the localhost with the JaxRsTester
     * protocol and the JaxRsTester Port. It uses the path of the given
     * jaxRsClass
     * 
     * @param jaxRsClass
     * @param subPath
     *                darf null sein
     * @return
     */
    public static Reference createReference(Class<?> jaxRsClass, String subPath) {
        Reference reference = new Reference();
        reference.setProtocol(Protocol.HTTP);
        reference.setAuthority("localhost");
        reference.setHostPort(PORT);
        String path = ResourceClass.getPathTemplate(jaxRsClass);
        if (path == null)
            throw new RuntimeException("no @Path available on " + jaxRsClass);
        if (!path.startsWith("/"))
            path = "/" + path;
        if (subPath != null && subPath.length() > 0)
            path += "/" + subPath;
        reference.setPath(path);
        return reference;
    }

    /**
     * @param subPath
     * @return
     */
    protected Request createGetRequest(String subPath) {
        Reference reference = createReference(getRootResourceClass(), subPath);
        return new Request(Method.GET, reference);
    }

    public static ServerWrapper getServerWrapper() {
        return serverWrapper;
    }

    /**
     * starts the Server for the given JaxRsTestCase, waits for an input from
     * {@link System#in} and then stops the server.
     * 
     * @param jaxRsTestCase
     * @throws Exception
     */
    public static void runServerUntilKeyPressed(JaxRsTestCase jaxRsTestCase)
            throws Exception {
        jaxRsTestCase.startServer();
        Collection<Class<?>> rrcs = jaxRsTestCase.getRootResourceColl();
        System.out
                .println("the root resource classes are available under the following pathes:");
        for (Class<?> rrc : rrcs) {
            try {
                System.out.print("http://localhost:" + jaxRsTestCase.getPort());
                System.out.println(rrc.getAnnotation(Path.class).value());
            } catch (RuntimeException e) {
                e.printStackTrace(System.out);
            }
        }
        System.out.println("press key to stop . . .");
        System.in.read();
        jaxRsTestCase.stopServer();
        System.out.println("server stopped");
    }

    /**
     * Sets the default ServerWrapper. Should be called before setUp.
     * 
     * @param newServerWrapper
     */
    public static void setServerWrapper(ServerWrapper newServerWrapper) {
        if (newServerWrapper == null)
            throw new IllegalArgumentException(
                    "null is an illegal ServerWrapper");
        serverWrapper = newServerWrapper;
    }

    /**
     * prints the entity to System.out, if the status indicates an error.
     * 
     * @param response
     * @throws IOException
     */
    public static void sysOutEntityIfError(Response response) {
        if (response.getStatus().isError()) {
            Representation entity = response.getEntity();
            try {
                if (entity != null)
                    System.out.println(entity.getText());
                else
                    System.out.println("no Entity available");
            } catch (IOException e) {
                System.out.println("Entity not readable: ");
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Checks, if the allowed methods of an OPTIONS request are the given one.
     * 
     * @param optionsResponse
     * @param methods
     *                The methods that must be allowed. If GET is included, a
     *                check for HEAD is automaticly done. But it is no problem
     *                to add the HEAD method.
     */
    public void assertAllowedMethod(Response optionsResponse, Method... methods) {
        if (optionsResponse.getStatus().isError())
            assertEquals(Status.SUCCESS_OK, optionsResponse.getStatus());
        Set<Method> expectedMethods = new HashSet<Method>(Arrays
                .asList(methods));
        if (expectedMethods.contains(Method.GET))
            expectedMethods.add(Method.HEAD);
        List<Method> allowedMethods = new ArrayList<Method>(optionsResponse
                .getAllowedMethods());
        for (Method method : methods) {
            assertTrue("allowedMethod must contain " + method, allowedMethods
                    .contains(method));
        }
        assertEquals("allowedMethods.size invalid", expectedMethods.size(),
                allowedMethods.size());
    }

    public Response get() {
        return accessServer(Method.GET, getRootResourceClass(), null, null);
    }

    public Response get(MediaType mediaType) {
        return accessServer(Method.GET, getRootResourceClass(), null, mediaType);
    }

    public Response get(String subPath) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null);
    }

    public Response get(String subPath, ChallengeResponse cr) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null,
                cr);
    }

    public Response get(String subPath, Conditions conditions) {
        return accessServer(Method.GET, getRootResourceClass(), subPath,
                conditions, null);
    }

    public Response get(String subPath, ClientInfo clientInfo) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null,
                clientInfo);
    }

    public Response get(String subPath, MediaType mediaType) {
        return accessServer(Method.GET, getRootResourceClass(), subPath,
                mediaType);
    }

    public int getPort() {
        return serverWrapper.getPort();
    }

    protected Class<?> getRootResourceClass() {
        throw new UnsupportedOperationException(
                "You must implement the methods getRootResourceClass() or createRootResourceColl(). If you only implemented createRootResourceColl(), you can't use this method");
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Collection<Class<?>> getRootResourceColl() {
        return (Collection) Collections.singleton(getRootResourceClass());
    }

    public Response head(String subPath, MediaType mediaType) {
        return accessServer(Method.HEAD, getRootResourceClass(), subPath,
                mediaType);
    }

    /**
     * @see #accessServer(Method, Class, String, Collection, ChallengeResponse)
     */
    public Response options() {
        return accessServer(Method.OPTIONS, getRootResourceClass(), null, null);
    }

    public Response options(String subPath) {
        return accessServer(Method.OPTIONS, getRootResourceClass(), subPath,
                null);
    }

    public Response post(String subPath, Representation entity,
            ChallengeResponse cr) {
        return accessServer(Method.POST, createReference(
                getRootResourceClass(), subPath), null, cr, entity);
    }

    @SuppressWarnings("unchecked")
    public Response post(String subPath, Representation entity,
            Collection accMediaTypes, ChallengeResponse challengeResponse) {
        return accessServer(Method.POST, createReference(
                getRootResourceClass(), subPath), accMediaTypes,
                challengeResponse, entity);
    }

    public Response post(String subPath, Representation entity) {
        return post(subPath, entity, null, null);
    }

    public Response post(Representation entity) {
        return post(null, entity, null, null);
    }

    public Response put(String subPath, Conditions conditions) {
        return accessServer(Method.PUT, getRootResourceClass(), subPath,
                conditions, null);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (shouldStartServerInSetUp()) {
            startServer();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected boolean shouldStartServerInSetUp() {
        return true;
    }

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @throws Exception
     */
    protected void startServer() throws Exception {
        final ChallengeScheme challengeScheme = ChallengeScheme.HTTP_BASIC;
        startServer(getRootResourceColl(), Protocol.HTTP, PORT,
                challengeScheme, null);
    }

    /**
     * @throws Exception
     */
    protected void startServer(ChallengeScheme challengeScheme)
            throws Exception {
        startServer(getRootResourceColl(), Protocol.HTTP, PORT,
                challengeScheme, null);
    }

    /**
     * @param rootResourceClasses
     * @param protocol
     * @param port
     * @param challengeScheme
     * @param contextParameter
     * @throws Exception
     */
    private void startServer(final Collection<Class<?>> rootResourceClasses,
            Protocol protocol, int port, final ChallengeScheme challengeScheme,
            Parameter contextParameter) throws Exception {
        try {
            serverWrapper.startServer(rootResourceClasses, protocol, port,
                    challengeScheme, contextParameter);
        } catch (Exception e) {
            try {
                serverWrapper.stopServer();
            } catch (Exception e1) {
                // ignore exception, throw before catched Exception later
            }
            throw e;
        }
    }

    /**
     * @throws Exception
     */
    protected void stopServer() throws Exception {
        serverWrapper.stopServer();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stopServer();
    }
}