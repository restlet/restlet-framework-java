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

package org.restlet.ext.oauth;

/**
 * Utility class for formating OAuth errors
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @author Kristoffer Gronowski
 * @see <a href="http://tools.ietf.org/html/rfc6749"> The OAuth 2.0 Authorization Framework</a>
 * @see <a href="http://tools.ietf.org/html/rfc6750"> Bearer Token Usage</a>
 */
public enum OAuthError {

    access_denied, // 4.1.2.1 & 4.2.2.1
    insufficient_scope, // Bearer 3.1, // 5.2
    invalid_client, // 5.2
    invalid_grant, // 4.1.2.1 & 4.2.2.1 & 5.2
    invalid_request, // 4.1.2.1 & 4.2.2.1 & 5.2
    invalid_scope, // 4.1.2.1 & 4.2.2.1 & 5.2
    invalid_token, // 5.2
    server_error, // 4.1.2.1 & 4.2.2.1
    unauthorized_client, // 4.1.2.1 & 4.2.2.1
    unsupported_grant_type, // Bearer 3.1
    unsupported_response_type
}
