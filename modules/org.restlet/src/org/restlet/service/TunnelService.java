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

package org.restlet.service;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.engine.application.TunnelFilter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.routing.Filter;

/**
 * Application service tunneling request method or client preferences. The
 * tunneling can use query parameters, file-like extensions and specific
 * headers. This is particularly useful for browser-based applications that
 * can't fully control the HTTP requests sent.<br>
 * <br>
 * Here is the list of the default parameter names supported:
 * <table>
 * <p>
 * <tr>
 * <th>Property</th>
 * <th>Default name</th>
 * <th>Value type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>methodParameter</td>
 * <td>method</td>
 * <td>See values in {@link org.restlet.data.Method}</td>
 * <td>For POST requests, let you specify the actual method to use (DELETE, PUT,
 * MOVE, etc.). For GET requests, let you specify OPTIONS as the actual method
 * to use.</td>
 * </tr>
 * <tr>
 * <td>characterSetParameter</td>
 * <td>charset</td>
 * <td>Use extension names defined in {@link MetadataService} or the full
 * character set name</td>
 * <td>For GET requests, replaces the accepted character set by the given value.
 * </td>
 * </tr>
 * <tr>
 * <td>encodingParameter</td>
 * <td>encoding</td>
 * <td>Use extension names defined in {@link MetadataService} or the full
 * encoding name</td>
 * <td>For GET requests, replaces the accepted encoding by the given value.</td>
 * </tr>
 * <tr>
 * <td>languageParameter</td>
 * <td>language</td>
 * <td>Use extension names defined in {@link MetadataService} or the full
 * language name</td>
 * <td>For GET requests, replaces the accepted language by the given value.</td>
 * </tr>
 * <tr>
 * <td>mediaTypeParameter</td>
 * <td>media</td>
 * <td>Use extension names defined in {@link MetadataService} or the full media
 * type name</td>
 * <td>For GET requests, replaces the accepted media type set by the given
 * value.</td>
 * </tr>
 * <tr>
 * <td>methodHeader</td>
 * <td>X-HTTP-Method-Override</td>
 * <td>Name of non-standard header. It is a good practice to prefix it with
 * "X-".</td>
 * <td>For POST requests, let you specify the actual method to use (DELETE, PUT,
 * MOVE, etc.).</td>
 * </tr>
 * </table>
 * <br>
 * The client preferences can also be updated according to the user agent
 * properties (its name, version, operating system, or other) available via the
 * {@link ClientInfo#getAgentAttributes()} method. Check the
 * {@link #isUserAgentTunnel()} method.<br>
 * <br>
 * The list of new media type preferences is loaded from a property file called
 * "accept.properties" located in the classpath in the sub directory
 * "org/restlet/service". This property file is composed of blocks of
 * properties. One "block" of properties starts either with the beginning of the
 * properties file or with the end of the previous block. One block ends with
 * the "acceptNew" property which contains the value of the new accept header.
 * Here is a sample block.<br>
 * 
 * <pre>
 * agentName: firefox
 * acceptOld: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
 * acceptNew: application/xhtml+xml,text/html,text/xml;q=0.9,application/xml;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
 * </pre>
 * 
 * Each declared property is a condition that must be filled in order to update
 * the client preferences. For example "agentName: firefox" expresses the fact
 * this block concerns only "firefox" clients. <br>
 * <br>
 * The "acceptOld" property allows to check the value of the current "Accept"
 * header. If it equals to the value of the "acceptOld" property or if the
 * "acceptOld" property is empty, then the preferences will be updated. This can
 * be useful for AJAX clients which looks like their browser (same agentName,
 * agentVersion, etc.) but can provide their own "Accept" header.
 * 
 * @author Jerome Louvel
 */
public class TunnelService extends Service {

    /** The name of the parameter containing the accepted character set. */
    private volatile String characterSetParameter;

    /** The name of the parameter containing the accepted encoding. */
    private volatile String encodingParameter;

    /**
     * Indicates if the client preferences can be tunneled via file-like
     * extensions.
     */
    private volatile boolean extensionsTunnel;

    /** Indicates if the method can be tunneled via the header. */
    private volatile boolean headersTunnel;

    /** The name of the parameter containing the accepted language. */
    private volatile String languageParameter;

    /** The name of the parameter containing the accepted media type. */
    private volatile String mediaTypeParameter;

    /** The name of the header that contains the method name. */
    private volatile String methodHeader;

    /** The name of the parameter containing the method name. */
    private volatile String methodParameter;

    /** Indicates if the method name can be tunneled. */
    private volatile boolean methodTunnel;

    /** Indicates if the client preferences can be tunneled. */
    private volatile boolean preferencesTunnel;

    /**
     * Indicates if the method and client preferences can be tunneled via query
     * parameters.
     */
    private volatile boolean queryTunnel;

    /**
     * Indicates if the client preferences can be tunneled via the user agent
     * string.
     */
    private volatile boolean userAgentTunnel;

    /**
     * Constructor that enables the query tunnel and disables the extensions and
     * user agent tunnels.
     * 
     * @param methodTunnel
     *            Indicates if the method name can be tunneled.
     * @param preferencesTunnel
     *            Indicates if the client preferences can be tunneled by query
     *            parameters or file-like extensions or user agent string.
     */
    public TunnelService(boolean methodTunnel, boolean preferencesTunnel) {
        this(true, methodTunnel, preferencesTunnel);
    }

    /**
     * Constructor that enables the query tunnel and disables the extensions and
     * user agent tunnels.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * @param methodTunnel
     *            Indicates if the method name can be tunneled.
     * @param preferencesTunnel
     *            Indicates if the client preferences can be tunneled by query
     *            parameters or file-like extensions or user agent string.
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel) {
        this(enabled, methodTunnel, preferencesTunnel, true, false);
    }

    /**
     * Constructor that disables the user agent tunnel.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * @param methodTunnel
     *            Indicates if the method can be tunneled using a query
     *            parameter.
     * @param preferencesTunnel
     *            Indicates if the client preferences can be tunneled using
     *            query parameters or file-like extensions or user agent string.
     * @param queryTunnel
     *            Indicates if tunneling can use query parameters.
     * @param extensionsTunnel
     *            Indicates if tunneling can use file-like extensions.
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel, boolean queryTunnel,
            boolean extensionsTunnel) {
        this(enabled, methodTunnel, preferencesTunnel, queryTunnel,
                extensionsTunnel, false);
    }

    /**
     * Constructor that enables the header tunneling.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * @param methodTunnel
     *            Indicates if the method can be tunneled using a query
     *            parameter.
     * @param preferencesTunnel
     *            Indicates if the client preferences can be tunneled using
     *            query parameters or file-like extensions or user agent string.
     * @param queryTunnel
     *            Indicates if tunneling can use query parameters.
     * @param extensionsTunnel
     *            Indicates if tunneling can use file-like extensions.
     * @param userAgentTunnel
     *            Indicates if tunneling can use user agent string.
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel, boolean queryTunnel,
            boolean extensionsTunnel, boolean userAgentTunnel) {
        this(enabled, methodTunnel, preferencesTunnel, queryTunnel,
                extensionsTunnel, userAgentTunnel, true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     * @param methodTunnel
     *            Indicates if the method can be tunneled using a query
     *            parameter.
     * @param preferencesTunnel
     *            Indicates if the client preferences can be tunneled using
     *            query parameters or file-like extensions or user agent string.
     * @param queryTunnel
     *            Indicates if tunneling can use query parameters.
     * @param extensionsTunnel
     *            Indicates if tunneling can use file-like extensions.
     * @param userAgentTunnel
     *            Indicates if tunneling can use user agent string.
     * @param headersTunnel
     *            Indicates if method can be tunneled via a specific header.
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel, boolean queryTunnel,
            boolean extensionsTunnel, boolean userAgentTunnel,
            boolean headersTunnel) {
        super(enabled);

        this.extensionsTunnel = extensionsTunnel;
        this.methodTunnel = methodTunnel;
        this.preferencesTunnel = preferencesTunnel;
        this.queryTunnel = queryTunnel;
        this.userAgentTunnel = userAgentTunnel;
        this.headersTunnel = headersTunnel;

        this.characterSetParameter = "charset";
        this.encodingParameter = "encoding";
        this.languageParameter = "language";
        this.mediaTypeParameter = "media";
        this.methodParameter = "method";
        this.methodHeader = HeaderConstants.HEADER_X_HTTP_METHOD_OVERRIDE;
    }

    /**
     * Indicates if the request from a given client can be tunneled. The default
     * implementation always return true. This could be customize to restrict
     * the usage of the tunnel service.
     * 
     * @param client
     *            The client to test.
     * @return True if the request from a given client can be tunneled.
     */
    public boolean allowClient(ClientInfo client) {
        return true;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new TunnelFilter(context);
    }

    /**
     * Returns the character set parameter name.
     * 
     * @return The character set parameter name.
     */
    public String getCharacterSetParameter() {
        return this.characterSetParameter;
    }

    /**
     * Returns the name of the parameter containing the accepted encoding.
     * 
     * @return The name of the parameter containing the accepted encoding.
     */
    public String getEncodingParameter() {
        return this.encodingParameter;
    }

    /**
     * Returns the name of the parameter containing the accepted language.
     * 
     * @return The name of the parameter containing the accepted language.
     */
    public String getLanguageParameter() {
        return this.languageParameter;
    }

    /**
     * Returns the name of the parameter containing the accepted media type.
     * 
     * @return The name of the parameter containing the accepted media type.
     */
    public String getMediaTypeParameter() {
        return this.mediaTypeParameter;
    }

    /**
     * Returns the name of the header containing the method name.
     * 
     * @return the name of the header containing the method name.
     */
    public String getMethodHeader() {
        return methodHeader;
    }

    /**
     * Returns the method parameter name.
     * 
     * @return The method parameter name.
     */
    public String getMethodParameter() {
        return this.methodParameter;
    }

    /**
     * Indicates if the client preferences can be tunneled via the extensions.
     * Returns false by default.
     * 
     * @return True if the client preferences can be tunneled via the extensions
     * @see Request#getOriginalRef()
     */
    public boolean isExtensionsTunnel() {
        return this.extensionsTunnel;
    }

    /**
     * Indicates if the method can be tunneled via the header. Returns true by
     * default.
     * 
     * @return True if the method can be tunneled via the header.
     */
    public boolean isHeadersTunnel() {
        return headersTunnel;
    }

    /**
     * Indicates if the method name can be tunneled. Returns true by default.
     * 
     * @return True if the method name can be tunneled.
     */
    public boolean isMethodTunnel() {
        return this.methodTunnel;
    }

    /**
     * Indicates if the client preferences can be tunneled via the query
     * parameters or via file extensions. Returns true by default.
     * 
     * @return True if the client preferences can be tunneled.
     */
    public boolean isPreferencesTunnel() {
        return this.preferencesTunnel;
    }

    /**
     * Indicates if the method and client preferences can be tunneled via query
     * parameters or file extensions. Returns true by default.
     * 
     * @return True if the method and client preferences can be tunneled.
     */
    public boolean isQueryTunnel() {
        return this.queryTunnel;
    }

    /**
     * Indicates if the client preferences can be tunneled according to the user
     * agent. Returns false by default.
     * 
     * @return True if the client preferences can be tunneled according to the
     *         user agent.
     */
    public boolean isUserAgentTunnel() {
        return this.userAgentTunnel;
    }

    /**
     * Sets the character set parameter name.
     * 
     * @param parameterName
     *            The character set parameter name.
     */
    public void setCharacterSetParameter(String parameterName) {
        this.characterSetParameter = parameterName;
    }

    /**
     * Sets the name of the parameter containing the accepted encoding.
     * 
     * @param parameterName
     *            The name of the parameter containing the accepted encoding.
     */
    public void setEncodingParameter(String parameterName) {
        this.encodingParameter = parameterName;
    }

    /**
     * Indicates if the client preferences can be tunneled via the extensions.
     * 
     * @param extensionTunnel
     *            True if the client preferences can be tunneled via the
     *            extensions.
     * @see Request#getOriginalRef()
     */
    public void setExtensionsTunnel(boolean extensionTunnel) {
        this.extensionsTunnel = extensionTunnel;
    }

    /**
     * Indicates if the method can be tunneled via the header.
     * 
     * @param headersTunnel
     *            True if the method can be tunneled via the header.
     */
    public void setHeadersTunnel(boolean headersTunnel) {
        this.headersTunnel = headersTunnel;
    }

    /**
     * Sets the name of the parameter containing the accepted language.
     * 
     * @param parameterName
     *            The name of the parameter containing the accepted language.
     */
    public void setLanguageParameter(String parameterName) {
        this.languageParameter = parameterName;
    }

    /**
     * Sets the name of the parameter containing the accepted media type.
     * 
     * @param parameterName
     *            The name of the parameter containing the accepted media type.
     */
    public void setMediaTypeParameter(String parameterName) {
        this.mediaTypeParameter = parameterName;
    }

    /**
     * Sets the name of the header containing the method name.
     * 
     * @param methodHeader
     *            The name of the header containing the method name.
     */
    public void setMethodHeader(String methodHeader) {
        this.methodHeader = methodHeader;
    }

    /**
     * Sets the method parameter name.
     * 
     * @param parameterName
     *            The method parameter name.
     */
    public void setMethodParameter(String parameterName) {
        this.methodParameter = parameterName;
    }

    /**
     * Indicates if the method name can be tunneled.
     * 
     * @param methodTunnel
     *            True if the method name can be tunneled.
     */
    public void setMethodTunnel(boolean methodTunnel) {
        this.methodTunnel = methodTunnel;
    }

    /**
     * Indicates if the client preferences can be tunneled via the query
     * parameters.
     * 
     * @param preferencesTunnel
     *            True if the client preferences can be tunneled via the query
     *            parameters.
     */
    public void setPreferencesTunnel(boolean preferencesTunnel) {
        this.preferencesTunnel = preferencesTunnel;
    }

    /**
     * Indicates if the method and client preferences can be tunneled via query
     * parameters.
     * 
     * @param queryTunnel
     *            True if the method and client preferences can be tunneled via
     *            query parameters.
     */
    public void setQueryTunnel(boolean queryTunnel) {
        this.queryTunnel = queryTunnel;
    }

    /**
     * Indicates if the client preferences can be tunneled according to the user
     * agent.
     * 
     * @param userAgentTunnel
     *            True if the client preferences can be tunneled according to
     *            the user agent.
     */
    public void setUserAgentTunnel(boolean userAgentTunnel) {
        this.userAgentTunnel = userAgentTunnel;
    }
}
