/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.util.Engine;

/**
 * Client specific data related to a call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ClientInfo {
    /** The IP addresses. */
    private List<String> addresses;

    /** The agent name. */
    private String agent;

    /** The port number. */
    private int port;

    /** The character set preferences. */
    private List<Preference<CharacterSet>> characterSetPrefs;

    /** The encoding preferences. */
    private List<Preference<Encoding>> encodingPrefs;

    /** The language preferences. */
    private List<Preference<Language>> languagePrefs;

    /** The media preferences. */
    private List<Preference<MediaType>> mediaTypePrefs;

    /**
     * Constructor.
     */
    public ClientInfo() {
        this.addresses = null;
        this.agent = null;
        this.port = -1;
        this.characterSetPrefs = null;
        this.encodingPrefs = null;
        this.languagePrefs = null;
        this.mediaTypePrefs = null;
    }

    /**
     * Returns the character set preferences.
     * 
     * @return The character set preferences.
     */
    public List<Preference<CharacterSet>> getAcceptedCharacterSets() {
        if (this.characterSetPrefs == null)
            this.characterSetPrefs = new ArrayList<Preference<CharacterSet>>();
        return this.characterSetPrefs;
    }

    /**
     * Returns the encoding preferences.
     * 
     * @return The encoding preferences.
     */
    public List<Preference<Encoding>> getAcceptedEncodings() {
        if (this.encodingPrefs == null)
            this.encodingPrefs = new ArrayList<Preference<Encoding>>();
        return this.encodingPrefs;
    }

    /**
     * Returns the language preferences.
     * 
     * @return The language preferences.
     */
    public List<Preference<Language>> getAcceptedLanguages() {
        if (this.languagePrefs == null)
            this.languagePrefs = new ArrayList<Preference<Language>>();
        return this.languagePrefs;
    }

    /**
     * Returns the media type preferences.
     * 
     * @return The media type preferences.
     */
    public List<Preference<MediaType>> getAcceptedMediaTypes() {
        if (this.mediaTypePrefs == null)
            this.mediaTypePrefs = new ArrayList<Preference<MediaType>>();
        return this.mediaTypePrefs;
    }

    /**
     * Returns the client's IP address.
     * 
     * @return The client's IP address.
     */
    public String getAddress() {
        return (this.addresses == null) ? null
                : (this.addresses.isEmpty() ? null : this.addresses.get(0));
    }

    /**
     * Returns the list of client IP addresses.<br/> The first address is the
     * one of the immediate client component as returned by the
     * getClientAdress() method and the last address should correspond to the
     * origin client (frequently a user agent). This is useful when the user
     * agent is separated from the origin server by a chain of intermediary
     * components.
     * 
     * @return The client IP addresses.
     */
    public List<String> getAddresses() {
        if (this.addresses == null)
            this.addresses = new ArrayList<String>();
        return this.addresses;
    }

    /**
     * Returns the agent name (ex: "Noelios Restlet Engine/1.0").
     * 
     * @return The agent name.
     */
    public String getAgent() {
        return this.agent;
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
     * types and accepted encodings.<br/>A default language is provided in case
     * the variants don't match the client preferences.
     * 
     * @param variants
     *            The list of variants to compare.
     * @param defaultLanguage
     *            The default language.
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
     * preferences.<br/>A default language is provided in case the resource's
     * variants don't match the client preferences.
     * 
     * @param resource
     *            The resource for which the best representation needs to be
     *            set.
     * @param defaultLanguage
     *            The default language.
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
     * Sets the agent name (ex: "Noelios Restlet Engine/1.0").
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
