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

package org.restlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.engine.io.IoUtils;

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
 * version (let's say Firefox 2.x, Internet Explorer IE 7.0, Opera, etc)
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
    private static volatile List<String> userAgentTemplates = null;

    // [ifndef gwt] method
    /**
     * Returns the preferred metadata taking into account both metadata
     * supported by the server and client preferences.
     * 
     * @param supported
     *            The metadata supported by the server.
     * @param preferences
     *            The client preferences.
     * @return The preferred metadata.
     */
    public static <T extends Metadata> T getPreferredMetadata(
            List<T> supported, List<Preference<T>> preferences) {
        T result = null;
        float maxQuality = 0;

        if (supported != null) {
            for (Preference<T> pref : preferences) {
                for (T metadata : supported) {
                    if (pref.getMetadata().isCompatible(metadata)
                            && (pref.getQuality() > maxQuality)) {
                        result = metadata;
                        maxQuality = pref.getQuality();
                    }
                }
            }
        }

        return result;
    }

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
                    java.net.URL userAgentPropertiesUrl = Engine
                            .getResource("org/restlet/data/agent.properties");
                    if (userAgentPropertiesUrl != null) {
                        BufferedReader reader;
                        try {
                            reader = new BufferedReader(new InputStreamReader(
                                    userAgentPropertiesUrl.openStream(),
                                    CharacterSet.UTF_8.getName()),
                                    IoUtils.BUFFER_SIZE);
                            String line = reader.readLine();
                            for (; line != null; line = reader.readLine()) {
                                if ((line.trim().length() > 0)
                                        && !line.trim().startsWith("#")) {
                                    if (u == null) {
                                        u = new CopyOnWriteArrayList<String>();
                                    }
                                    u.add(line);
                                }
                            }
                            reader.close();
                        } catch (IOException e) {
                            if (Context.getCurrent() != null) {
                                Context.getCurrent()
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
                    ClientInfo.userAgentTemplates = u;
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

    /** The patch preferences. */
    private volatile List<Preference<MediaType>> acceptedPatches;

    /** The immediate IP addresses. */
    private volatile String address;

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

    // [ifndef gwt] member
    /** List of client certificates. */
    private volatile List<java.security.cert.Certificate> certificates;

    // [ifndef gwt] member
    /** The SSL Cipher Suite, if available and accessible. */
    private volatile String cipherSuite;

    // [ifndef gwt] member
    /** List of expectations. */
    private volatile List<org.restlet.data.Expectation> expectations;

    /** The forwarded IP addresses. */
    private volatile List<String> forwardedAddresses;

    /** The email address of the human user controlling the user agent. */
    private volatile String from;

    /** The port number. */
    private volatile int port;

    // [ifndef gwt] member
    /** List of additional client principals. */
    private volatile List<java.security.Principal> principals;

    // [ifndef gwt] member
    /** List of user roles. */
    private volatile List<org.restlet.security.Role> roles;

    // [ifndef gwt] member
    /** Authenticated user. */
    private volatile org.restlet.security.User user;

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
        this.acceptedPatches = null;
        this.forwardedAddresses = null;
        this.from = null;
        // [ifndef gwt]
        this.agentProducts = null;
        this.principals = null;
        this.user = null;
        this.roles = null;
        this.expectations = null;
        // [enddef]
    }

    // [ifndef gwt] method
    /**
     * Constructor from a list of variants. Note that only media types are taken
     * into account.
     * 
     * @param variants
     *            The variants corresponding to the accepted media types.
     */
    public ClientInfo(
            List<? extends org.restlet.representation.Variant> variants) {
        if (variants != null) {
            for (org.restlet.representation.Variant variant : variants) {
                getAcceptedMediaTypes().add(
                        new Preference<MediaType>(variant.getMediaType()));
            }
        }
    }

    /**
     * Constructor from a media type.
     * 
     * @param mediaType
     *            The preferred media type.
     */
    public ClientInfo(MediaType mediaType) {
        getAcceptedMediaTypes().add(new Preference<MediaType>(mediaType));
    }

    /**
     * Updates the client preferences to accept the given metadata (media types,
     * character sets, etc.) with a 1.0 quality in addition to existing ones.
     * 
     * @param metadata
     *            The metadata to accept.
     */
    public void accept(Metadata... metadata) {
        if (metadata != null) {
            for (Metadata md : metadata) {
                accept(md, 1.0F);
            }
        }
    }

    /**
     * Updates the client preferences to accept the given metadata (media types,
     * character sets, etc.) with a given quality in addition to existing ones.
     * 
     * @param metadata
     *            The metadata to accept.
     * @param quality
     *            The quality to set.
     */
    public void accept(Metadata metadata, float quality) {
        if (metadata instanceof MediaType) {
            getAcceptedMediaTypes().add(
                    new Preference<MediaType>((MediaType) metadata, quality));
        } else if (metadata instanceof Language) {
            getAcceptedLanguages().add(
                    new Preference<Language>((Language) metadata, quality));
        } else if (metadata instanceof Encoding) {
            getAcceptedEncodings().add(
                    new Preference<Encoding>((Encoding) metadata, quality));
        } else {
            getAcceptedCharacterSets().add(
                    new Preference<CharacterSet>((CharacterSet) metadata,
                            quality));
        }
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
                    this.acceptedCharacterSets = a = new CopyOnWriteArrayList<Preference<CharacterSet>>();
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
                    this.acceptedEncodings = a = new CopyOnWriteArrayList<Preference<Encoding>>();
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
                    this.acceptedLanguages = a = new CopyOnWriteArrayList<Preference<Language>>();
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
                    this.acceptedMediaTypes = a = new CopyOnWriteArrayList<Preference<MediaType>>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of patch preferences. Creates a new instance
     * if no one has been set.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Accept-Patch" header.
     * 
     * @return The patch preferences.
     */
    public List<Preference<MediaType>> getAcceptedPatches() {
        // Lazy initialization with double-check.
        List<Preference<MediaType>> a = this.acceptedPatches;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedPatches;
                if (a == null) {
                    this.acceptedPatches = a = new CopyOnWriteArrayList<Preference<MediaType>>();
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
     * Returns the agent name (ex: "Restlet-Framework/2.0"). Note that when used
     * with HTTP connectors, this property maps to the "User-Agent" header.
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
            this.agentAttributes = new ConcurrentHashMap<String, String>();
            Map<String, Object> map = new ConcurrentHashMap<String, Object>();

            // Loop on a list of user-agent templates until a template match
            // the current user-agent string. The list of templates is
            // located in a file named "agent.properties" available on
            // the classpath.
            // Some defined variables are used in order to catch the name,
            // version and optional comment. Respectively, these
            // variables are called "agentName", "agentVersion" and
            // "agentComment".
            org.restlet.routing.Template template = null;
            // Predefined variables.
            org.restlet.routing.Variable agentName = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_TOKEN);
            org.restlet.routing.Variable agentVersion = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_TOKEN);
            org.restlet.routing.Variable agentComment = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_COMMENT);
            org.restlet.routing.Variable agentCommentAttribute = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_COMMENT_ATTRIBUTE);
            org.restlet.routing.Variable facultativeData = new org.restlet.routing.Variable(
                    org.restlet.routing.Variable.TYPE_ALL, null, false, false);

            if (ClientInfo.getUserAgentTemplates() != null) {
                for (String string : ClientInfo.getUserAgentTemplates()) {
                    template = new org.restlet.routing.Template(string,
                            org.restlet.routing.Template.MODE_EQUALS);

                    // Update the predefined variables.
                    template.getVariables().put("agentName", agentName);
                    template.getVariables().put("agentVersion", agentVersion);
                    template.getVariables().put("agentComment", agentComment);
                    template.getVariables().put("agentOs",
                            agentCommentAttribute);
                    template.getVariables().put("commentAttribute",
                            agentCommentAttribute);
                    template.getVariables().put("facultativeData",
                            facultativeData);

                    // Parse the template
                    if (template.parse(getAgent(), map) > -1) {
                        for (String key : map.keySet()) {
                            this.agentAttributes
                                    .put(key, (String) map.get(key));
                        }
                        break;
                    }
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
            this.agentProducts = org.restlet.engine.header.ProductReader
                    .read(getAgent());
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

    // [ifndef gwt] method
    /**
     * Returns the client certificates. Those certificates are available when a
     * request is received via an HTTPS connection, corresponding to the SSL/TLS
     * certificates.
     * 
     * @return The client certificates.
     * @see javax.net.ssl.SSLSession#getPeerCertificates()
     */
    public List<java.security.cert.Certificate> getCertificates() {
        // Lazy initialization with double-check.
        List<java.security.cert.Certificate> a = this.certificates;
        if (a == null) {
            synchronized (this) {
                a = this.certificates;
                if (a == null) {
                    this.certificates = a = new CopyOnWriteArrayList<java.security.cert.Certificate>();
                }
            }
        }
        return a;
    }

    // [ifndef gwt] method
    /**
     * Returns the SSL Cipher Suite, if available and accessible.
     * 
     * @return The SSL Cipher Suite, if available and accessible.
     * @see javax.net.ssl.SSLSession#getCipherSuite()
     */
    public String getCipherSuite() {
        return this.cipherSuite;
    }

    // [ifndef gwt] method
    /**
     * Returns the client expectations.
     * 
     * @return The client expectations.
     */
    public List<org.restlet.data.Expectation> getExpectations() {
        // Lazy initialization with double-check.
        List<org.restlet.data.Expectation> a = this.expectations;
        if (a == null) {
            synchronized (this) {
                a = this.expectations;
                if (a == null) {
                    this.expectations = a = new CopyOnWriteArrayList<org.restlet.data.Expectation>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the list of forwarded IP addresses. This is useful when the user
     * agent is separated from the origin server by a chain of intermediary
     * components. Creates a new instance if no one has been set. <br>
     * <br>
     * The first address is the one of the immediate client component and the
     * last address should correspond to the origin client (frequently a user
     * agent).<br>
     * <br>
     * This information is only safe for intermediary components within your
     * local network. Other addresses could easily be changed by setting a fake
     * header and should not be trusted for serious security checks.<br>
     * <br>
     * Note that your HTTP server connectors need to have a special
     * "useForwardedForHeader" parameter explicitly set to "true" in order to
     * activate this feature, due to potential security issues.
     * 
     * @return The list of forwarded IP addresses.
     * @see #getUpstreamAddress()
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
                    this.forwardedAddresses = a = new CopyOnWriteArrayList<String>();
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
        return getPreferredMetadata(supported, getAcceptedCharacterSets());
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
        return getPreferredMetadata(supported, getAcceptedEncodings());
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
        return getPreferredMetadata(supported, getAcceptedLanguages());
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
        return getPreferredMetadata(supported, getAcceptedMediaTypes());
    }

    // [ifndef gwt] method
    /**
     * Returns the preferred patch among a list of supported ones, based on the
     * client preferences.
     * 
     * @param supported
     *            The supported patches.
     * @return The preferred patch.
     */
    public MediaType getPreferredPatch(List<MediaType> supported) {
        return getPreferredMetadata(supported, getAcceptedPatches());
    }

    // [ifndef gwt] method
    /**
     * Returns the additional client principals. Note that {@link #getUser()}
     * and {@link #getRoles()} methods already return user and role principals.
     * 
     * @return The additional client principals.
     */
    public List<java.security.Principal> getPrincipals() {
        // Lazy initialization with double-check.
        List<java.security.Principal> a = this.principals;
        if (a == null) {
            synchronized (this) {
                a = this.principals;
                if (a == null) {
                    this.principals = a = new CopyOnWriteArrayList<java.security.Principal>();
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
                    this.roles = a = new CopyOnWriteArrayList<org.restlet.security.Role>();
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
     * Note that your HTTP server connectors need to have a special
     * "useForwardedForHeader" parameter explicitly set to "true" in order to
     * activate this feature, due to potential security issues.
     * 
     * @return The most upstream IP address.
     * @see #getAddress()
     * @see #getForwardedAddresses()
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
     * {@link org.restlet.security.Authenticator} or manually.
     * 
     * @return True if the identifier or principal has been authenticated.
     */
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Sets the character set preferences. Note that when used with HTTP
     * connectors, this property maps to the "Accept-Charset" header.
     * 
     * @param acceptedCharacterSets
     *            The character set preferences.
     */
    public void setAcceptedCharacterSets(
            List<Preference<CharacterSet>> acceptedCharacterSets) {
        synchronized (this) {
            List<Preference<CharacterSet>> ac = getAcceptedCharacterSets();
            ac.clear();
            ac.addAll(acceptedCharacterSets);
        }
    }

    /**
     * Sets the encoding preferences. Note that when used with HTTP connectors,
     * this property maps to the "Accept-Encoding" header.
     * 
     * @param acceptedEncodings
     *            The encoding preferences.
     */
    public void setAcceptedEncodings(
            List<Preference<Encoding>> acceptedEncodings) {
        synchronized (this) {
            List<Preference<Encoding>> ac = getAcceptedEncodings();
            ac.clear();
            ac.addAll(acceptedEncodings);
        }
    }

    /**
     * Sets the language preferences. Note that when used with HTTP connectors,
     * this property maps to the "Accept-Language" header.
     * 
     * @param acceptedLanguages
     *            The language preferences.
     */
    public void setAcceptedLanguages(
            List<Preference<Language>> acceptedLanguages) {
        synchronized (this) {
            List<Preference<Language>> ac = getAcceptedLanguages();
            ac.clear();
            ac.addAll(acceptedLanguages);
        }
    }

    /**
     * Sets the media type preferences. Note that when used with HTTP
     * connectors, this property maps to the "Accept" header.
     * 
     * @param acceptedMediaTypes
     *            The media type preferences.
     */
    public void setAcceptedMediaTypes(
            List<Preference<MediaType>> acceptedMediaTypes) {
        synchronized (this) {
            List<Preference<MediaType>> ac = getAcceptedMediaTypes();
            ac.clear();
            ac.addAll(acceptedMediaTypes);
        }
    }

    /**
     * Sets the patch preferences. Note that when used with HTTP connectors,
     * this property maps to the "Accept-Patch" header.
     * 
     * @param acceptedPatches
     *            The media type preferences.
     */
    public void setAcceptedPatches(List<Preference<MediaType>> acceptedPatches) {
        synchronized (this) {
            List<Preference<MediaType>> ac = getAcceptedPatches();
            ac.clear();
            ac.addAll(acceptedPatches);
        }
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
     * Sets the agent name (ex: "Restlet-Framework/2.0"). Note that when used
     * with HTTP connectors, this property maps to the "User-Agent" header.
     * 
     * @param agent
     *            The agent name.
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    // [ifndef gwt] method
    /**
     * Sets a list of attributes taken from the name of the user agent.
     * 
     * @param agentAttributes
     *            A list of attributes taken from the name of the user agent.
     */
    public void setAgentAttributes(Map<String, String> agentAttributes) {
        synchronized (this) {
            Map<String, String> aa = getAgentAttributes();
            aa.clear();
            aa.putAll(agentAttributes);
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the list of product tokens from the user agent name.
     * 
     * @param agentProducts
     *            The list of product tokens from the user agent name.
     */
    public void setAgentProducts(List<Product> agentProducts) {
        synchronized (this) {
            List<Product> ap = getAgentProducts();
            ap.clear();
            ap.addAll(agentProducts);
        }
    }

    // [ifndef gwt] method
    /**
     * Indicates if the identifier or principal has been authenticated. The
     * application is responsible for updating this property, relying on a
     * {@link org.restlet.security.Authenticator} or manually.
     * 
     * @param authenticated
     *            True if the identifier or principal has been authenticated.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    // [ifndef gwt] method
    /**
     * Sets the new client certificates.
     * 
     * @param certificates
     *            The client certificates.
     * @see #getCertificates()
     */
    public void setCertificates(
            List<java.security.cert.Certificate> certificates) {
        synchronized (this) {
            List<java.security.cert.Certificate> fa = getCertificates();
            fa.clear();
            fa.addAll(certificates);
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the SSL Cipher Suite, if available and accessible.
     * 
     * @param cipherSuite
     *            The SSL Cipher Suite, if available and accessible.
     */
    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    // [ifndef gwt] method
    /**
     * Sets the client expectations.
     * 
     * @param expectations
     *            The client expectations.
     */
    public void setExpectations(List<org.restlet.data.Expectation> expectations) {
        synchronized (this) {
            List<org.restlet.data.Expectation> e = getExpectations();
            e.clear();
            e.addAll(expectations);
        }
    }

    /**
     * Sets the list of forwarded IP addresses.
     * 
     * @param forwardedAddresses
     *            The list of forwarded IP addresses.
     * @see #getForwardedAddresses()
     */
    public void setForwardedAddresses(List<String> forwardedAddresses) {
        synchronized (this) {
            List<String> fa = getForwardedAddresses();
            fa.clear();
            fa.addAll(forwardedAddresses);
        }
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
     * Sets the additional client principals.
     * 
     * @param principals
     *            The additional client principals.
     * @see #getPrincipals()
     */
    public void setPrincipals(List<java.security.Principal> principals) {
        synchronized (this) {
            List<java.security.Principal> fa = getPrincipals();
            fa.clear();
            fa.addAll(principals);
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the authenticated user roles.
     * 
     * @param roles
     *            The authenticated user roles.
     */
    public void setRoles(List<org.restlet.security.Role> roles) {
        synchronized (this) {
            List<org.restlet.security.Role> r = getRoles();
            r.clear();
            r.addAll(roles);
        }
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
