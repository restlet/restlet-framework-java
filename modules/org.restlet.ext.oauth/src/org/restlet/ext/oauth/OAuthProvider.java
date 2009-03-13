/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.ext.oauth;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

/**
 * Manages OAuth state and operations.
 * 
 * @author Adam Rosien
 */
public abstract class OAuthProvider {

    /**
     * Add a consumer.
     * 
     * @param key
     * @param consumer
     */
    abstract void addConsumer(String key, OAuthConsumer consumer);

    /**
     * Generate an access token.
     * 
     * @param accessor
     */
    abstract void generateAccessToken(OAuthAccessor accessor);

    /**
     * Generate a request token.
     * 
     * @param accessor
     */
    abstract void generateRequestToken(OAuthAccessor accessor);

    /**
     * Get the accessor from a request.
     * 
     * @param requestMessage
     * @return Accessor, null if none found.
     */
    abstract OAuthAccessor getAccessor(OAuthMessage requestMessage);

    /**
     * Get the consumer from a request.
     * 
     * @param requestMessage
     * @return Consumer, null if none found.
     */
    abstract OAuthConsumer getConsumer(OAuthMessage requestMessage);

    /**
     * Authorize a request token for a user.
     * 
     * @param accessor
     * @param userId
     */
    abstract void markAsAuthorized(OAuthAccessor accessor, String userId);

}
