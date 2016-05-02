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

package org.restlet.engine.header;

/**
 * Constants related to the HTTP protocol.
 * 
 * @author Jerome Louvel
 */
public final class HeaderConstants {

    // --------------------
    // --- Expectations ---
    // --------------------

    public static final String EXPECT_CONTINUE = "100-continue";

    // ------------------------
    // --- Cache directives ---
    // ------------------------

    public static final String CACHE_NO_CACHE = "no-cache";

    public static final String CACHE_NO_STORE = "no-store";

    public static final String CACHE_MAX_AGE = "max-age";

    public static final String CACHE_MAX_STALE = "max-stale";

    public static final String CACHE_MIN_FRESH = "min-fresh";

    public static final String CACHE_NO_TRANSFORM = "no-transform";

    public static final String CACHE_ONLY_IF_CACHED = "only-if-cached";

    public static final String CACHE_PUBLIC = "public";

    public static final String CACHE_PRIVATE = "private";

    public static final String CACHE_MUST_REVALIDATE = "must-revalidate";

    public static final String CACHE_PROXY_MUST_REVALIDATE = "proxy-revalidate";

    public static final String CACHE_SHARED_MAX_AGE = "s-maxage";

    // ---------------------
    // --- Header names ---
    // ---------------------

    public static final String HEADER_ACCEPT = "Accept";

    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    public static final String HEADER_ACCEPT_PATCH = "Accept-Patch";

    public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";

    public static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    public static final String HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    public static final String HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    public static final String HEADER_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    
    public static final String HEADER_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    public static final String HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    public static final String HEADER_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

    public static final String HEADER_AGE = "Age";

    public static final String HEADER_ALLOW = "Allow";

    public static final String HEADER_AUTHENTICATION_INFO = "Authentication-Info";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_CONNECTION = "Connection";

    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

    public static final String HEADER_CONTENT_LANGUAGE = "Content-Language";

    public static final String HEADER_CONTENT_LENGTH = "Content-Length";

    public static final String HEADER_CONTENT_LOCATION = "Content-Location";

    public static final String HEADER_CONTENT_MD5 = "Content-MD5";

    public static final String HEADER_CONTENT_RANGE = "Content-Range";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String HEADER_COOKIE = "Cookie";

    public static final String HEADER_DATE = "Date";

    public static final String HEADER_ETAG = "ETag";

    public static final String HEADER_EXPECT = "Expect";

    public static final String HEADER_EXPIRES = "Expires";

    public static final String HEADER_FROM = "From";

    public static final String HEADER_HOST = "Host";

    public static final String HEADER_IF_MATCH = "If-Match";

    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";

    public static final String HEADER_IF_RANGE = "If-Range";

    public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    public static final String HEADER_LOCATION = "Location";

    public static final String HEADER_MAX_FORWARDS = "Max-Forwards";

    public static final String HEADER_PRAGMA = "Pragma";

    public static final String HEADER_PROXY_AUTHENTICATE = "Proxy-Authenticate";

    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

    public static final String HEADER_RANGE = "Range";

    public static final String HEADER_REFERRER = "Referer";

    public static final String HEADER_RETRY_AFTER = "Retry-After";

    public static final String HEADER_SERVER = "Server";

    public static final String HEADER_SET_COOKIE = "Set-Cookie";

    public static final String HEADER_SET_COOKIE2 = "Set-Cookie2";

    public static final String HEADER_SLUG = "Slug";

    public static final String HEADER_TRAILER = "Trailer";

    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String HEADER_TRANSFER_EXTENSION = "TE";

    public static final String HEADER_UPGRADE = "Upgrade";

    public static final String HEADER_USER_AGENT = "User-Agent";

    public static final String HEADER_VARY = "Vary";

    public static final String HEADER_VIA = "Via";

    public static final String HEADER_WARNING = "Warning";

    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    public static final String HEADER_X_FORWARDED_PORT = "X-Forwarded-Port";
    
    public static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
    

    public static final String HEADER_X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

    // -------------------------
    // --- Attribute names ---
    // -------------------------

    public static final String ATTRIBUTE_HEADERS = "org.restlet.http.headers";

    public static final String ATTRIBUTE_VERSION = "org.restlet.http.version";

    public static final String ATTRIBUTE_HTTPS_KEY_SIZE = "org.restlet.https.keySize";

    public static final String ATTRIBUTE_HTTPS_SSL_SESSION_ID = "org.restlet.https.sslSessionId";
}
