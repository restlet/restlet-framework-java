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

package org.restlet.ext.sip;

import org.restlet.data.Reference;

/**
 * Information on a SIP contact. Used by the SIP "Contact" header.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class ContactInfo extends Address {

    /** The delay of expiration. */
    private String expires;

    /** The quality/preference level. */
    private float quality;

    /**
     * Default constructor.
     */
    public ContactInfo() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     */
    public ContactInfo(Reference reference) {
        super(reference);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     * @param displayName
     *            The name displayed.
     */
    public ContactInfo(Reference reference, String displayName) {
        super(reference, displayName);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     * @param expires
     *            The delay of expiration.
     * @param quality
     *            The quality/preference level.
     */
    public ContactInfo(Reference reference, String expires, int quality) {
        super(reference);
        this.expires = expires;
        this.quality = quality;
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     * @param displayName
     *            The name displayed.
     * @param expires
     *            The delay of expiration.
     * @param quality
     *            The quality/preference level.
     */
    public ContactInfo(Reference reference, String displayName, String expires,
            int quality) {
        super(reference, displayName);
        this.expires = expires;
        this.quality = quality;
    }

    /**
     * Returns the delay of expiration.
     * 
     * @return The delay of expiration.
     */
    public String getExpires() {
        return expires;
    }

    /**
     * Returns the quality/preference level.
     * 
     * @return The quality/preference level.
     */
    public float getQuality() {
        return quality;
    }

    /**
     * Sets the delay of expiration.
     * 
     * @param expires
     *            The delay of expiration.
     */
    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * Sets the quality/preference level.
     * 
     * @param quality
     *            The quality/preference level.
     */
    public void setQuality(float quality) {
        this.quality = quality;
    }

}
