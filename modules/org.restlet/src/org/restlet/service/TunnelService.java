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
import org.restlet.data.Request;

/**
 * Service tunnelling method names or client preferences via query parameters or
 * extensions. Clients applications such as browsers can easily override the
 * default values of their client connector by specifying additional query
 * parameters. Here is the list of the default parameter names supported:
 * <table>
 * <tr>
 * <th>Property</th>
 * <th>Default name</th>
 * <th>Value type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>methodParameter</td>
 * <td>method</td>
 * <td>See values in org.restlet.data.Method</td>
 * <td>For POST requests, specify the actual method to use (DELETE, PUT, etc.).</td>
 * </tr>
 * <tr>
 * <td>characterSetParameter</td>
 * <td>charset</td>
 * <td>Use extension names defined in org.restlet.service.MetadataService</td>
 * <td>For GET requests, replaces the accepted character set by the given
 * value.</td>
 * </tr>
 * <tr>
 * <td>encodingParameter</td>
 * <td>encoding</td>
 * <td>Use extension names defined in org.restlet.service.MetadataService</td>
 * <td>For GET requests, replaces the accepted encoding by the given value.</td>
 * </tr>
 * <tr>
 * <td>languageParameter</td>
 * <td>language</td>
 * <td>Use extension names defined in org.restlet.service.MetadataService</td>
 * <td>For GET requests, replaces the accepted language by the given value.</td>
 * </tr>
 * <tr>
 * <td>mediaTypeParameter</td>
 * <td>media</td>
 * <td>Use extension names defined in org.restlet.service.MetadataService</td>
 * <td>For GET requests, replaces the accepted media type set by the given
 * value.</td>
 * </tr>
 * </table><br>
 * The client preferences can also be updated via the extensions (in the meaning
 * of file extensions: e.g. ".txt") discovered in the resource's URI. This
 * mechanism relies on the mapping between an extension and a metadata (e.g.
 * ".txt" => "text/plain") maintained by the MetadataService
 * {@link MetadataService}.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TunnelService {

    /**
     * Key in the attributes of a {@link Request} ({@link Request#getAttributes()})
     * for the resource URI without the extensions.<br>
     * The type of the value is {@link String}.
     * 
     * @see #REF_ORIGINAL_KEY
     * @see #REF_EXTENSIONS_KEY
     * @see #isExtensionTunnel()
     * @see #setExtensionTunnel(boolean)
     */
    public static final String REF_CUT_KEY = "org.restlet.TunnelService.reference.cut";

    /**
     * Key in the attributes of a {@link Request} ({@link Request#getAttributes()})
     * for the pulled out extensions.<br>
     * The type of the value is {@link String}. If no extensions are matched,
     * the value is null.
     * 
     * @see #REF_ORIGINAL_KEY
     * @see #REF_CUT_KEY
     * @see #isExtensionTunnel()
     * @see #setExtensionTunnel(boolean)
     */
    public static final String REF_EXTENSIONS_KEY = "org.restlet.TunnelService.reference.extensions";

    /**
     * Key in the attributes of a {@link Request} ({@link Request#getAttributes()})
     * for the original resource URI before extraction of known extensions.<br>
     * The type of the value is {@link String}.
     * 
     * @see #REF_CUT_KEY
     * @see #REF_EXTENSIONS_KEY
     * @see #isExtensionTunnel()
     * @see #setExtensionTunnel(boolean)
     */
    public static final String REF_ORIGINAL_KEY = "org.restlet.TunnelService.reference.original";

    /** The name of the parameter containing the accepted character set. */
    private volatile String characterSetParameter;

    /** Indicates if the service has been enabled. */
    private volatile boolean enabled;

    /** The name of the parameter containing the accepted encoding. */
    private volatile String encodingParameter;

    /** Indicates if the client preferences can be tunnelled. */
    private volatile boolean extensionTunnel;

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
     * Constructor.
     * 
     * @param enabled
     *                True if the service has been enabled.
     * @param methodTunnel
     *                Indicates if the method name can be tunnelled.
     * @param preferencesTunnel
     *                Indicates if the client preferences for a GET request can
     *                be tunnelled by query parameters and file "extensions".
     * @deprecated Use this constructor TunnelService(boolean, boolean, boolean,
     *             boolean) instead.
     */
    @Deprecated
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel) {
        this(enabled, methodTunnel, preferencesTunnel, preferencesTunnel);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *                True if the service has been enabled.
     * @param methodTunnel
     *                Indicates if the method name can be tunnelled.
     * @param preferencesTunnel
     *                Indicates if the client preferences for a GET request can
     *                be tunnelled by query parameters.
     * @param extensionTunnel
     *                Indicates if the client preferences can be tunnelled by
     *                file "extensions".
     */
    public TunnelService(boolean enabled, boolean methodTunnel,
            boolean preferencesTunnel, boolean extensionTunnel) {
        this.enabled = enabled;
        this.methodTunnel = methodTunnel;
        this.methodParameter = "method";
        this.extensionTunnel = extensionTunnel;
        this.preferencesTunnel = preferencesTunnel;
        this.characterSetParameter = "charset";
        this.encodingParameter = "encoding";
        this.languageParameter = "language";
        this.mediaTypeParameter = "media";
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
     * @deprecated Use getCharacterSetParameter instead.
     */
    @Deprecated
    public String getCharacterSetAttribute() {
        return this.characterSetParameter;
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
     * @deprecated Use getEncodingParameter instead.
     */
    @Deprecated
    public String getEncodingAttribute() {
        return this.encodingParameter;
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
     * @deprecated Use getLanguageParameter instead.
     */
    @Deprecated
    public String getLanguageAttribute() {
        return this.languageParameter;
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
     * @deprecated Use getMediaTypeParameter instead.
     */
    @Deprecated
    public String getMediaTypeAttribute() {
        return this.mediaTypeParameter;
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
     * @see #REF_CUT_KEY
     * @see #REF_EXTENSIONS_KEY
     * @see #REF_ORIGINAL_KEY
     */
    public boolean isExtensionTunnel() {
        return this.extensionTunnel;
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
     * Sets the character set parameter name.
     * 
     * @param parameterName
     *                The character set parameter name.
     * @deprecated Use setCharacterSetParameter instead.
     */
    @Deprecated
    public void setCharacterSetAttribute(String parameterName) {
        this.characterSetParameter = parameterName;
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
     * @deprecated Use setEncodingParameter instead.
     */
    @Deprecated
    public void setEncodingAttribute(String parameterName) {
        this.encodingParameter = parameterName;
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
     * @see #REF_CUT_KEY
     * @see #REF_EXTENSIONS_KEY
     * @see #REF_ORIGINAL_KEY
     */
    public void setExtensionTunnel(boolean extensionTunnel) {
        this.extensionTunnel = extensionTunnel;
    }

    /**
     * Sets the name of the parameter containing the accepted language.
     * 
     * @param parameterName
     *                The name of the parameter containing the accepted
     *                language.
     * @deprecated Use setLanguageParameter instead.
     */
    @Deprecated
    public void setLanguageAttribute(String parameterName) {
        this.languageParameter = parameterName;
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
     * @deprecated Use setMediaTypeParameter instead.
     */
    @Deprecated
    public void setMediaTypeAttribute(String parameterName) {
        this.mediaTypeParameter = parameterName;
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

}
