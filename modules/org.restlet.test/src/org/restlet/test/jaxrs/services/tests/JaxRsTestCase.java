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
import org.restlet.Component;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

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

    public static final Protocol PROTOCOL = Protocol.HTTP;

    /**
     * ServerWrapper to use.
     */
    private static ServerWrapper serverWrapper = new RestletServerWrapper();

    /**
     * @see #accessServer(Class, String, Method, Collection)
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Class<?> klasse, Method httpMethod) {
        return accessServer(klasse, httpMethod, (Collection) null);
    }

    /**
     * @param klasse
     * @param httpMethod
     * @param mediaTypePrefs
     *                Collection with Preference&lt;MediaType&gt; and/or
     *                MediaType.
     * @return
     * @throws IllegalArgumentException
     *                 If an element in the mediaTypes is neither a
     *                 Preference&lt;MediaType&gt; or a MediaType-Objekten.
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Class<?> klasse, Method httpMethod,
            Collection mediaTypes) throws IllegalArgumentException {
        return accessServer(klasse, null, httpMethod, mediaTypes);
    }

    /**
     * @param klasse
     * @param httpMethod
     * @param mediaTypePrefs
     * @return
     */
    public static Response accessServer(Class<?> klasse, Method httpMethod,
            MediaType mediaType) {
        return accessServer(klasse, httpMethod, createPrefColl(mediaType, 1f));
    }

    @SuppressWarnings("unchecked")
    public static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod) {
        return accessServer(klasse, subPath, httpMethod, (Collection) null);
    }

    /**
     * @param klasse
     * @param subPath
     * @param httpMethod
     * @param mediaTypes
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod, Collection mediaTypes) {
        Reference reference = createReference(klasse, subPath);
        Client client = new Client(PROTOCOL);
        Request request = new Request(httpMethod, reference);
        addAcceptedMediaTypes(request, mediaTypes);
        // ausgeben(request);
        Response response = client.handle(request);
        return response;
    }

    /**
     * @param klasse
     * @param subPath
     * @param httpMethod
     * @param conditions
     * @return
     */
    public static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod, Conditions conditions, ClientInfo clientInfo) {
        Reference reference = createReference(klasse, subPath);
        Client client = new Client(PROTOCOL);
        Request request = new Request(httpMethod, reference);
        if(conditions != null)
            request.setConditions(conditions);
        if(clientInfo != null)
            request.setClientInfo(clientInfo);
        Response response = client.handle(request);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod, MediaType mediaType) {
        Collection<MediaType> mediaTypes = null;
        if (mediaType != null)
            mediaTypes = Collections.singleton(mediaType);
        return accessServer(klasse, subPath, httpMethod, mediaTypes);
    }

    /**
     * @param request
     * @param mediaTypes
     */
    @SuppressWarnings("unchecked")
    private static void addAcceptedMediaTypes(Request request,
            Collection mediaTypes) {
        if (mediaTypes != null && !mediaTypes.isEmpty()) {
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
            request.getClientInfo().getAcceptedMediaTypes().addAll(
                    mediaTypePrefs);
        }
    }

    /**
     * check, if the mainType and the subType is equal. The parameters are
     * ignored.
     * 
     * @param expected
     * @param actual
     */
    public static void assertEqualMediaType(MediaType expected, MediaType actual) {
        assertEquals(expected.getMainType(), actual.getMainType());
        assertEquals(expected.getSubType(), actual.getSubType());
    }

    /**
     * @param mediaType
     * @param mediaTypeQual
     *                default is 1.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Collection<Preference<MediaType>> createPrefColl(
            MediaType mediaType, float mediaTypeQual) {
        if (mediaType == null)
            return Collections.EMPTY_LIST;
        return Collections.singleton(new Preference<MediaType>(mediaType,
                mediaTypeQual));
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
        reference.setProtocol(PROTOCOL);
        reference.setAuthority("localhost");
        reference.setHostPort(PORT);
        Path pathAnnotation = jaxRsClass.getAnnotation(Path.class);
        if (pathAnnotation == null)
            throw new RuntimeException("no @Path available on " + jaxRsClass);
        String path = pathAnnotation.value();
        if (!path.startsWith("/"))
            path = "/" + path;
        if (subPath != null && subPath.length() > 0)
            path += "/" + subPath;
        reference.setPath(path);
        return reference;
    }

    public static ServerWrapper getServerWrapper() {
        return serverWrapper;
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

    /**
     * @return
     */
    protected abstract Collection<Class<?>> createRootResourceColl();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.startServer(createRootResourceColl());
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @see #startServer(Protocol, int, Collection)
     */
    public void startServer(Class<?> klasse) throws Exception {
        startServer(PORT, klasse);
    }

    /**
     * @see #startServer(Protocol, int, Collection)
     */
    public void startServer(final Collection<Class<?>> rootResourceClasses)
            throws Exception {
        startServer(PORT, rootResourceClasses);
    }

    /**
     * @see #startServer(Protocol, int, Collection)
     */
    public void startServer(int port, Class<?> rootResourceClass)
            throws Exception {
        final Collection<Class<?>> rootResourceClasses = new ArrayList<Class<?>>();
        rootResourceClasses.add(rootResourceClass);
        startServer(port, rootResourceClasses);
    }

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param protocol
     * @param port
     * @param rootResourceClasses
     * @return Returns the started component. Should be stopped with
     *         {@link #stopServer(Component)}
     * @throws Exception
     */
    public void startServer(int port,
            final Collection<Class<?>> rootResourceClasses) throws Exception {
        try {
            serverWrapper.startServer(rootResourceClasses, port);
        } catch (Exception e) {
            try {
                serverWrapper.stopServer();
            } catch (Exception e1) {
                // ignore exception, throw before catched Exception later
            }
            throw e;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        serverWrapper.stopServer();
    }
}