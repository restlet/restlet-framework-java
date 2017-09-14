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

package org.restlet.test.ext.jaxrs.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MemoryRealm;
import org.restlet.security.User;
import org.restlet.util.Series;
import org.restlet.util.WrapperRepresentation;

/**
 * <p>
 * This JUnit {@link TestCase} subclass could be used to test
 * {@link Application}s. It allows switching between real TCP server access (via
 * localhost) or direct application access.<br>
 * Set {@link #useTcp} via {@link #setUseTcp(boolean)} to
 * <ul>
 * <li>true to use real TCP to access the server.</li>
 * <li>false to access the {@link Application} directly without TCP attach (very
 * fast, but nearly the same effect for testing).</li>
 * </ul>
 * That's all you need to switch between real TCP access to the server or direct
 * {@link Application} access.
 * </p>
 * <p>
 * Because the response is not deserialized, perhaps something of the request is
 * not real enough, especially for HEAD requests, because the entity content is
 * perhaps available. If you need to know if the request was with tcp or
 * without, you can use {@link #shouldAccessWithoutTcp()} (or also
 * {@link #shouldStartServerInSetUp()}).
 * </p>
 * 
 * @author Stephan Koops
 */
public abstract class RestletServerTestCase extends TestCase {

    /**
     * ServerWrapperFactory to use. Default: {@link RestletServerWrapperFactory}
     */
    private static ServerWrapperFactory serverWrapperFactory;

    /**
     * if true, a real server is started and all communication uses real TCP,
     * real Restlet request and response serialization. If false, the
     * application is called without serialization.<br>
     * The first is real, the last is very fast.
     * 
     * @see #setServerWrapperFactory(ServerWrapperFactory)
     */
    private static boolean usingTcp = false;

    /**
     * Adds the given media types to the accepted media types.
     * 
     * @param request
     *            a Restlet {@link Request}
     * @param mediaTypes
     *            a collection of {@link MediaType}s or {@link Preference}<
     *            {@link MediaType}>; mixing is also allowed.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void addAcceptedMediaTypes(Request request, Collection mediaTypes) {
        if ((mediaTypes == null) || mediaTypes.isEmpty()) {
            return;
        }
        final Collection<Preference<MediaType>> mediaTypePrefs = new ArrayList<Preference<MediaType>>(
                mediaTypes.size());
        for (final Object mediaType : mediaTypes) {
            if (mediaType instanceof MediaType) {
                mediaTypePrefs.add(new Preference<MediaType>(
                        (MediaType) mediaType));
                continue;
            }
            if (mediaType instanceof Preference) {
                final Preference<Metadata> preference = (Preference) mediaType;
                if (preference.getMetadata() instanceof MediaType) {
                    mediaTypePrefs.add((Preference) preference);
                    continue;
                }
            }
            throw new IllegalArgumentException(
                    "Valid mediaTypes are only Preference<MediaType> or MediaType");
        }
        request.getClientInfo().getAcceptedMediaTypes().addAll(mediaTypePrefs);
    }

    /**
     * creates a Guard, that could be used for testing.
     * 
     * @param context
     * @param challengeScheme
     * @return
     */
    public static ChallengeAuthenticator createAuthenticator(
            final Context context, final ChallengeScheme challengeScheme) {
        MemoryRealm realm = new MemoryRealm();

        realm.getUsers().add(new User("admin", "adminPW".toCharArray()));
        realm.getUsers().add(new User("alice", "alicesSecret".toCharArray()));
        realm.getUsers().add(new User("bob", "bobsSecret".toCharArray()));

        context.setDefaultEnroler(realm.getEnroler());
        context.setDefaultVerifier(realm.getVerifier());

        return new ChallengeAuthenticator(context, challengeScheme, "");
    }

    /**
     * Returns the HTTP headers of the Restlet {@link Request} as {@link Form}.
     * 
     * @param request
     * @return Returns the HTTP headers of the Request.
     */
    public static Series<Header> getHttpHeaders(Request request) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) request.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);

        if (headers == null) {
            headers = new Series<Header>(Header.class);
            request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    headers);
        }

        return headers;
    }

    public static ServerWrapperFactory getServerWrapperFactory() {
        if (serverWrapperFactory == null) {
            if (usingTcp) {
                serverWrapperFactory = new RestletServerWrapperFactory();
            } else {
                serverWrapperFactory = new DirectServerWrapperFactory();
            }
        }
        return serverWrapperFactory;
    }

    /**
     * Sets the default ServerWrapper. Should be called before setUp.
     * 
     * @param newServerWrapper
     */
    public static void setServerWrapperFactory(ServerWrapperFactory swf) {
        if (swf == null) {
            throw new IllegalArgumentException(
                    "null is an illegal ServerWrapperFactory");
        }
        serverWrapperFactory = swf;
    }

    /**
     * @param usingTcp
     *            the useTcp to set
     */
    public static void setUseTcp(boolean usingTcp) {
        if (usingTcp) {
            if ((serverWrapperFactory != null)
                    && !serverWrapperFactory.usesTcp()) {
                serverWrapperFactory = null;
            }
        } else {
            if ((serverWrapperFactory != null)
                    && serverWrapperFactory.usesTcp()) {
                serverWrapperFactory = null;
            }
        }
        RestletServerTestCase.usingTcp = usingTcp;
    }

    /**
     * @param response
     */
    public static void sysOutEntity(Response response) {
        final Representation entity = response.getEntity();
        try {
            if (entity != null) {
                System.out.println(entity.getText());
            } else {
                System.out.println("[no Entity available]");
            }
        } catch (IOException e) {
            System.out.println("Entity not readable: ");
            e.printStackTrace(System.out);
        }
    }

    /**
     * Utility method: Prints the entity to System.out, if the status indicates
     * an error.
     * 
     * @param response
     * @throws IOException
     */
    public static void sysOutEntityIfError(Response response) {
        if (response.getStatus().isError()) {
            sysOutEntity(response);
        }
    }

    /**
     * @param status
     * @param response
     */
    public static void sysOutEntityIfNotStatus(Status status, Response response) {
        if (!response.getStatus().equals(status)) {
            sysOutEntity(response);
        }
    }

    /**
     * @return the useTcp
     */
    public static boolean usesTcp() {
        return usingTcp;
    }

    /**
     * ServerWrapper to use.
     */
    protected ServerWrapper serverWrapper;

    /**
     * 
     */
    public RestletServerTestCase() {
        super();
    }

    /**
     * @param name
     */
    public RestletServerTestCase(String name) {
        super(name);
    }

    public Response accessServer(Method httpMethod, Reference reference) {
        return accessServer(httpMethod, reference, null, null, null, null,
                null, null);
    }

    /**
     * access the server with the given values.
     * 
     * @param httpMethod
     *            The HTTP method to use.
     * @param reference
     *            The {@link Reference}
     * @param accMediaTypes
     *            the accepted {@link MediaType}s and/or {@link Preference}<
     *            {@link MediaType}> (may be mixed). May be null or empty.
     * @param entity
     *            the entity to send. null for GET and DELETE requests
     * @param challengeResponse
     * @param conditions
     *            the conditions to send with the request. May be null.
     * @param addCookies
     *            {@link Cookie}s to add to the {@link Request}. May be null.
     * @param addHeaders
     *            headers to add to the request. May be null.
     * @return
     * @see #accessServer(Request)
     * @see #accessServer(Method, Reference)
     */
    @SuppressWarnings("rawtypes")
    public Response accessServer(Method httpMethod, Reference reference,
            Collection accMediaTypes, Representation entity,
            ChallengeResponse challengeResponse, Conditions conditions,
            Collection<Cookie> addCookies, Collection<Header> addHeaders) {
        Request request = new Request(httpMethod, reference);
        addAcceptedMediaTypes(request, accMediaTypes);
        request.setChallengeResponse(challengeResponse);
        request.setEntity(entity);
        request.setConditions(conditions);

        if (addCookies != null) {
            request.getCookies().addAll(addCookies);
        }

        if (addHeaders != null) {
            getHttpHeaders(request).addAll(addHeaders);
        }

        return accessServer(request);
    }

    /**
     * @param request
     * @return
     * @see #accessServer(Method, Reference)
     * @see #accessServer(Method, Reference, Collection, Representation,
     *      ChallengeResponse, Conditions, Collection, Collection)
     */
    public Response accessServer(Request request) {
        final Reference reference = request.getResourceRef();

        if (reference.getBaseRef() == null) {
            reference.setBaseRef(reference.getHostIdentifier());
        }
        request.setOriginalRef(reference.getTargetRef());
        final Restlet connector = getClientConnector();

        if (shouldAccessWithoutTcp()) {
            final String hostDomain = request.getResourceRef().getHostDomain();
            getHttpHeaders(request).add("host", hostDomain);
        }

        Response response = new Response(request);
        connector.handle(request, response);

        if (!usingTcp && request.getMethod().equals(Method.HEAD)) {
            response.setEntity(new WrapperRepresentation(response.getEntity()) {

                @Override
                public ReadableByteChannel getChannel() throws IOException {
                    return IoUtils.getChannel(getStream());
                }

                @Override
                public Reader getReader() throws IOException {
                    return new StringReader("");
                }

                @Override
                public InputStream getStream() throws IOException {
                    return new ByteArrayInputStream(new byte[0]);
                }

                @Override
                public String getText() {
                    return null;
                }

                @Override
                public boolean isAvailable() {
                    return false;
                }

                @Override
                public void write(OutputStream outputStream) throws IOException {
                }

                @Override
                public void write(WritableByteChannel writableChannel)
                        throws IOException {
                }

                @Override
                public void write(Writer writer) throws IOException {
                }
            });
        }
        return response;
    }

    /**
     * Creates the application and returns it. You can define other abstract
     * methods for this.
     */
    protected abstract Application createApplication();

    protected Reference createBaseRef() {
        final Reference reference = new Reference();
        reference.setProtocol(Protocol.HTTP);
        reference.setAuthority("localhost");
        if (!shouldAccessWithoutTcp()) {
            reference.setHostPort(getServerWrapper().getServerPort());
        }
        return reference;
    }

    /**
     * @return
     */
    private Restlet getClientConnector() {
        return getServerWrapper().getClientConnector();
    }

    public int getServerPort() {
        return getServerWrapper().getServerPort();
    }

    public ServerWrapper getServerWrapper() {
        if (this.serverWrapper == null) {
            this.serverWrapper = getServerWrapperFactory()
                    .createServerWrapper();
        }
        return this.serverWrapper;
    }

    /**
     * This methods shows information about the started server after starting
     * it.<br>
     * You may override this method to do what ever you want
     */
    protected void runServerAfterStart() {
        System.out.print("server is accessable via http://localhost:");
        System.out.println(getServerPort());
    }

    /**
     * <p>
     * Starts the current test case as a normal HTTP server (sets
     * {@link #useTcp} to true), waits for an input from {@link System#in} and
     * then stops the server.<br>
     * After startup the method {@link #runServerAfterStart()} is called; you
     * may override it to give more information about the startet server.
     * </p>
     * <p>
     * This method is easy to use. Just instantiate the unit test case class and
     * call this method, e.g. in the main method.
     * </p>
     */
    public void runServerUntilKeyPressed() throws Exception {
        setUseTcp(true);
        startServer(createApplication());
        runServerAfterStart();
        System.out.println("press key to stop . . .");
        System.in.read();
        stopServer();
        System.out.println("server stopped");
    }

    public void setServerWrapper(ServerWrapper serverWrapper) {
        this.serverWrapper = serverWrapper;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Engine.clearThreadLocalVariables();

        if (shouldStartServerInSetUp()) {
            startServer(createApplication());
        }
    }

    /**
     * @return
     */
    public boolean shouldAccessWithoutTcp() {
        return getServerWrapper() instanceof DirectServerWrapper;
    }

    public boolean shouldStartServerInSetUp() {
        return true;
    }

    /**
     * @param application
     * @throws Exception
     */
    public void startServer() throws Exception {
        startServer(createApplication());
    }

    /**
     * @param application
     * @throws Exception
     */
    public void startServer(Application application) throws Exception {
        startServer(application, Protocol.HTTP);
    }

    /**
     * @param jaxRsApplication
     * @param protocol
     * @throws Exception
     */
    protected void startServer(Application jaxRsApplication, Protocol protocol)
            throws Exception {
        try {
            getServerWrapper().startServer(jaxRsApplication, protocol);
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
     * @see {@link #accessServer(Request)}
     */
    protected void stopServer() throws Exception {
        if (this.serverWrapper != null) {
            this.serverWrapper.stopServer();
        }
        this.serverWrapper = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stopServer();
        Engine.clearThreadLocalVariables();
    }

}
