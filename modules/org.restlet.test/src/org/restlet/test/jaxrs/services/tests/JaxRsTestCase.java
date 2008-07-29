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
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.ApplicationConfig;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.ext.jaxrs.RoleChecker;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.server.RestletServerTestCase;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public abstract class JaxRsTestCase extends RestletServerTestCase {

    /**
     * Checks, if the allowed methods of an OPTIONS request are the given one.
     * 
     * @param optionsResponse
     * @param methods
     *            The methods that must be allowed. If GET is included, a check
     *            for HEAD is automaticly done. But it is no problem to add the
     *            HEAD method.
     */
    public static void assertAllowedMethod(Response optionsResponse,
            Method... methods) {
        if (optionsResponse.getStatus().isError()) {
            assertEquals(Status.SUCCESS_OK, optionsResponse.getStatus());
        }
        final Set<Method> expectedMethods = new HashSet<Method>(Arrays
                .asList(methods));
        if (expectedMethods.contains(Method.GET)) {
            expectedMethods.add(Method.HEAD);
        }
        final List<Method> allowedMethods = new ArrayList<Method>(
                optionsResponse.getAllowedMethods());
        for (final Method method : methods) {
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
        if (response.getEntity() != null) {
            assertEquals(null, response.getEntity().getText());
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
        expected = Converter.getMediaTypeWithoutParams(expected);
        actual = Converter.getMediaTypeWithoutParams(actual);
        assertEquals(expected, actual);
    }

    public static void assertEqualMediaType(MediaType expected,
            Representation actualEntity) {
        assertEqualMediaType(expected, actualEntity.getMediaType());
    }

    public static void assertEqualMediaType(MediaType expected,
            Response actualResponse) {
        assertEqualMediaType(expected, actualResponse.getEntity());
    }

    /**
     * @param accMediaType
     * @param mediaTypeQuality
     *            default is 1.
     * @return
     */
    public static Collection<Preference<MediaType>> createPrefColl(
            MediaType accMediaType, float mediaTypeQuality) {
        if (accMediaType == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(new Preference<MediaType>(accMediaType,
                mediaTypeQuality));
    }

    /**
     * Some features are not ready and should only tested on the workspace of
     * the JAX-RS implementor, and if the actual date is after a given date.
     * This method checks this.
     * 
     * Year is 2008
     * 
     * @param dayOfMonth
     *            1-31
     * @param month
     *            1-12
     * @return true, if this workspace seems not to be JAX-RS implementors
     *         workspace.
     */
    public static boolean jaxRxImplementorCheck(int dayOfMonth, int month) {
        return jaxRxImplementorCheck(dayOfMonth, month, 2008);
    }

    /**
     * @param dayOfMonth
     *            1-31
     * @param month
     *            1-12
     * @param year
     *            e.g. 2008
     * @return true, if this workspace seems not to be JAX-RS implementors
     *         workspace.
     * @see #jaxRxImplementorAndAfter(int, int)
     */
    public static boolean jaxRxImplementorCheck(int dayOfMonth, int month,
            int year) {
        final Date afterDate = new Date(year - 1900, month - 1, dayOfMonth);
        if (new Date().after(afterDate)) {
            final String userHome = System.getProperty("user.home");
            if (userHome == null) {
                return true;
            }
            if (userHome.equals("C:\\Dokumente und Einstellungen\\Stephan")) {
                final String javaClassPath = System
                        .getProperty("java.class.path");
                if (javaClassPath == null) {
                    return true;
                }
                if (javaClassPath.startsWith("D:\\eclipse-workspaces\\Mastera")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param httpMethod
     * @param klasse
     * @param mediaTypePrefs
     *            Collection with Preference&lt;MediaType&gt; and/or MediaType.
     * @return
     * @throws IllegalArgumentException
     *             If an element in the mediaTypes is neither a
     *             Preference&lt;MediaType&gt; or a MediaType object.
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
        final Reference reference = createReference(klasse, subPath);
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
        final Reference reference = createReference(klasse, subPath);
        final Request request = new Request(httpMethod, reference);
        if (conditions != null) {
            request.setConditions(conditions);
        }
        if (clientInfo != null) {
            request.setClientInfo(clientInfo);
        }
        return accessServer(request);
    }

    public Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, MediaType accMediaType) {
        Collection<MediaType> mediaTypes = null;
        if (accMediaType != null) {
            mediaTypes = Collections.singleton(accMediaType);
        }
        return accessServer(httpMethod, klasse, subPath, mediaTypes, null);
    }

    protected Application createApplication() {
        return createApplication(getAppConfig(), ChallengeScheme.HTTP_BASIC,
                null);
    }

    /**
     * Creates a {@link JaxRsApplication}
     * 
     * @param appConfig
     *            the applicationConfi to use
     * @param challengeScheme
     *            the challengeScheme to use, if a RoleChecker is given.
     * @param roleChecker
     *            the RoleChecer to use.
     * @return
     */
    public JaxRsApplication createApplication(ApplicationConfig appConfig,
            ChallengeScheme challengeScheme, RoleChecker roleChecker) {
        final JaxRsApplication application = new JaxRsApplication(new Context());
        if (roleChecker != null) {
            application.setRoleChecker(roleChecker);
            final Guard guard = createGuard(application.getContext(),
                    challengeScheme);
            application.setGuard(guard);
        }
        application.add(appConfig);
        return application;
    }

    /**
     * @param subPath
     * @return
     */
    protected Request createGetRequest(String subPath) {
        final Reference reference = createReference(getRootResourceClass(),
                subPath);
        return new Request(Method.GET, reference);
    }

    /**
     * Creates an reference that access the localhost with the JaxRsTester
     * protocol and the JaxRsTester Port. It uses the path of the given
     * jaxRsClass
     * 
     * @param jaxRsClass
     * @param subPath
     *            darf null sein
     * @return
     * @see #createReference(String, String)
     */
    public Reference createReference(Class<?> jaxRsClass, String subPath) {
        String path;
        try {
            path = Util.getPathTemplate(jaxRsClass);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
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
        final Reference reference = createBaseRef();
        reference.setBaseRef(createBaseRef());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (subPath != null) {
            if (subPath.startsWith(";")) {
                path += subPath;
            } else if (subPath.length() > 0) {
                if (path.endsWith("/") || subPath.startsWith("/")) {
                    path += subPath;
                } else {
                    path += "/" + subPath;
                }
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

    public Response get(MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClass(), null,
                accMediaType);
    }

    public Response get(Reference reference) {
        return accessServer(Method.GET, reference);
    }

    public Response get(Reference reference, MediaType mediaType) {
        Collection<MediaType> mediaTypes = null;
        if (mediaType != null) {
            mediaTypes = new ArrayList<MediaType>();
            mediaTypes.add(mediaType);
        }
        return accessServer(Method.GET, reference, mediaTypes, null, null,
                null, null, null);
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

    public Response get(String subPath, MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClass(), subPath,
                accMediaType);
    }

    /**
     * @return
     */
    protected ApplicationConfig getAppConfig() {
        final ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getProviderClasses() {
                return (Set) getProvClasses();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getResourceClasses() {
                return (Set) Collections.singleton(getRootResourceClass());
            }
        };
        return appConfig;
    }

    public Response getAuth(String subPath, String username, String pw) {
        return get(subPath, new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
                username, pw));
    }

    /**
     * @return
     * @see #getAppConfig()
     */
    protected Set<Class<?>> getProvClasses() {
        return Collections.emptySet();
    }

    protected Class<?> getRootResourceClass() {
        throw new UnsupportedOperationException(
                "You must implement the methods getRootResourceClass() or getAppConfig(). If you only implemented getAppConfig(), you can't use this method");
    }

    public Response getWithCookies(String subPath, Collection<Cookie> cookies) {
        return accessServer(Method.GET, createReference(getRootResourceClass(),
                subPath), null, null, null, null, cookies, null);
    }

    public Response getWithHeaders(String subPath, Collection<Parameter> headers) {
        return accessServer(Method.GET, createReference(getRootResourceClass(),
                subPath), null, null, null, null, null, headers);
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

    public Response post(String subPath, Representation entity) {
        return post(subPath, entity, null);
    }

    public Response post(String subPath, Representation entity,
            ChallengeResponse cr) {
        return accessServer(Method.POST, createReference(
                getRootResourceClass(), subPath), null, entity, cr, null, null,
                null);
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

    /**
     * 
     */
    protected void runServerAfterStart() {
        final ApplicationConfig appConfig = getAppConfig();
        final Collection<Class<?>> rrcs = appConfig.getResourceClasses();
        System.out
                .println("the root resource classes are available under the following pathes:");
        for (final Class<?> rrc : rrcs) {
            try {
                System.out.print("http://localhost:" + getServerPort());
                final String path = rrc.getAnnotation(Path.class).value();
                if (!path.startsWith("/")) {
                    System.out.print("/");
                }
                System.out.println(path);
            } catch (final RuntimeException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * starts the Server for the given JaxRsTestCase, waits for an input from
     * {@link System#in} and then stops the server.
     * 
     * @param jaxRsTestCase
     * @throws Exception
     */
    public void runServerUntilKeyPressed() throws Exception {
        setUseTcp(true);
        startServer(this.createApplication());
        runServerAfterStart();
        System.out.println("press key to stop . . .");
        System.in.read();
        stopServer();
        System.out.println("server stopped");
    }

    /**
     * @param protocol
     * @param challengeScheme
     * @param roleChecker
     *            the {@link RoleChecker} to use.
     * @param rootResourceClasses
     * @throws Exception
     */
    private void startServer(ApplicationConfig appConfig, Protocol protocol,
            final ChallengeScheme challengeScheme, RoleChecker roleChecker)
            throws Exception {
        final Application jaxRsApplication = createApplication(appConfig,
                challengeScheme, roleChecker);
        startServer(jaxRsApplication, protocol);
    }

    /**
     * @param roleChecker
     *            the {@link RoleChecker} to use.
     * @throws Exception
     */
    protected void startServer(ChallengeScheme challengeScheme,
            RoleChecker roleChecker) throws Exception {
        final ApplicationConfig appConfig = getAppConfig();
        startServer(appConfig, Protocol.HTTP, challengeScheme, roleChecker);
    }
}