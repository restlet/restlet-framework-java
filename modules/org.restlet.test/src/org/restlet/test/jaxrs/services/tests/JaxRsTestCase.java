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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.ApplicationConfig;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
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
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.server.DirectServerWrapper;
import org.restlet.test.jaxrs.server.DirectServerWrapperFactory;
import org.restlet.test.jaxrs.server.RestletServerWrapperFactory;
import org.restlet.test.jaxrs.server.ServerWrapper;
import org.restlet.test.jaxrs.server.ServerWrapperFactory;
import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public abstract class JaxRsTestCase extends TestCase {

    /**
     * ServerWrapperFactory to use. Default: {@link RestletServerWrapperFactory}
     */
    private static ServerWrapperFactory serverWrapperFactory;

    /**
     * if true, a real server is started and all communication uses real TCP,
     * real Restlet request and response serialization. If false, the
     * application is called without serialization.<br>
     * The first is more real, the last is very fast.
     * 
     * @see #setServerWrapperFactory(ServerWrapperFactory)
     */
    public static final boolean USE_TCP = false;
    static {
        if (USE_TCP)
            serverWrapperFactory = new RestletServerWrapperFactory();
        else
            serverWrapperFactory = new DirectServerWrapperFactory();
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
     * Some features are not ready and should only tested on the workspace of
     * the JAX-RS implementor, and if the actual date is after a given date.
     * This method checks this.
     * 
     * Year is 2008
     * 
     * @param dayOfMonth
     *                1-31
     * @param month
     *                1-12
     * @return true, if this workspace seems not to be JAX-RS implementors
     *         workspace.
     */
    public static boolean jaxRxImplementorCheck(int dayOfMonth, int month) {
        return jaxRxImplementorCheck(dayOfMonth, month, 2008);
    }

    /**
     * @param dayOfMonth
     *                1-31
     * @param month
     *                1-12
     * @param year
     *                e.g. 2008
     * @return true, if this workspace seems not to be JAX-RS implementors
     *         workspace.
     * @see #jaxRxImplementorAndAfter(int, int)
     */
    public static boolean jaxRxImplementorCheck(int dayOfMonth, int month,
            int year) {
        Date afterDate = new Date(year - 1900, month - 1, dayOfMonth);
        if (new Date().after(afterDate)) {
            String userHome = System.getProperty("user.home");
            if (userHome == null)
                return true;
            if (userHome.equals("C:\\Dokumente und Einstellungen\\Stephan")) {
                String javaClassPath = System.getProperty("java.class.path");
                if (javaClassPath == null)
                    return true;
                if (javaClassPath.startsWith("D:\\eclipse-workspaces\\Mastera"))
                    return false;
            }
        }
        return true;
    }

    /**
     * @param accMediaType
     * @param mediaTypeQuality
     *                default is 1.
     * @return
     */
    public static Collection<Preference<MediaType>> createPrefColl(
            MediaType accMediaType, float mediaTypeQuality) {
        if (accMediaType == null)
            return Collections.emptyList();
        return Collections.singleton(new Preference<MediaType>(accMediaType,
                mediaTypeQuality));
    }

    public static ServerWrapperFactory getServerWrapperFactory() {
        if(serverWrapperFactory == null)
        {
            if (USE_TCP)
                serverWrapperFactory = new RestletServerWrapperFactory();
            else
                serverWrapperFactory = new DirectServerWrapperFactory();
        }
        return serverWrapperFactory;
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
        ApplicationConfig appConfig = jaxRsTestCase.getAppConfig();
        Collection<Class<?>> rrcs = appConfig.getResourceClasses();
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
    public static void setServerWrapperFactory(ServerWrapperFactory swf) {
        if (swf == null)
            throw new IllegalArgumentException(
                    "null is an illegal ServerWrapperFactory");
        serverWrapperFactory = swf;
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
     * ServerWrapper to use.
     */
    private ServerWrapper serverWrapper = serverWrapperFactory
            .createServerWrapper();

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
    public Response accessServer(Method httpMethod, Class<?> klasse,
            Collection mediaTypes) throws IllegalArgumentException {
        return accessServer(httpMethod, klasse, null, mediaTypes, null);
    }

    /**
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param accMediaTypes
     * @param challengeResponse
     * @return
     */
    @SuppressWarnings("unchecked")
    public Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, Collection accMediaTypes,
            ChallengeResponse challengeResponse) {
        Reference reference = createReference(klasse, subPath);
        return accessServer(httpMethod, reference, accMediaTypes, null,
                challengeResponse, null, null, null);
    }

    /**
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param contextResolver
     * @return
     */
    public Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, Conditions conditions, ClientInfo clientInfo) {
        Reference reference = createReference(klasse, subPath);
        Request request = new Request(httpMethod, reference);
        if (conditions != null)
            request.setConditions(conditions);
        if (clientInfo != null)
            request.setClientInfo(clientInfo);
        return accessServer(request);
    }

    @SuppressWarnings("unchecked")
    public Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, MediaType accMediaType) {
        Collection<MediaType> mediaTypes = null;
        if (accMediaType != null)
            mediaTypes = Collections.singleton(accMediaType);
        return accessServer(httpMethod, klasse, subPath, mediaTypes, null);
    }

    protected Response accessServer(Method httpMethod, Reference reference) {
        return accessServer(httpMethod, reference, null, null, null, null,
                null, null);
    }

    @SuppressWarnings("unchecked")
    protected Response accessServer(Method httpMethod, Reference reference,
            Collection accMediaTypes, Representation entity,
            ChallengeResponse challengeResponse, Conditions conditions,
            Collection<Cookie> addCookies, Collection<Parameter> addHeaders) {
        Request request = new Request(httpMethod, reference);
        addAcceptedMediaTypes(request, accMediaTypes);
        request.setChallengeResponse(challengeResponse);
        request.setEntity(entity);
        request.setConditions(conditions);
        if (addCookies != null)
            request.getCookies().addAll(addCookies);
        if (addHeaders != null) {
            Util.getHttpHeaders(request).addAll(addHeaders);
        }
        return accessServer(request);
    }

    /**
     * @param request
     * @return
     */
    protected Response accessServer(Request request) {
        Restlet connector = getConnector();
        if (shouldAccessWithoutTcp()) {
            String hostDomain = request.getResourceRef().getHostDomain();
            Util.getHttpHeaders(request).add("host", hostDomain);
        }
        return connector.handle(request);
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
    public static void assertAllowedMethod(Response optionsResponse,
            Method... methods) {
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
     * @param response
     * @throws IOException
     */
    public static void assertEmptyEntity(Response response) throws IOException {
        if (response.getEntity() != null)
            assertEquals(null, response.getEntity().getText());
    }

    public Reference createBaseRef() {
        Reference reference = new Reference();
        reference.setProtocol(Protocol.HTTP);
        reference.setAuthority("localhost");
        if (!shouldAccessWithoutTcp())
            reference.setHostPort(serverWrapper.getPort());
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

    /**
     * Creates an reference that access the localhost with the JaxRsTester
     * protocol and the JaxRsTester Port. It uses the path of the given
     * jaxRsClass
     * 
     * @param jaxRsClass
     * @param subPath
     *                darf null sein
     * @return
     * @see #createReference(String, String)
     */
    public Reference createReference(Class<?> jaxRsClass, String subPath) {
        String path;
        try {
            path = Util.getPathTemplate(jaxRsClass);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return createReference(path, subPath);
    }

    /**
     * @param path
     * @param subPath
     * @return
     * @see #createReference(Class, String)
     */
    public Reference createReference(String path, String subPath) {
        Reference reference = createBaseRef();
        reference.setBaseRef(createBaseRef());
        if (!path.startsWith("/"))
            path = "/" + path;
        if (subPath != null) {
            if (subPath.startsWith(";")) {
                path += subPath;
            } else if (subPath.length() > 0) {
                if (path.endsWith("/") || subPath.startsWith("/"))
                    path += subPath;
                else
                    path += "/" + subPath;
            }
        }
        reference.setPath(path);
        return reference;
    }

    public Response get() {
        return accessServer(Method.GET, getRootResourceClass(), null, null);
    }

    public Response get(Cookie cookie) {
        return get(null, cookie);
    }

    public Response get(Reference reference) {
        if (reference.getBaseRef() == null)
            reference.setBaseRef(reference.getHostIdentifier());
        return accessServer(Method.GET, reference);
    }

    public Response get(Reference reference, MediaType mediaType) {
        if (reference.getBaseRef() == null)
            reference.setBaseRef(reference.getHostIdentifier());
        Collection<MediaType> mediaTypes = null;
        if (mediaType != null) {
            mediaTypes = new ArrayList<MediaType>();
            mediaTypes.add(mediaType);
        }
        return accessServer(Method.GET, reference, mediaTypes, null, null,
                null, null, null);
    }

    public Response get(MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClass(), null,
                accMediaType);
    }

    public Response get(String subPath) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null);
    }

    public Response get(String subPath, ChallengeResponse cr) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null,
                cr);
    }

    public Response get(String subPath, ClientInfo clientInfo) {
        return accessServer(Method.GET, getRootResourceClass(), subPath, null,
                clientInfo);
    }

    public Response get(String subPath, Conditions conditions) {
        return accessServer(Method.GET, getRootResourceClass(), subPath,
                conditions, null);
    }

    public Response get(String subPath, Cookie cookie) {
        return accessServer(Method.GET, createReference(getRootResourceClass(),
                subPath), null, null, null, null, TestUtils.createList(cookie),
                null);
    }

    public Response getWithCookies(String subPath, Collection<Cookie> cookies) {
        return accessServer(Method.GET, createReference(getRootResourceClass(),
                subPath), null, null, null, null, cookies, null);
    }

    public Response getWithHeaders(String subPath, Collection<Parameter> headers) {
        return accessServer(Method.GET, createReference(getRootResourceClass(),
                subPath), null, null, null, null, null, headers);
    }

    public Response get(String subPath, MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClass(), subPath,
                accMediaType);
    }

    /**
     * @return
     */
    protected ApplicationConfig getAppConfig() {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getResourceClasses() {
                return (Set) Collections.singleton(getRootResourceClass());
            }

            @Override
            public Set<Class<?>> getProviderClasses() {
                return (Set) getProvClasses();
            }
        };
        return appConfig;
    }

    /**
     * @return
     * @see #getAppConfig()
     */
    protected Set<Class<?>> getProvClasses() {
        return Collections.emptySet();
    }

    public Response getAuth(String subPath, String username, String pw) {
        return get(subPath, new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
                username, pw));
    }

    /**
     * @return
     */
    protected Restlet getConnector() {
        Restlet connector;
        if (shouldAccessWithoutTcp()) {
            connector = ((DirectServerWrapper) serverWrapper).getConnector();
        } else {
            connector = new Client(Protocol.HTTP);
        }
        return connector;
    }

    public int getPort() {
        return serverWrapper.getPort();
    }

    protected Class<?> getRootResourceClass() {
        throw new UnsupportedOperationException(
                "You must implement the methods getRootResourceClass() or getAppConfig(). If you only implemented getAppConfig(), you can't use this method");
    }

    public ServerWrapper getServerWrapper() {
        return serverWrapper;
    }

    public Response head(String subPath, MediaType accMediaType) {
        return accessServer(Method.HEAD, getRootResourceClass(), subPath,
                accMediaType);
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

    public Response post(Representation entity) {
        return post(null, entity, null, null);
    }

    public Response post(String subPath, MediaType mediaType) {
        return accessServer(Method.POST, getRootResourceClass(), subPath,
                mediaType);
    }

    public Response post(String subPath, Representation entity,
            ChallengeResponse cr) {
        return accessServer(Method.POST, createReference(
                getRootResourceClass(), subPath), null, entity, cr, null, null,
                null);
    }

    public Response post(String subPath, Representation entity) {
        return post(subPath, entity, null);
    }

    @SuppressWarnings("unchecked")
    public Response post(String subPath, Representation entity,
            Collection accMediaTypes, ChallengeResponse challengeResponse) {
        return accessServer(Method.POST, createReference(
                getRootResourceClass(), subPath), accMediaTypes, entity,
                challengeResponse, null, null, null);
    }

    public Response put(String subPath, Representation entity) {
        return put(subPath, entity, null);
    }

    public Response put(String subPath, Representation entity,
            Conditions conditions) {
        return accessServer(Method.PUT, createReference(getRootResourceClass(),
                subPath), null, entity, null, conditions, null, null);
    }

    public void setServerWrapper(ServerWrapper serverWrapper) {
        this.serverWrapper = serverWrapper;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (shouldStartServerInSetUp()) {
            startServer();
        }
    }

    /**
     * @return
     */
    protected boolean shouldAccessWithoutTcp() {
        return serverWrapper instanceof DirectServerWrapper;
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
        startServer(ChallengeScheme.HTTP_BASIC);
    }

    /**
     * @param rootResourceClasses
     * @param protocol
     * @param challengeScheme
     * @param contextParameter
     * @throws Exception
     */
    private void startServer(ApplicationConfig appConfig, Protocol protocol,
            final ChallengeScheme challengeScheme, Parameter contextParameter)
            throws Exception {
        try {
            serverWrapper.startServer(appConfig, protocol, challengeScheme,
                    contextParameter);
        } catch (Exception e) {
            try {
                stopServer();
            } catch (Exception e1) {
                // ignore exception, throw before catched Exception later
            }
            throw e;
        }
    }

    /**
     * @throws Exception
     */
    protected void startServer(ChallengeScheme challengeScheme)
            throws Exception {
        ApplicationConfig appConfig = getAppConfig();
        startServer(appConfig, Protocol.HTTP, challengeScheme, null);
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
