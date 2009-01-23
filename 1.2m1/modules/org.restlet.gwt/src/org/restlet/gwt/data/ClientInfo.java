/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.gwt.util.Engine;

/**
 * Client specific data related to a call.
 * 
 * @author Jerome Louvel
 */
public final class ClientInfo {
    /** The character set preferences. */
    private List<Preference<CharacterSet>> acceptedCharacterSets;

    /** The encoding preferences. */
    private List<Preference<Encoding>> acceptedEncodings;

    /** The language preferences. */
    private List<Preference<Language>> acceptedLanguages;

    /** The media preferences. */
    private List<Preference<MediaType>> acceptedMediaTypes;

    /** The IP addresses. */
    private List<String> addresses;

    /** The agent name. */
    private String agent;

    /** The list of product tokens taken from the agent name. */
    private List<Product> agentProducts;

    /** The port number. */
    private int port;

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
                if (a == null) {
                    this.acceptedCharacterSets = a = new ArrayList<Preference<CharacterSet>>();
                }
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
                if (a == null) {
                    this.acceptedEncodings = a = new ArrayList<Preference<Encoding>>();
                }
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
                if (a == null) {
                    this.acceptedLanguages = a = new ArrayList<Preference<Language>>();
                }
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
                if (a == null) {
                    this.acceptedMediaTypes = a = new ArrayList<Preference<MediaType>>();
                }
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
                if (a == null) {
                    this.addresses = a = new ArrayList<String>();
                }
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
     * Returns the port number which sent the call. If no port is specified, -1
     * is returned.
     * 
     * @return The port number which sent the call.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Sets the character set preferences.
     * 
     * @param acceptedCharacterSets
     *            The character set preferences.
     */
    public void setAcceptedCharacterSets(
            List<Preference<CharacterSet>> acceptedCharacterSets) {
        this.acceptedCharacterSets = acceptedCharacterSets;
    }

    /**
     * Sets the encoding preferences.
     * 
     * @param acceptedEncodings
     *            The encoding preferences.
     */
    public void setAcceptedEncodings(
            List<Preference<Encoding>> acceptedEncodings) {
        this.acceptedEncodings = acceptedEncodings;
    }

    /**
     * Sets the language preferences.
     * 
     * @param acceptedLanguages
     *            The language preferences.
     */
    public void setAcceptedLanguages(
            List<Preference<Language>> acceptedLanguages) {
        this.acceptedLanguages = acceptedLanguages;
    }

    /**
     * Sets the media type preferences.
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
     *            The list of client IP addresses.
     */
    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    /**
     * Sets the agent name (ex: "Noelios Restlet Engine/1.1").
     * 
     * @param agent
     *            The agent name.
     */
    public void setAgent(String agent) {
        this.agent = agent;
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
