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

package org.restlet.service;

import org.restlet.data.ClientInfo;
import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * Service tunneling request method or client preferences. The tunneling can use
 * query parameters and file-like extensions.
 * <p>
 * This is particularly useful for browser-based applications that can't fully
 * control the HTTP requests sent.
 * <p>
 * Here is the list of the default parameter names supported: <table>
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
 * <td>For POST requests, specify the actual method to use (DELETE, PUT, etc.).</td>
 * </tr>
 * <tr>
 * <td>characterSetParameter</td>
 * <td>charset</td>
 * <td>Use extension names defined in {@link MetadataService}</td>
 * <td>For GET requests, replaces the accepted character set by the given
 * value.</td>
 * </tr>
 * <tr>
 * <td>encodingParameter</td>
 * <td>encoding</td>
 * <td>Use extension names defined in {@link MetadataService}</td>
 * <td>For GET requests, replaces the accepted encoding by the given value.</td>
 * </tr>
 * <tr>
 * <td>languageParameter</td>
 * <td>language</td>
 * <td>Use extension names defined in {@link MetadataService}</td>
 * <td>For GET requests, replaces the accepted language by the given value.</td>
 * </tr>
 * <tr>
 * <td>mediaTypeParameter</td>
 * <td>media</td>
 * <td>Use extension names defined in {@link MetadataService}</td>
 * <td>For GET requests, replaces the accepted media type set by the given
 * value.</td>
 * </tr>
 * </table>
 * <p>
 * The client preferences can also be updated based on the extensions available
 * in the last path segment. The syntax is similar to file extensions by allows
 * several extensions to be present, in any particular order: e.g.
 * "/path/foo.fr.txt"). This mechanism relies on the mapping between an
 * extension and a metadata (e.g. "txt" => "text/plain") declared by the
 * {@link MetadataService}.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TunnelService {
    /**
     * Attribute name used to store the original resource URI in case of
     * modification by the tunnel service (query string or file extensions).
     * <p>
     * In case of modification (only), the ({@link Request#getAttributes()})
     * contains the attribute name as a key and the original resource URI as a
     * {@link Reference} value.
     */
    public static final String ATTRIBUTE_ORIGINAL_REF = "org.restlet.TunnelService.originalRef";

    /** The name of the parameter containing the accepted character set. */
    private volatile String characterSetParameter;

    /** Indicates if the service has been enabled. */
    private volatile boolean enabled;

    /** The name of the parameter containing the accepted encoding. */
    private volatile String encodingParameter;

    /**
     * Indicates if the client preferences can be tunnelled via file-like
     * extensions.
     */
    private volatile boolean extensionsTunnel;

    /** The name of the parameter containing the accepted language. */
    private volatile String languageParameter;

    /** The name of the parameter containing the accepted media type. */
    private volatile String mediaTypeParameter;

    /** The name of the parameter containing the method name. */
    private volatile String methodParameter;

    /** Indicates if the method name can be tunnelled. */
    private volatile boolean methodTunnel;

    /** Indicates if the client preferences can be tunnelled. */
    private volatile boolean preferencesTunnel;

    /**
     * Indicates if the method and client preferences can be tunnelled via query
     * parameters.
     */
    private volatile boolean queryTunnel;

    /**
     * Constructor.
     * 
     * @param enabled
     *                True if the service has been enabled.
     * @param methodTunnel
     *                Indicates if the method name can be tunnelled.
     * @param preferencesTunnel
     *                Indicates if the client preferences for a GET request can
     *                be tunnelled by query parameters and file "extensions".
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel) {
        this(enabled, methodTunnel, preferencesTunnel, true, true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *                True if the service has been enabled.
     * @param methodTunnel
     *                Indicates if the method can be tunnelled using a query
     *                parameter.
     * @param preferencesTunnel
     *                Indicates if the client preferences can be tunnelled using
     *                query parameters or file-like extensions.
     * @param queryTunnel
     *                Indicates if tunneling can use query parameters.
     * @param extensionsTunnel
     *                Indicates if tunneling can use file-like extensions.
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel, boolean queryTunnel,
            boolean extensionsTunnel) {
        this.enabled = enabled;

        this.extensionsTunnel = extensionsTunnel;
        this.methodTunnel = methodTunnel;
        this.preferencesTunnel = preferencesTunnel;
        this.queryTunnel = queryTunnel;

        this.characterSetParameter = "charset";
        this.encodingParameter = "encoding";
        this.languageParameter = "language";
        this.mediaTypeParameter = "media";
        this.methodParameter = "method";
    }

    /**
     * Indicates if the request from a given client can be tunnelled. The
     * default implementation always return true. This could be customize to
     * restrict the usage of the tunnel service.
     * 
     * @param client
     *                The client to test.
     * @return True if the request from a given client can be tunnelled.
     */
    public boolean allowClient(@SuppressWarnings("unused")
    ClientInfo client) {
        return true;
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
     * Returns the method parameter name.
     * 
     * @return The method parameter name.
     */
    public String getMethodParameter() {
        return this.methodParameter;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @return True if the service should be enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Indicates if the client preferences can be tunnelled via the extensions.
     * 
     * @return True if the client preferences can be tunnelled via the
     *         extensions
     * @see #ATTRIBUTE_ORIGINAL_REF
     */
    public boolean isExtensionsTunnel() {
        return this.extensionsTunnel;
    }

    /**
     * Indicates if the method name can be tunnelled.
     * 
     * @return True if the method name can be tunnelled.
     */
    public boolean isMethodTunnel() {
        return this.methodTunnel;
    }

    /**
     * Indicates if the client preferences can be tunnelled via the query
     * parameters.
     * 
     * @return True if the client preferences can be tunnelled via the query
     *         parameters.
     */
    public boolean isPreferencesTunnel() {
        return this.preferencesTunnel;
    }

    /**
     * Indicates if the method and client preferences can be tunnelled via query
     * parameters.
     * 
     * @return True if the method and client preferences can be tunnelled via
     *         query parameters.
     */
    public boolean isQueryTunnel() {
        return queryTunnel;
    }

    /**
     * Sets the character set parameter name.
     * 
     * @param parameterName
     *                The character set parameter name.
     */
    public void setCharacterSetParameter(String parameterName) {
        this.characterSetParameter = parameterName;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @param enabled
     *                True if the service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the name of the parameter containing the accepted encoding.
     * 
     * @param parameterName
     *                The name of the parameter containing the accepted
     *                encoding.
     */
    public void setEncodingParameter(String parameterName) {
        this.encodingParameter = parameterName;
    }

    /**
     * Indicates if the client preferences can be tunnelled via the extensions.
     * 
     * @param extensionTunnel
     *                True if the client preferences can be tunnelled via the
     *                extensions.
     * @see #ATTRIBUTE_ORIGINAL_REF
     */
    public void setExtensionsTunnel(boolean extensionTunnel) {
        this.extensionsTunnel = extensionTunnel;
    }

    /**
     * Sets the name of the parameter containing the accepted language.
     * 
     * @param parameterName
     *                The name of the parameter containing the accepted
     *                language.
     */
    public void setLanguageParameter(String parameterName) {
        this.languageParameter = parameterName;
    }

    /**
     * Sets the name of the parameter containing the accepted media type.
     * 
     * @param parameterName
     *                The name of the parameter containing the accepted media
     *                type.
     */
    public void setMediaTypeParameter(String parameterName) {
        this.mediaTypeParameter = parameterName;
    }

    /**
     * Sets the method parameter name.
     * 
     * @param parameterName
     *                The method parameter name.
     */
    public void setMethodParameter(String parameterName) {
        this.methodParameter = parameterName;
    }

    /**
     * Indicates if the method name can be tunnelled.
     * 
     * @param methodTunnel
     *                True if the method name can be tunnelled.
     */
    public void setMethodTunnel(boolean methodTunnel) {
        this.methodTunnel = methodTunnel;
    }

    /**
     * Indicates if the client preferences can be tunnelled via the query
     * parameters.
     * 
     * @param preferencesTunnel
     *                True if the client preferences can be tunnelled via the
     *                query parameters.
     */
    public void setPreferencesTunnel(boolean preferencesTunnel) {
        this.preferencesTunnel = preferencesTunnel;
    }

    /**
     * Indicates if the method and client preferences can be tunnelled via query
     * parameters.
     * 
     * @param queryTunnel
     *                True if the method and client preferences can be tunnelled
     *                via query parameters.
     */
    public void setQueryTunnel(boolean queryTunnel) {
        this.queryTunnel = queryTunnel;
    }
}
