/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.connector;


/**
 * HTTP connector call.
 */
public interface HttpCall
{
   // ---------------------
   // ---  Status codes ---
   // ---------------------

   public static final int STATUS_SUCCESS_CREATED = 201;
   public static final int STATUS_REDIRECTION_MULTIPLE_CHOICES = 300;
   public static final int STATUS_REDIRECTION_MOVED_PERMANENTLY = 301;
   public static final int STATUS_REDIRECTION_FOUND = 302;
   public static final int STATUS_REDIRECTION_SEE_OTHER = 303;
   public static final int STATUS_REDIRECTION_TEMPORARY_REDIRECT = 307;
   public static final int STATUS_CLIENT_ERROR_UNAUTHORIZED = 401;

   
   // ---------------------
   // ---  Header names ---
   // ---------------------

   public static final String HEADER_ACCEPT = "Accept";
   public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
   public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
   public static final String HEADER_AUTHORIZATION = "Authorization";
   public static final String HEADER_COOKIE = "Cookie";
   public static final String HEADER_CONTENT_LENGTH = "Content-Length";
   public static final String HEADER_CONTENT_TYPE = "Content-Type";
   public static final String HEADER_ETAG = "ETag";
   public static final String HEADER_EXPIRES = "Expires";
   public static final String HEADER_IF_MATCH = "If-Match";
   public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
   public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
   public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
   public static final String HEADER_LAST_MODIFIED = "Last-Modified";
   public static final String HEADER_LOCATION = "Location";
   public static final String HEADER_REFERRER = "Referer";
   public static final String HEADER_USER_AGENT = "User-Agent";
   public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
   
}
