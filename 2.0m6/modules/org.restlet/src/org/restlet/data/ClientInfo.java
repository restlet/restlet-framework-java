/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.representation.Variant;

/**
 * Client specific data related to a call. When extracted from a request, most
 * of these data are directly taken from the underlying headers. There are some
 * exceptions: agentAttributes and mainAgentProduct which are taken from the
 * agent name (for example the "user-agent" header for HTTP requests).<br>
 * <br>
 * As described by the HTTP specification, the "user-agent" can be seen as a
 * ordered list of products name (ie a name and a version) and/or comments.<br>
 * <br>
 * Each HTTP client (mainly browsers and web crawlers) defines its own
 * "user-agent" header which can be seen as the "signature" of the client.
 * Unfortunately, there is no rule to identify clearly a kind a client and its
 * version (let's say firefox 2.x, Internet Explorer IE 7.0, Opera, etc)
 * according to its signature. Each signature follow its own rules which may
 * vary according to the version of the client.<br>
 * <br>
 * In order to help retrieving interesting data such as product name (Firefox,
 * IE, etc), version, operating system, Restlet users has the ability to define
 * their own way to extract data from the "user-agent" header. It is based on a
 * list of templates declared in a file called "agent.properties" and located in
 * the classpath in the sub directory "org/restlet/data". Each template
 * describes a typical user-agent string and allows to use predefined variables
 * that help to retrieve the content of the agent name, version, operating
 * system.<br>
 * <br>
 * The "user-agent" string is confronted to the each template from the beginning
 * of the property file to the end. The loop stops at the first matched
 * template.<br>
 * <br>
 * Here is a sample of such template:<br>
 * 
 * <pre>
 * #Firefox for Windows
 *  Mozilla/{mozillaVersion} (Windows; U; {agentOs}; {osData}; rv:{releaseVersion}) Gecko/{geckoReleaseDate} {agentName}/{agentVersion}
 * </pre>
 * 
 * This template matches the "user-agent" string of the Firefox client for
 * windows:
 * 
 * <pre>
 *  Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1) Gecko/20060918 Firefox/2.0
 * </pre>
 * 
 * At this time, six predefined variables are used:<br>
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>agentName</td>
 * <td>Name of the user agent (i.e.: Firefox)</td>
 * </tr>
 * <tr>
 * <td>agentVersion</td>
 * <td>Version of the user agent</td>
 * </tr>
 * <tr>
 * <td>agentOs</td>
 * <td>Operating system of the user agent</td>
 * </tr>
 * <tr>
 * <td>agentComment</td>
 * <td>Comment string, that is to say a sequence of characters enclosed "(", or
 * ")"</td>
 * </tr>
 * <tr>
 * <td>commentAttribute</td>
 * <td>A sequence of characters enclosed by ";", "(", or ")"</td>
 * </tr>
 * <tr>
 * <td>facultativeData</td>
 * <td>A sequence of characters that can be empty</td>
 * </tr>
 * </table>
 * <br>
 * <br>
 * These variables are used to generate a {@link Product} instance with the main
 * data (name, version, comment). This instance is accessible via the
 * {@link ClientInfo#getMainAgentProduct()} method. All other variables used in
 * the template aims at catching a sequence of characters and are accessible via
 * the {@link ClientInfo#getAgentAttributes()} method.
 * 
 * @author Jerome Louvel
 */
public final class ClientInfo {

    // [ifndef gwt] member
    /**
     * List of user-agent templates defined in "agent.properties" file.<br>
     * 
     * @see The {@link ClientInfo#getAgentAttributes()} method.
     */
    private static List<String> userAgentTemplates = null;

    // [ifndef gwt] method
    /**
     * Returns the list of user-agent templates defined in "agent.properties"
     * file.
     * 
     * @return The list of user-agent templates defined in "agent.properties"
     *         file.
     * @see The {@link ClientInfo#getAgentAttributes()} method.
     */
    private static List<String> getUserAgentTemplates() {
        // Lazy initialization with double-check.
        List<String> u = ClientInfo.userAgentTemplates;
        if (u == null) {
            synchronized (ClientInfo.class) {
                u = ClientInfo.userAgentTemplates;
                if (u == null) {
                    // Load from the "agent.properties" file
                    final java.net.URL userAgentPropertiesUrl = Engine
                            .getClassLoader().getResource(
                                    "org/restlet/data/agent.properties");
                    if (userAgentPropertiesUrl != null) {
                        BufferedReader reader;
                        try {
                            reader = new BufferedReader(new InputStreamReader(
                                    userAgentPropertiesUrl.openStream(),
                                    CharacterSet.UTF_8.getName()));
                            String line = reader.readLine();
                            for (; line != null; line = reader.readLine()) {
                                if ((line.trim().length() > 0)
                                        && !line.trim().startsWith("#")) {
                                    if (u == null) {
                                        u = new ArrayList<String>();
                                    }
                                    u.add(line);
                                }
                            }
                            reader.close();
                        } catch (IOException e) {
                            if (Context.getCurrent() != null) {
                                Context
                                        .getCurrent()
                                        .getLogger()
                                        .warning(
                                                "Cannot read '"
                                                        + userAgentPropertiesUrl
                                                                .toString()
                                                        + "' due to: "
                                                        + e.getMessage());
                            }

                        }
                    }
                }
            }
        }
        return u;
    }

    /** The character set preferences. */
    private volatile List<Preference<CharacterSet>> acceptedCharacterSets;

    /** The encoding preferences. */
    private volatile List<Preference<Encoding>> acceptedEncodings;

    /** The language preferences. */
    private volatile List<Preference<Language>> acceptedLanguages;

    /** The media preferences. */
    private volatile List<Preference<MediaType>> acceptedMediaTypes;

    /** The immediate IP addresses. */
    private volatile String address;

    /** The email address of the human user controlling the user agent. */
    private volatile String from;

    /** The agent name. */
    private volatile String agent;

    // [ifndef gwt] member
    /** The attributes data taken from the agent name. */
    private volatile Map<String, String> agentAttributes;

    // [ifndef gwt] member
    /** The main product data taken from the agent name. */
    private volatile Product agentMainProduct;

    // [ifndef gwt] member
    /** The list of product tokens taken from the agent name. */
    private volatile List<Product> agentProducts;

    // [ifndef gwt] member
    /**
     * Indicates if the subject has been authenticated. The application is
     * responsible for updating this property, relying on
     * {@link org.restlet.security.Authenticator} or manually.
     */
    private volatile boolean authenticated;

    /** The forwarded IP addresses. */
    private volatile List<String> forwardedAddresses;

    /** The port number. */
    private volatile int port;

    // [ifndef gwt] member
    /** List of security principals. */
    private volatile List<java.security.Principal> principals;

    // [ifndef gwt] member
    /** Authenticated user. */
    private volatile org.restlet.security.User user;

    // [ifndef gwt] member
    /** List of user roles. */
    private volatile List<org.restlet.security.Role> roles;

    /**
     * Constructor.
     */
    public ClientInfo() {
        this.address = null;
        this.agent = null;
        this.port = -1;
        this.acceptedCharacterSets = null;
        this.acceptedEncodings = null;
        this.acceptedLanguages = null;
        this.acceptedMediaTypes = null;
        this.forwardedAddresses = null;
        this.from = null;
        // [ifndef gwt]
        this.agentProducts = null;
        this.principals = null;
        // [enddef]
    }

    /**
     * Returns the modifiable list of character set preferences. Creates a new
     * instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Charset" header.
     * 
     * @return The character set preferences.
     */
    public List<Preference<CharacterSet>> getAcceptedCharacterSets() {
        // Lazy initialization with double-check.
        List<Preference<CharacterSet>> a = this.acceptedCharacterSets;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedCharacterSets;
                if (a == null) {
                    this.acceptedCharacterSets = a = new ArrayList<Preference<CharacterSet>>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of encoding preferences. Creates a new
     * instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Encoding" header.
     * 
     * @return The encoding preferences.
     */
    public List<Preference<Encoding>> getAcceptedEncodings() {
        // Lazy initialization with double-check.
        List<Preference<Encoding>> a = this.acceptedEncodings;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedEncodings;
                if (a == null) {
                    this.acceptedEncodings = a = new ArrayList<Preference<Encoding>>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of language preferences. Creates a new
     * instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Language" header.
     * 
     * @return The language preferences.
     */
    public List<Preference<Language>> getAcceptedLanguages() {
        // Lazy initialization with double-check.
        List<Preference<Language>> a = this.acceptedLanguages;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedLanguages;
                if (a == null) {
                    this.acceptedLanguages = a = new ArrayList<Preference<Language>>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of media type preferences. Creates a new
     * instance if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept" header.
     * 
     * @return The media type preferences.
     */
    public List<Preference<MediaType>> getAcceptedMediaTypes() {
        // Lazy initialization with double-check.
        List<Preference<MediaType>> a = this.acceptedMediaTypes;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedMediaTypes;
                if (a == null) {
                    this.acceptedMediaTypes = a = new ArrayList<Preference<MediaType>>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the immediate client's IP address. If the real client is
     * separated from the server by a proxy server, this will return the IP
     * address of the proxy.
     * 
     * @return The immediate client's IP address.
     * @see #getUpstreamAddress()
     * @see #getForwardedAddresses()
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Returns the list of client IP addresses.<br>
     * <br>
     * The first address is the one of the immediate client component as
     * returned by the getClientAdress() method and the last address should
     * correspond to the origin client (frequently a user agent).<br>
     * <br>
     * This is useful when the user agent is separated from the origin server by
     * a chain of intermediary components. Creates a new instance if no one has
     * been set.
     * 
     * @return The client IP addresses.
     * @deprecated Use the {@link #getForwardedAddresses()} method instead.
     */
    @Deprecated
    public List<String> getAddresses() {
        return getForwardedAddresses();
    }

    /**
     * Returns the agent name (ex: "Restlet-Framework/2.0").<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "User-Agent" header.
     * 
     * @return The agent name.
     */
    public String getAgent() {
        return this.agent;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of attributes taken from the name of the user agent.
     * 
     * @return A list of attributes taken from the name of the user agent.
     * @see #getAgent()
     */
    public Map<String, String> getAgentAttributes() {

        if (this.agentAttributes == null) {
            this.agentAttributes = new HashMap<String, String>();
            final Map<String, Object> map = new HashMap<String, Object>();

            // Loop on a list of user-agent templates until a template match
            // the current user-agent string. The list of templates is
            // located in a file named "agent.properties" available on
            // the classpath.
            // Some defined variables are used in order to catch the name,
            // version and facultative comment. Respectively, these
            // variables are called "agentName", "agentVersion" and
            // "agentComment".
            org.restlet.routing.Template template = null;
            // Predefined variables.
            final org.restlet.routing.Variable agentName = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_TOKEN);
            final org.restlet.routing.Variable agentVersion = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_TOKEN);
            final org.restlet.routing.Variable agentComment = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_COMMENT);
            final org.restlet.routing.Variable agentCommentAttribute = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_COMMENT_ATTRIBUTE);
            final org.restlet.routing.Variable facultativeData = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_ALL, null, false, false);
            for (String string : ClientInfo.getUserAgentTemplates()) {
                template = new org.restlet.routing.Template(string,
                        org.restlet.routing.Template.MODE_EQUALS);
                // Update the predefined variables.
                template.getVariables().put("agentName", agentName);
                template.getVariables().put("agentVersion", agentVersion);
                template.getVariables().put("agentComment", agentComment);
                template.getVariables().put("agentOs", agentCommentAttribute);
                template.getVariables().put("commentAttribute",
                        agentCommentAttribute);
                template.getVariables().put("facultativeData", facultativeData);
                // Parse the template
                if (template.parse(getAgent(), map) > -1) {
                    for (final String key : map.keySet()) {
                        this.agentAttributes.put(key, (String) map.get(key));
                    }
                    break;
                }
            }
        }

        return this.agentAttributes;
    }

    // [ifndef gwt] method
    /**
     * Returns the name of the user agent.
     * 
     * @return The name of the user agent.
     * @see #getAgent()
     */
    public String getAgentName() {
        final Product product = getMainAgentProduct();
        if (product != null) {
            return product.getName();
        }

        return null;
    }

    // [ifndef gwt] method
    /**
     * Returns the list of product tokens from the user agent name.
     * 
     * @return The list of product tokens from the user agent name.
     * @see #getAgent()
     */
    public List<Product> getAgentProducts() {
        if (this.agentProducts == null) {
            this.agentProducts = org.restlet.engine.http.UserAgentUtils
                    .parse(getAgent());
        }
        return this.agentProducts;
    }

    // [ifndef gwt] method
    /**
     * Returns the version of the user agent.
     * 
     * @return The version of the user agent.
     * @see #getAgent()
     */
    public String getAgentVersion() {
        final Product product = getMainAgentProduct();
        if (product != null) {
            return product.getVersion();
        }
        return null;

    }

    /**
     * Returns the list of forwarded IP addresses.<br>
     * <br>
     * This is useful when the user agent is separated from the origin server by
     * a chain of intermediary components. Creates a new instance if no one has
     * been set. <br>
     * <br>
     * The first address is the one of the immediate client component and the
     * last address should correspond to the origin client (frequently a user
     * agent).<br>
     * <br>
     * This information is only safe for intermediary components within your
     * local network. Other addresses could easily be changed by setting a fake
     * header and should not be trusted for serious security checks.<br>
     * <br>
     * Note that your HTTP server connectors have a special
     * "useForwardedForHeader" parameter that you need to explicitly set to
     * "true" in order to activate this feature due to potential security
     * issues.
     * 
     * @return The list of forwarded IP addresses.
     * @see <a href="http://en.wikipedia.org/wiki/X-Forwarded-For">Wikipedia
     *      page for the "X-Forwarded-For" HTTP header</a>
     */
    public List<String> getForwardedAddresses() {
        // Lazy initialization with double-check.
        List<String> a = this.forwardedAddresses;
        if (a == null) {
            synchronized (this) {
                a = this.forwardedAddresses;
                if (a == null) {
                    this.forwardedAddresses = a = new ArrayList<String>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the email address of the human user controlling the user agent.
     * Default value is null.
     * 
     * @return The email address of the human user controlling the user agent.
     */
    public String getFrom() {
        return from;
    }

    // [ifndef gwt] method
    /**
     * Returns a Product object based on the name of the user agent.
     * 
     * @return A Product object based on name of the user agent.
     */
    public Product getMainAgentProduct() {
        if (this.agentMainProduct == null) {
            if (getAgentAttributes() != null) {
                this.agentMainProduct = new Product(getAgentAttributes().get(
                        "agentName"), getAgentAttributes().get("agentVersion"),
                        getAgentAttributes().get("agentComment"));
            }
        }

        return this.agentMainProduct;
    }

    /**
     * Returns the port number which sent the call. If no port is specified, -1
     * is returned.
     * 
     * @return The port number which sent the call.
     */
    public int getPort() {
        return this.port;
    }

    // [ifndef gwt] method
    /**
     * Returns the preferred character set among a list of supported ones, based
     * on the client preferences.
     * 
     * @param supported
     *            The supported character sets.
     * @return The preferred character set.
     */
    public CharacterSet getPreferredCharacterSet(List<CharacterSet> supported) {
        return org.restlet.engine.util.ConnegUtils.getPreferredMetadata(
                supported, getAcceptedCharacterSets());
    }

    // [ifndef gwt] method
    /**
     * Returns the preferred encoding among a list of supported ones, based on
     * the client preferences.
     * 
     * @param supported
     *            The supported encodings.
     * @return The preferred encoding.
     */
    public Encoding getPreferredEncoding(List<Encoding> supported) {
        return org.restlet.engine.util.ConnegUtils.getPreferredMetadata(
                supported, getAcceptedEncodings());
    }

    // [ifndef gwt] method
    /**
     * Returns the preferred language among a list of supported ones, based on
     * the client preferences.
     * 
     * @param supported
     *            The supported languages.
     * @return The preferred language.
     */
    public Language getPreferredLanguage(List<Language> supported) {
        return org.restlet.engine.util.ConnegUtils.getPreferredMetadata(
                supported, getAcceptedLanguages());
    }

    // [ifndef gwt] method
    /**
     * Returns the preferred media type among a list of supported ones, based on
     * the client preferences.
     * 
     * @param supported
     *            The supported media types.
     * @return The preferred media type.
     */
    public MediaType getPreferredMediaType(List<MediaType> supported) {
        return org.restlet.engine.util.ConnegUtils.getPreferredMetadata(
                supported, getAcceptedMediaTypes());
    }

    // [ifndef gwt] method
    /**
     * Returns the best variant for a given resource according the the client
     * preferences: accepted languages, accepted character sets, accepted media
     * types and accepted encodings.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param variants
     *            The list of variants to compare.
     * @param metadataService
     *            The metadata service.
     * @return The best variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(List<Variant> variants,
            org.restlet.service.MetadataService metadataService) {
        return org.restlet.engine.util.ConnegUtils.getPreferredVariant(this,
                variants, metadataService);
    }

    // [ifndef gwt] method
    /**
     * Returns the best variant for a given resource according the the client
     * preferences.<br>
     * A default language is provided in case the resource's variants don't
     * match the client preferences.
     * 
     * @param resource
     *            The resource for which the best representation needs to be
     *            set.
     * @param metadataService
     *            The metadata service.
     * @return The best variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     * @deprecated Used
     *             {@link #getPreferredVariant(List, org.restlet.service.MetadataService)}
     *             instead.
     */
    @Deprecated
    public Variant getPreferredVariant(org.restlet.resource.Resource resource,
            org.restlet.service.MetadataService metadataService) {
        return getPreferredVariant(resource.getVariants(), metadataService);
    }

    // [ifndef gwt] method
    /**
     * Returns the client principals.
     * 
     * @return The client principals.
     */
    public List<java.security.Principal> getPrincipals() {
        // Lazy initialization with double-check.
        List<java.security.Principal> a = this.principals;
        if (a == null) {
            synchronized (this) {
                a = this.principals;
                if (a == null) {
                    this.principals = a = new ArrayList<java.security.Principal>();
                }
            }
        }
        return a;
    }

    // [ifndef gwt] method
    /**
     * Returns the authenticated user roles.
     * 
     * @return The authenticated user roles.
     */
    public List<org.restlet.security.Role> getRoles() {
        // Lazy initialization with double-check.
        List<org.restlet.security.Role> a = this.roles;
        if (a == null) {
            synchronized (this) {
                a = this.roles;
                if (a == null) {
                    this.roles = a = new ArrayList<org.restlet.security.Role>();
                }
            }
        }
        return a;
    }

    // [ifndef gwt] method
    /**
     * Returns the IP address of the upstream client component. In general this
     * will correspond the the user agent IP address. This is useful if there
     * are intermediary components like proxies and load balancers.
     * 
     * If the supporting {@link #getForwardedAddresses()} method returns a non
     * empty list, the IP address will be the first element. Otherwise, the
     * value of {@link #getAddress()} will be returned.<br>
     * <br>
     * Note that your HTTP server connectors have a special
     * "useForwardedForHeader" parameter that you need to explicitly set to
     * "true" in order to activate this feature due to potential security
     * issues.
     * 
     * @return The most upstream IP address.
     */
    public String getUpstreamAddress() {
        if (this.forwardedAddresses == null
                || this.forwardedAddresses.isEmpty()) {
            return getAddress();
        }

        return this.forwardedAddresses.get(0);
    }

    // [ifndef gwt] method
    /**
     * Returns the authenticated user.
     * 
     * @return The authenticated user.
     */
    public org.restlet.security.User getUser() {
        return user;
    }

    // [ifndef gwt] method
    /**
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.security.Guard} or manually.
     * 
     * @return True if the identifier or principal has been authenticated.
     */
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Sets the character set preferences.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Charset" header.
     * 
     * @param acceptedCharacterSets
     *            The character set preferences.
     */
    public void setAcceptedCharacterSets(
            List<Preference<CharacterSet>> acceptedCharacterSets) {
        this.acceptedCharacterSets = acceptedCharacterSets;
    }

    /**
     * Sets the encoding preferences.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Encoding" header.
     * 
     * @param acceptedEncodings
     *            The encoding preferences.
     */
    public void setAcceptedEncodings(
            List<Preference<Encoding>> acceptedEncodings) {
        this.acceptedEncodings = acceptedEncodings;
    }

    /**
     * Sets the language preferences.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Language" header.
     * 
     * @param acceptedLanguages
     *            The language preferences.
     */
    public void setAcceptedLanguages(
            List<Preference<Language>> acceptedLanguages) {
        this.acceptedLanguages = acceptedLanguages;
    }

    /**
     * Sets the media type preferences.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept" header.
     * 
     * @param acceptedMediaTypes
     *            The media type preferences.
     */
    public void setAcceptedMediaTypes(
            List<Preference<MediaType>> acceptedMediaTypes) {
        this.acceptedMediaTypes = acceptedMediaTypes;
    }

    /**
     * Sets the client's IP address.
     * 
     * @param address
     *            The client's IP address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the list of client IP addresses.
     * 
     * @param addresses
     *            The list of client IP addresses.
     * @deprecated See the {@link #setForwardedAddresses(List)} method instead.
     */
    @Deprecated
    public void setAddresses(List<String> addresses) {
        setForwardedAddresses(addresses);
    }

    /**
     * Sets the agent name (ex: "Restlet-Framework/2.0").<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "User-Agent" header.
     * 
     * @param agent
     *            The agent name.
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    // [ifndef gwt] method
    /**
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.security.Guard} or manually.
     * 
     * @param authenticated
     *            True if the identifier or principal has been authenticated.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Sets the list of forwarded IP addresses.
     * 
     * @param forwardedAddresses
     *            The list of forwarded IP addresses.
     * @see #getForwardedAddresses()
     */
    public void setForwardedAddresses(List<String> forwardedAddresses) {
        this.forwardedAddresses = forwardedAddresses;
    }

    /**
     * Sets the email address of the human user controlling the user agent.
     * 
     * @param from
     *            The email address of the human user controlling the user
     *            agent.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Sets the port number which sent the call.
     * 
     * @param port
     *            The port number which sent the call.
     */
    public void setPort(int port) {
        this.port = port;
    }

    // [ifndef gwt] method
    /**
     * Sets the user principals.
     * 
     * @param principals
     *            The user principals.
     */
    public void setPrincipals(List<java.security.Principal> principals) {
        this.principals = principals;
    }

    // [ifndef gwt] method
    /**
     * Sets the authenticated user roles.
     * 
     * @param roles
     *            The authenticated user roles.
     */
    public void setRoles(List<org.restlet.security.Role> roles) {
        this.roles = roles;
    }

    // [ifndef gwt] method
    /**
     * Sets the authenticated user.
     * 
     * @param user
     *            The authenticated user.
     */
    public void setUser(org.restlet.security.User user) {
        this.user = user;
    }

}
