/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

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
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.server.RestletServerTestCase;
import org.restlet.test.jaxrs.util.OrderedReadonlySet;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relative to the
 * path of the root resource class.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public abstract class JaxRsTestCase extends RestletServerTestCase {

    /**
     * Checks, if the allowed methods of an OPTIONS {@link Response} are the
     * same as the expected.
     * 
     * @param optionsResponse
     * @param methods
     *            The methods that must be allowed. If GET is included, a check
     *            for HEAD is automatically done. But it is no problem to add
     *            the HEAD method in the parameters.
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
     * Checks, if the entity of the Response is null or empty.
     * 
     * @param response
     * @throws IOException
     */
    public static void assertEmptyEntity(Response response) throws IOException {
        if (response.getEntity() != null) {
            assertEquals(null, response.getEntity().getText());
        }
    }

    /**
     * Check, if the mainType and the subType of the actual MediaType is as
     * expected. The parameters are ignored.
     */
    public static void assertEqualMediaType(MediaType expected, MediaType actual) {
        expected = Converter.getMediaTypeWithoutParams(expected);
        actual = Converter.getMediaTypeWithoutParams(actual);
        assertEquals(expected, actual);
    }

    /**
     * Check, if the mainType and the subType of the MediaType of the given
     * entity is as expected. The parameters of the MediaTypes are ignored.
     */
    public static void assertEqualMediaType(MediaType expected,
            Representation actualEntity) {
        assertEqualMediaType(expected, actualEntity.getMediaType());
    }

    /**
     * Check, if the mainType and the subType of the MediaType of the entity of
     * the given Response is as expected. The parameters of the MediaTypes are
     * ignored.
     */
    public static void assertEqualMediaType(MediaType expected,
            Response actualResponse) {
        assertEqualMediaType(expected, actualResponse.getEntity());
    }

    /**
     * Creates a singleton Collection with the given MediaType as Preference.
     * 
     * @param accMediaType
     * @param mediaTypeQuality
     *            the default value is 1.
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
     * Sends a request to the first resource for this test case with the given
     * HTTP method.
     */
    public Response accessServer(Method httpMethod) {
        return accessServer(httpMethod, getRootResourceClassFromAppConf(), null);
    }

    /**
     * Sends a request to the given root resource with the given HTTP method and
     * the given accepted media types
     * 
     * @param httpMethod
     * @param klasse
     * @param acceptedMediaTypes
     *            Collection with {@link Preference}&lt;{@link MediaType}&gt;
     *            and/or {@link MediaType}s, also mixed.
     * @throws IllegalArgumentException
     *             If an element in the mediaTypes is neither a
     *             Preference&lt;MediaType&gt; or a MediaType object.
     */
    @SuppressWarnings("unchecked")
    public Response accessServer(Method httpMethod, Class<?> klasse,
            Collection acceptedMediaTypes) throws IllegalArgumentException {
        return accessServer(httpMethod, klasse, null, acceptedMediaTypes, null);
    }

    /**
     * Sends a request to the given sub path of the given root resource with the
     * given HTTP method and the given accepted media types and the given
     * {@link ChallengeResponse}.
     * 
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param accMediaTypes
     *            Collection with {@link Preference}&lt;{@link MediaType}&gt;
     *            and/or {@link MediaType}s, also mixed.
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
     * Sends a request to the given sub path of the given root resource with the
     * given HTTP method, the given {@link Conditions} and the given
     * {@link ClientInfo}.
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

    /**
     * Sends a request to the given sub path of the given root resource with the
     * given HTTP method and the given acceptable media type.
     * 
     * @param httpMethod
     * @param klasse
     * @param subPath
     * @param accMediaType
     *            the acceptable MediaType.
     * @param challengeResponse
     */
    public Response accessServer(Method httpMethod, Class<?> klasse,
            String subPath, MediaType accMediaType) {
        Collection<MediaType> mediaTypes = null;
        if (accMediaType != null) {
            mediaTypes = Collections.singleton(accMediaType);
        }
        return accessServer(httpMethod, klasse, subPath, mediaTypes, null);
    }

    protected org.restlet.Application createApplication() {
        return createApplication(getApplication(), ChallengeScheme.HTTP_BASIC,
                null);
    }

    /**
     * Creates a {@link JaxRsApplication}. Not needed for the test case
     * developer by default.
     * 
     * @param appConfig
     *            the applicationConfi to use
     * @param challengeScheme
     *            the challengeScheme to use, if a RoleChecker is given.
     * @param roleChecker
     *            the RoleChecer to use.
     * @return
     */
    protected JaxRsApplication createApplication(Application appConfig,
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
        final Reference reference = createReference(
                getRootResourceClassFromAppConf(), subPath);
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
            path = Util.getPathTemplateWithoutRegExps(jaxRsClass);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return createReference(path, subPath);
    }

    /**
     * Creates a Reference relative to the main resource class.
     * 
     * @param subPath
     * @return
     */
    public Reference createReference(String subPath) {
        return createReference(getRootResourceClassFromAppConf(), subPath);
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
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                null, null);
    }

    public Response get(Cookie cookie) {
        return get(null, cookie);
    }

    public Response get(MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                null, accMediaType);
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
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                subPath, null);
    }

    public Response get(String subPath, ChallengeResponse cr) {
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                subPath, null, cr);
    }

    public Response get(String subPath, ClientInfo clientInfo) {
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                subPath, null, clientInfo);
    }

    public Response get(String subPath, Conditions conditions) {
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                subPath, conditions, null);
    }

    public Response get(String subPath, Cookie cookie) {
        return accessServer(Method.GET, createReference(
                getRootResourceClassFromAppConf(), subPath), null, null, null,
                null, TestUtils.createList(cookie), null);
    }

    public Response get(String subPath, MediaType accMediaType) {
        return accessServer(Method.GET, getRootResourceClassFromAppConf(),
                subPath, accMediaType);
    }

    /**
     * In the implementation of this method the test case developer returns the
     * JAX-RS {@link Application} containing the root resource classes and the
     * providers.
     * 
     * @see Application
     */
    protected abstract Application getApplication();

    /**
     * Sends a GET request to the given subpath of the first root resource
     * class, authenticated with HTTP_BASIC with the given username and password
     */
    public Response getAuth(String subPath, String username, String password) {
        return get(subPath, new ChallengeResponse(ChallengeScheme.HTTP_BASIC,
                username, password));
    }

    /**
     * @return the first (by the {@link Iterator} of the {@link Set}) root
     *         resource class from the JAX-RS {@link Application} of the test
     *         case. First the classes of the Application are checked, then the
     *         singletons. To ensure that a defined class is the first in the
     *         Set, you could use the class {@link OrderedReadonlySet}.
     * @see #getApplication()
     * @throws IllegalStateException
     *             if now root resource class was found.
     */
    private Class<?> getRootResourceClassFromAppConf()
            throws IllegalStateException {
        Set<Class<?>> classes = getApplication().getClasses();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Path.class))
                return clazz;
        }
        Set<Object> singletons = getApplication().getSingletons();
        for (Object singleton : singletons) {
            final Class<? extends Object> clazz = singleton.getClass();
            if (clazz.isAnnotationPresent(Path.class))
                return clazz;
        }
        throw new IllegalStateException("Sorry, no root resource class found");
    }

    /**
     * Sends a request to the given sub path of the first root resource class
     * with the given {@link Cookie}s.
     */
    public Response getWithCookies(String subPath, Collection<Cookie> cookies) {
        return accessServer(Method.GET, createReference(
                getRootResourceClassFromAppConf(), subPath), null, null, null,
                null, cookies, null);
    }

    /**
     * Sends a request to the given sub path of the first root resource class
     * with the given headers.
     */
    public Response getWithHeaders(String subPath, Collection<Parameter> headers) {
        return accessServer(Method.GET, createReference(
                getRootResourceClassFromAppConf(), subPath), null, null, null,
                null, null, headers);
    }

    /**
     * Sends a HEAD request to the first root resource class, with the given
     * accepted {@link MediaType}.
     */
    public Response head(String subPath, MediaType accMediaType) {
        return accessServer(Method.HEAD, getRootResourceClassFromAppConf(),
                subPath, accMediaType);
    }

    /**
     * Sends an OPTION request to the main resource first root resource class.
     * 
     * @see #accessServer(Method, Class, String, Collection, ChallengeResponse)
     */
    public Response options() {
        return accessServer(Method.OPTIONS, getRootResourceClassFromAppConf(),
                null, null);
    }

    /**
     * Sends an OPTION request to the given sub path of the first root resource
     * class.
     * 
     * @see #accessServer(Method, Class, String, Collection, ChallengeResponse)
     */
    public Response options(String subPath) {
        return accessServer(Method.OPTIONS, getRootResourceClassFromAppConf(),
                subPath, null);
    }

    public Response post(Representation entity) {
        return post(null, entity, null, null);
    }

    public Response post(String subPath, MediaType mediaType) {
        return accessServer(Method.POST, getRootResourceClassFromAppConf(),
                subPath, mediaType);
    }

    public Response post(String subPath, Representation entity) {
        return post(subPath, entity, null);
    }

    public Response post(String subPath, Representation entity,
            ChallengeResponse cr) {
        return accessServer(Method.POST, createReference(
                getRootResourceClassFromAppConf(), subPath), null, entity, cr,
                null, null, null);
    }

    public Response post(String subPath, Representation entity,
            Collection accMediaTypes, ChallengeResponse challengeResponse) {
        return accessServer(Method.POST, createReference(
                getRootResourceClassFromAppConf(), subPath), accMediaTypes,
                entity, challengeResponse, null, null, null);
    }

    public Response put(String subPath, Representation entity) {
        return put(subPath, entity, null);
    }

    public Response put(String subPath, Representation entity,
            Conditions conditions) {
        return accessServer(Method.PUT, createReference(
                getRootResourceClassFromAppConf(), subPath), null, entity,
                null, conditions, null, null);
    }

    /**
     * This method is called, if the server is started.
     */
    protected void runServerAfterStart() {
        final Application appConfig = getApplication();
        final Collection<Class<?>> rrcs = appConfig.getClasses();
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
     * Starts the Server for the given JaxRsTestCase, waits for an input from
     * {@link System#in} and then stops the server.
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
     * @param application the JAX-RS {@link Application}.
     * @param protocol
     * @param challengeScheme
     * @param roleChecker
     *            the {@link RoleChecker} to use.
     * @throws Exception
     */
    private void startServer(Application application, Protocol protocol,
            final ChallengeScheme challengeScheme, RoleChecker roleChecker)
            throws Exception {
        final org.restlet.Application jaxRsApplication = createApplication(
                application, challengeScheme, roleChecker);
        startServer(jaxRsApplication, protocol);
    }

    /**
     * @see #startServer(Application, Protocol, ChallengeScheme, RoleChecker)
     */
    protected void startServer(ChallengeScheme challengeScheme,
            RoleChecker roleChecker) throws Exception {
        final Application appConfig = getApplication();
        startServer(appConfig, Protocol.HTTP, challengeScheme, roleChecker);
    }
}