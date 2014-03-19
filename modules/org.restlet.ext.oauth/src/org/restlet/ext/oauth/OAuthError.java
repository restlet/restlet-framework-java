/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.ext.oauth;

/**
 * Utility class for formating OAuth errors
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @author Kristoffer Gronowski
 * @see <a href="http://tools.ietf.org/html/rfc6749"> The OAuth 2.0
 *      Authorization Framework</a>
 * @see <a href="http://tools.ietf.org/html/rfc6750"> Bearer Token Usage</a>
 */
public enum OAuthError {

    access_denied, // 4.1.2.1 & 4.2.2.1
    invalid_client, // 5.2
    invalid_grant, // 5.2
    invalid_request, // 4.1.2.1 & 4.2.2.1 & 5.2
    invalid_scope, // 4.1.2.1 & 4.2.2.1 & 5.2
    unauthorized_client, // 4.1.2.1 & 4.2.2.1 & 5.2
    unsupported_grant_type, // 5.2
    unsupported_response_type, // 4.1.2.1 & 4.2.2.1
    server_error, // 4.1.2.1 & 4.2.2.1

    invalid_token, // Bearer 3.1
    insufficient_scope // Bearer 3.1
}
