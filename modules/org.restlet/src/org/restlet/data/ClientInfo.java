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

package org.restlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.util.Engine;
import org.restlet.util.Template;
import org.restlet.util.Variable;

/**
 * Client specific data related to a call.<br>
 * When extracted from a request, most of these data are directly taken from the
 * underlying headers. There are some exceptions: agentAttributes and
 * mainAgentProduct which are taken from the agent name (for example the
 * "user-agent" header for HTTP requests).<br>
 * As described by the HTTP specification, the "user-agent" can be seen as a
 * ordered list of products name (ie a name and a version) and/or comments.<br>
 * Each HTTP client (mainly browsers and web crawlers) defines its own
 * "user-agent" header which can be seen as the "signature" of the client.
 * Unfortunately, there is no rule to identify clearly a kind a client and its
 * version (let's say firefox 2.x, Internet Explorer IE 7.0, Opera, etc)
 * according to its signature. Each signature follow its own rules which may
 * vary according to the version of the client.<br>
 * In order to help retrieving interesting data such as product name (Firefox,
 * IE, etc), version, operating system, Restlet users has the ability to define
 * their own way to extract data from the "user-agent" header. It is based on a
 * list of templates declared in a file called "agent.properties" and located in
 * the classpath in the sub directory "org/restlet/data". Each template
 * describes a typical user-agent string and allows to use predefined variables
 * that help to retrieve the content of the agent name, version, operating
 * system.<br>
 * The "user-agent" string is confronted to the each template from the beginning
 * of the property file to the end. The loop stops at the first matched
 * template.<br>
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
 * At this time, four predefined variables are used:<br>
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
 * <td>Comment string</td>
 * </tr>
 * </table><br>
 * These variables are used to generate a {@link Product} instance with the main
 * data (name, version, comment). This instance is accessible via the
 * {@link ClientInfo#getMainAgentProduct()} method. All other variables used in
 * the template aims at catching a sequence of characters and are accessible via
 * the {@link ClientInfo#getAgentAttributes()} method.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ClientInfo {
    /** The character set preferences. */
    private volatile List<Preference<CharacterSet>> acceptedCharacterSets;

    /** The encoding preferences. */
    private volatile List<Preference<Encoding>> acceptedEncodings;

    /** The language preferences. */
    private volatile List<Preference<Language>> acceptedLanguages;

    /** The media preferences. */
    private volatile List<Preference<MediaType>> acceptedMediaTypes;

    /** The IP addresses. */
    private volatile List<String> addresses;

    /** The agent name. */
    private volatile String agent;

    /** The main product data taken from the agent name. */
    private volatile Product agentMainProduct;

    /** The attributes data taken from the agent name. */
    private volatile Map<String, String> agentAttributes;

    /** The list of product tokens taken from the agent name. */
    private volatile List<Product> agentProducts;

    /** The port number. */
    private volatile int port;

    /**
     * Constructor.
     */
    public ClientInfo() {
        this.addresses = null;
        this.agent = null;
        this.port = -1;
        this.acceptedCharacterSets = null;
        this.acceptedEncodings = null;
        this.acceptedLanguages = null;
        this.acceptedMediaTypes = null;
        this.agentProducts = null;
    }

    /**
     * Returns the modifiable list of character set preferences. Creates a new
     * instance if no one has been set.
     * 
     * @return The character set preferences.
     */
    public List<Preference<CharacterSet>> getAcceptedCharacterSets() {
        // Lazy initialization with double-check.
        List<Preference<CharacterSet>> a = this.acceptedCharacterSets;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedCharacterSets;
                if (a == null)
                    this.acceptedCharacterSets = a = new ArrayList<Preference<CharacterSet>>();
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of encoding preferences. Creates a new
     * instance if no one has been set.
     * 
     * @return The encoding preferences.
     */
    public List<Preference<Encoding>> getAcceptedEncodings() {
        // Lazy initialization with double-check.
        List<Preference<Encoding>> a = this.acceptedEncodings;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedEncodings;
                if (a == null)
                    this.acceptedEncodings = a = new ArrayList<Preference<Encoding>>();
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of language preferences. Creates a new
     * instance if no one has been set.
     * 
     * @return The language preferences.
     */
    public List<Preference<Language>> getAcceptedLanguages() {
        // Lazy initialization with double-check.
        List<Preference<Language>> a = this.acceptedLanguages;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedLanguages;
                if (a == null)
                    this.acceptedLanguages = a = new ArrayList<Preference<Language>>();
            }
        }
        return a;
    }

    /**
     * Returns the modifiable list of media type preferences. Creates a new
     * instance if no one has been set.
     * 
     * @return The media type preferences.
     */
    public List<Preference<MediaType>> getAcceptedMediaTypes() {
        // Lazy initialization with double-check.
        List<Preference<MediaType>> a = this.acceptedMediaTypes;
        if (a == null) {
            synchronized (this) {
                a = this.acceptedMediaTypes;
                if (a == null)
                    this.acceptedMediaTypes = a = new ArrayList<Preference<MediaType>>();
            }
        }
        return a;
    }

    /**
     * Returns the client's IP address which is the first address in the list of
     * client addresses, if this list exists and isn't empty.
     * 
     * @return The client's IP address.
     */
    public String getAddress() {
        return (this.addresses == null) ? null
                : (this.addresses.isEmpty() ? null : this.addresses.get(0));
    }

    /**
     * Returns the modifiable list of client IP addresses.<br>
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
     */
    public List<String> getAddresses() {
        // Lazy initialization with double-check.
        List<String> a = this.addresses;
        if (a == null) {
            synchronized (this) {
                a = this.addresses;
                if (a == null)
                    this.addresses = a = new ArrayList<String>();
            }
        }
        return a;
    }

    /**
     * Returns the agent name (ex: "Noelios-Restlet-Engine/1.1").
     * 
     * @return The agent name.
     */
    public String getAgent() {
        return this.agent;
    }

    /**
     * Returns a list of attributes taken from the name of the user agent.
     * 
     * @return A list of attributes taken from the name of the user agent.
     */
    public Map<String, String> getAgentAttributes() {

        if (this.agentAttributes == null) {
            this.agentAttributes = new HashMap<String, String>();
            Map<String, Object> map = new HashMap<String, Object>();

            // Loop on a list of user-agent templates until a template match
            // the current user-agent string. The list of templates is
            // located in a file named "agent.properties" available on
            // the classpath.
            // Soem defined variables are used in order to catch the name,
            // version and facultative comment. Respectively, these
            // variables are called "agentName", "agentVersion" and
            // "agentComment".
            URL userAgentPropertiesUrl = Engine.getClassLoader().getResource(
                    "org/restlet/data/agent.properties");
            if (userAgentPropertiesUrl != null) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(
                            userAgentPropertiesUrl.openStream(),
                            CharacterSet.UTF_8.getName()));
                    Template template = null;
                    // Predefined variables.
                    Variable agentName = new Variable(Variable.TYPE_TOKEN);
                    Variable agentVersion = new Variable(Variable.TYPE_TOKEN);
                    Variable agentComment = new Variable(Variable.TYPE_COMMENT);
                    Variable agentCommentAttribute = new Variable(
                            Variable.TYPE_COMMENT_ATTRIBUTE);
                    Variable facultativeData = new Variable(Variable.TYPE_ALL,
                            null, false, false);
                    String line = reader.readLine();
                    for (; line != null; line = reader.readLine()) {
                        if (line.trim().length() > 0
                                && !line.trim().startsWith("#")) {
                            template = new Template(line, Template.MODE_EQUALS);
                            // Update the predefined variables.
                            template.getVariables().put("agentName", agentName);
                            template.getVariables().put("agentVersion",
                                    agentVersion);
                            template.getVariables().put("agentComment",
                                    agentComment);
                            template.getVariables().put("agentOs",
                                    agentCommentAttribute);
                            template.getVariables().put("commentAttribute",
                                    agentCommentAttribute);
                            template.getVariables().put("facultativeData",
                                    facultativeData);
                            // Parse the template
                            if (template.parse(getAgent(), map) > -1) {
                                for (String key : map.keySet()) {
                                    this.agentAttributes.put(key, (String) map
                                            .get(key));
                                }
                                break;
                            }
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    return this.agentAttributes;
                }
            }
        }

        return this.agentAttributes;
    }

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
     * Returns the name of the user agent.
     * 
     * @return The name of the user agent.
     */
    public String getAgentName() {
        Product product = getMainAgentProduct();
        if (product != null) {
            return product.getName();
        }

        return null;
    }

    /**
     * Returns the list of product tokens from the user agent name.
     * 
     * @return The list of product tokens from the user agent name.
     */
    public List<Product> getAgentProducts() {
        if (this.agentProducts == null) {
            this.agentProducts = Engine.getInstance()
                    .parseUserAgent(getAgent());
        }
        return this.agentProducts;
    }

    /**
     * Returns the version of the user agent.
     * 
     * @return The version of the user agent.
     */
    public String getAgentVersion() {
        Product product = getMainAgentProduct();
        if (product != null) {
            return product.getVersion();
        }
        return null;

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

    /**
     * Returns the best variant for a given resource according the the client
     * preferences: accepted languages, accepted character sets, accepted media
     * types and accepted encodings.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param variants
     *                The list of variants to compare.
     * @param defaultLanguage
     *                The default language.
     * @return The best variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(List<Variant> variants,
            Language defaultLanguage) {
        return Engine.getInstance().getPreferredVariant(this, variants,
                defaultLanguage);
    }

    /**
     * Returns the best variant for a given resource according the the client
     * preferences.<br>
     * A default language is provided in case the resource's variants don't
     * match the client preferences.
     * 
     * @param resource
     *                The resource for which the best representation needs to be
     *                set.
     * @param defaultLanguage
     *                The default language.
     * @return The best variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(Resource resource,
            Language defaultLanguage) {
        return getPreferredVariant(resource.getVariants(), defaultLanguage);
    }

    /**
     * Sets the character set preferences.
     * 
     * @param acceptedCharacterSets
     *                The character set preferences.
     */
    public void setAcceptedCharacterSets(
            List<Preference<CharacterSet>> acceptedCharacterSets) {
        this.acceptedCharacterSets = acceptedCharacterSets;
    }

    /**
     * Sets the encoding preferences.
     * 
     * @param acceptedEncodings
     *                The encoding preferences.
     */
    public void setAcceptedEncodings(
            List<Preference<Encoding>> acceptedEncodings) {
        this.acceptedEncodings = acceptedEncodings;
    }

    /**
     * Sets the language preferences.
     * 
     * @param acceptedLanguages
     *                The language preferences.
     */
    public void setAcceptedLanguages(
            List<Preference<Language>> acceptedLanguages) {
        this.acceptedLanguages = acceptedLanguages;
    }

    /**
     * Sets the media type preferences.
     * 
     * @param acceptedMediaTypes
     *                The media type preferences.
     */
    public void setAcceptedMediaTypes(
            List<Preference<MediaType>> acceptedMediaTypes) {
        this.acceptedMediaTypes = acceptedMediaTypes;
    }

    /**
     * Sets the client's IP address.
     * 
     * @param address
     *                The client's IP address.
     */
    public void setAddress(String address) {
        if (getAddresses().isEmpty()) {
            getAddresses().add(address);
        } else {
            getAddresses().set(0, address);
        }
    }

    /**
     * Sets the list of client IP addresses.
     * 
     * @param addresses
     *                The list of client IP addresses.
     */
    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    /**
     * Sets the agent name (ex: "Noelios Restlet Engine/1.1").
     * 
     * @param agent
     *                The agent name.
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * Sets the port number which sent the call.
     * 
     * @param port
     *                The port number which sent the call.
     */
    public void setPort(int port) {
        this.port = port;
    }

}
