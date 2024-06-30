/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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








    /** The forwarded IP addresses. */
    private volatile List<String> forwardedAddresses;

    /** The email address of the human user controlling the user agent. */
    private volatile String from;

    /** The port number. */
    private volatile int port;




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




}
