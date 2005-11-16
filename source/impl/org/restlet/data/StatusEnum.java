/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Enumeration of call status.
 */
public enum StatusEnum implements Status
{
   INFO_CONTINUE,
   INFO_SWITCHING_PROTOCOL,
   INFO_PROCESSING,
   
   SUCCESS_OK,
   SUCCESS_CREATED,
   SUCCESS_ACCEPTED,
   SUCCESS_NON_AUTHORITATIVE,
   SUCCESS_NO_CONTENT,
   SUCCESS_RESET_CONTENT,
   SUCCESS_PARTIAL_CONTENT,
   SUCCESS_MULTI_STATUS,

   REDIRECTION_MULTIPLE_CHOICES,
   REDIRECTION_MOVED_PERMANENTLY,
   REDIRECTION_MOVED_TEMPORARILY,
   REDIRECTION_FOUND,
   REDIRECTION_SEE_OTHER,
   REDIRECTION_NOT_MODIFIED,
   REDIRECTION_USE_PROXY,  
   REDIRECTION_TEMPORARY_REDIRECT,

   CLIENT_ERROR_BAD_REQUEST,
   CLIENT_ERROR_UNAUTHORIZED,
   CLIENT_ERROR_PAYMENT_REQUIRED,
   CLIENT_ERROR_FORBIDDEN,
   CLIENT_ERROR_NOT_FOUND,
   CLIENT_ERROR_METHOD_NOT_ALLOWED,
   CLIENT_ERROR_NOT_ACCEPTABLE,
   CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED,
   CLIENT_ERROR_REQUEST_TIMEOUT,
   CLIENT_ERROR_CONFLICT,
   CLIENT_ERROR_GONE,
   CLIENT_ERROR_LENGTH_REQUIRED,
   CLIENT_ERROR_PRECONDITION_FAILED,
   CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE,
   CLIENT_ERROR_REQUEST_URI_TOO_LONG,
   CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
   CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE,
   CLIENT_ERROR_EXPECTATION_FAILED,
   CLIENT_ERROR_UNPROCESSABLE_ENTITY,
   CLIENT_ERROR_LOCKED,
   CLIENT_ERROR_FAILED_DEPENDENCY,

   SERVER_ERROR_INTERNAL,
   SERVER_ERROR_NOT_IMPLEMENTED,
   SERVER_ERROR_BAD_GATEWAY,
   SERVER_ERROR_SERVICE_UNAVAILABLE,
   SERVER_ERROR_GATEWAY_TIMEOUT,
   SERVER_ERROR_HTTP_VERSION_NOT_SUPPORTED,
   SERVER_ERROR_INSUFFICIENT_STORAGE;

   /**
    * Returns the HTTP code.
    * @return The HTTP code.
    */
   public int getHttpCode()
   {
      int result = 0;
      
      switch(this)
      {
         case INFO_CONTINUE:
            result = 100;
            break;
         case INFO_SWITCHING_PROTOCOL:
            result = 101;
            break;
         case INFO_PROCESSING:
            result = 102;
            break;
            
         case SUCCESS_OK:
            result = 200;
            break;
         case SUCCESS_CREATED:
            result = 201;
            break;
         case SUCCESS_ACCEPTED:
            result = 202;
            break;
         case SUCCESS_NON_AUTHORITATIVE:
            result = 203;
            break;
         case SUCCESS_NO_CONTENT:
            result = 204;
            break;
         case SUCCESS_RESET_CONTENT:
            result = 205;
            break;
         case SUCCESS_PARTIAL_CONTENT:
            result = 206;
            break;
         case SUCCESS_MULTI_STATUS:
            result = 207;
            break;
            
         case REDIRECTION_MULTIPLE_CHOICES: 
            result = 300; 
            break;
         case REDIRECTION_MOVED_PERMANENTLY: 
            result = 301; 
            break;
         case REDIRECTION_MOVED_TEMPORARILY: 
         case REDIRECTION_FOUND: 
            result = 302; 
            break;
         case REDIRECTION_SEE_OTHER: 
            result = 303; 
            break;
         case REDIRECTION_NOT_MODIFIED: 
            result = 304; 
            break;
         case REDIRECTION_USE_PROXY:
            result = 305;
            break;
         case REDIRECTION_TEMPORARY_REDIRECT: 
            result = 307; 
            break;
            
         case CLIENT_ERROR_BAD_REQUEST: 
            result = 400; 
            break;
         case CLIENT_ERROR_UNAUTHORIZED: 
            result = 401; 
            break;
         case CLIENT_ERROR_PAYMENT_REQUIRED:
            result = 402;
            break;
         case CLIENT_ERROR_FORBIDDEN: 
            result = 403; 
            break;
         case CLIENT_ERROR_NOT_FOUND: 
            result = 404; 
            break;
         case CLIENT_ERROR_METHOD_NOT_ALLOWED: 
            result = 405; 
            break;
         case CLIENT_ERROR_NOT_ACCEPTABLE: 
            result = 406; 
            break;
         case CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED:
            result = 407;
            break;
         case CLIENT_ERROR_REQUEST_TIMEOUT:
            result = 408;
            break;
         case CLIENT_ERROR_CONFLICT: 
            result = 409; 
            break;
         case CLIENT_ERROR_GONE: 
            result = 410; 
            break;
         case CLIENT_ERROR_LENGTH_REQUIRED:
            result = 411;
            break;
         case CLIENT_ERROR_PRECONDITION_FAILED:
            result = 412;
            break;
         case CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE: 
            result = 413; 
            break;
         case CLIENT_ERROR_REQUEST_URI_TOO_LONG:
            result = 414;
            break;
         case CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE: 
            result = 415; 
            break;
         case CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE:
            result = 416;
            break;
         case CLIENT_ERROR_EXPECTATION_FAILED:
            result = 417;
            break;
         case CLIENT_ERROR_UNPROCESSABLE_ENTITY:
            result = 422;
            break;
         case CLIENT_ERROR_LOCKED:
            result = 423;
            break;
         case CLIENT_ERROR_FAILED_DEPENDENCY:
            result = 424;
            break;
            
         case SERVER_ERROR_INTERNAL: 
            result = 500; 
            break;
         case SERVER_ERROR_NOT_IMPLEMENTED: 
            result = 501; 
            break;
         case SERVER_ERROR_BAD_GATEWAY:
            result = 402;
            break;
         case SERVER_ERROR_SERVICE_UNAVAILABLE: 
            result = 503; 
            break;
         case SERVER_ERROR_GATEWAY_TIMEOUT:
            result = 504;
            break;
         case SERVER_ERROR_HTTP_VERSION_NOT_SUPPORTED:
            result = 505;
            break;
         case SERVER_ERROR_INSUFFICIENT_STORAGE:
            result = 507;
            break;
      }
      
      return result;
   }

   /**
    * Returns the URI of the specification describing the status.
    * @return The URI of the specification describing the status.
    */
   public String getUri()
   {
      String result = null;
      String httpRoot = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html";
      String webDavRoot = "http://www.webdav.org/specs/rfc2518.html";
      
      switch(this)
      {
         case INFO_CONTINUE:
            result = httpRoot + "#sec10.1.1";
            break;
         case INFO_SWITCHING_PROTOCOL:
            result = httpRoot + "#sec10.1.2";
            break;
         case INFO_PROCESSING:
            result = webDavRoot + "#STATUS_102";
            break;
            
         case SUCCESS_OK:
            result = httpRoot + "#sec10.2.1";
            break;
         case SUCCESS_CREATED:
            result = httpRoot + "#sec10.2.2";
            break;
         case SUCCESS_ACCEPTED:
            result = httpRoot + "#sec10.2.3";
            break;
         case SUCCESS_NON_AUTHORITATIVE:
            result = httpRoot + "#sec10.2.4";
            break;
         case SUCCESS_NO_CONTENT:
            result = httpRoot + "#sec10.2.5";
            break;
         case SUCCESS_RESET_CONTENT:
            result = httpRoot + "#sec10.2.6";
            break;
         case SUCCESS_PARTIAL_CONTENT:
            result = httpRoot + "#sec10.2.7";
            break;
         case SUCCESS_MULTI_STATUS:
            result = webDavRoot + "#STATUS_207";
            break;
            
         case REDIRECTION_MULTIPLE_CHOICES: 
            result = httpRoot + "#sec10.3.1";
            break;
         case REDIRECTION_MOVED_PERMANENTLY: 
            result = httpRoot + "#sec10.3.2";
            break;
         case REDIRECTION_MOVED_TEMPORARILY: 
         case REDIRECTION_FOUND: 
            result = httpRoot + "#sec10.3.3";
            break;
         case REDIRECTION_SEE_OTHER: 
            result = httpRoot + "#sec10.3.4";
            break;
         case REDIRECTION_NOT_MODIFIED: 
            result = httpRoot + "#sec10.3.5";
            break;
         case REDIRECTION_USE_PROXY:
            result = httpRoot + "#sec10.3.6";
            break;
         case REDIRECTION_TEMPORARY_REDIRECT: 
            result = httpRoot + "#sec10.3.8";
            break;
            
         case CLIENT_ERROR_BAD_REQUEST: 
            result = httpRoot + "#sec10.4.1";
            break;
         case CLIENT_ERROR_UNAUTHORIZED: 
            result = httpRoot + "#sec10.4.2";
            break;
         case CLIENT_ERROR_PAYMENT_REQUIRED: 
            result = httpRoot + "#sec10.4.3";
            break;
         case CLIENT_ERROR_FORBIDDEN: 
            result = httpRoot + "#sec10.4.4";
            break;
         case CLIENT_ERROR_NOT_FOUND: 
            result = httpRoot + "#sec10.4.5";
            break;
         case CLIENT_ERROR_METHOD_NOT_ALLOWED: 
            result = httpRoot + "#sec10.4.6";
            break;
         case CLIENT_ERROR_NOT_ACCEPTABLE: 
            result = httpRoot + "#sec10.4.7";
            break;
         case CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED: 
            result = httpRoot + "#sec10.4.8";
            break;
         case CLIENT_ERROR_REQUEST_TIMEOUT: 
            result = httpRoot + "#sec10.4.9";
            break;
         case CLIENT_ERROR_CONFLICT: 
            result = httpRoot + "#sec10.4.10";
            break;
         case CLIENT_ERROR_GONE: 
            result = httpRoot + "#sec10.4.11";
            break;
         case CLIENT_ERROR_LENGTH_REQUIRED: 
            result = httpRoot + "#sec10.4.12";
            break;
         case CLIENT_ERROR_PRECONDITION_FAILED: 
            result = httpRoot + "#sec10.4.13";
            break;
         case CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE: 
            result = httpRoot + "#sec10.4.14";
            break;
         case CLIENT_ERROR_REQUEST_URI_TOO_LONG: 
            result = httpRoot + "#sec10.4.15";
            break;
         case CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE: 
            result = httpRoot + "#sec10.4.16";
            break;
         case CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE: 
            result = httpRoot + "#sec10.4.17";
            break;
         case CLIENT_ERROR_EXPECTATION_FAILED: 
            result = httpRoot + "#sec10.4.18";
            break;
         case CLIENT_ERROR_UNPROCESSABLE_ENTITY:
            result = webDavRoot + "#STATUS_422";
            break;
         case CLIENT_ERROR_LOCKED:
            result = webDavRoot + "#STATUS_423";
            break;
         case CLIENT_ERROR_FAILED_DEPENDENCY:
            result = webDavRoot + "#STATUS_424";
            break;
            
            
         case SERVER_ERROR_INTERNAL: 
            result = httpRoot + "#sec10.5.1";
            break;
         case SERVER_ERROR_NOT_IMPLEMENTED: 
            result = httpRoot + "#sec10.5.2";
            break;
         case SERVER_ERROR_BAD_GATEWAY: 
            result = httpRoot + "#sec10.5.3";
            break;
         case SERVER_ERROR_SERVICE_UNAVAILABLE: 
            result = httpRoot + "#sec10.5.4";
            break;
         case SERVER_ERROR_GATEWAY_TIMEOUT: 
            result = httpRoot + "#sec10.5.5";
            break;
         case SERVER_ERROR_HTTP_VERSION_NOT_SUPPORTED: 
            result = httpRoot + "#sec10.5.6";
            break;
         case SERVER_ERROR_INSUFFICIENT_STORAGE:
            result = webDavRoot + "#STATUS_507";
            break;
      }
      
      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      String result = null;
      
      switch(this)
      {
         case INFO_CONTINUE:
            result = "The client should continue with its request";
            break;
         case INFO_SWITCHING_PROTOCOL:
            result = "The server is willing to change the application protocol being used on this connection";
            break;
         case INFO_PROCESSING:
            result = "Interim response used to inform the client that the server has accepted the complete request, but has not yet completed it";
            break;

         case SUCCESS_OK:
            result = "The request has succeeded";
            break;
         case SUCCESS_CREATED:
            result = "The request has been fulfilled and resulted in a new resource being created";
            break;
         case SUCCESS_ACCEPTED:
            result = "The request has been accepted for processing, but the processing has not been completed";
            break;
         case SUCCESS_NON_AUTHORITATIVE:
            result = "The returned metainformation is not the definitive set as available from the origin server";
            break;
         case SUCCESS_NO_CONTENT:
            result = "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation";
            break;
         case SUCCESS_RESET_CONTENT:
            result = "The server has fulfilled the request and the user agent should reset the document view which caused the request to be sent";
            break;
         case SUCCESS_PARTIAL_CONTENT:
            result = "The server has fulfilled the partial get request for the resource";
            break;
         case SUCCESS_MULTI_STATUS:
            result = "Provides status for multiple independent operations";
            break;
            
         case REDIRECTION_MULTIPLE_CHOICES: 
            result = "The requested resource corresponds to any one of a set of representations";
            break;
         case REDIRECTION_MOVED_PERMANENTLY: 
            result = "The requested resource has been assigned a new permanent URI";
            break;
         case REDIRECTION_MOVED_TEMPORARILY: 
         case REDIRECTION_FOUND: 
            result = "The requested resource resides temporarily under a different URI";
            break;
         case REDIRECTION_SEE_OTHER: 
            result = "The response to the request can be found under a different URI";
            break;
         case REDIRECTION_NOT_MODIFIED: 
            result = "If the client has performed a conditional GET request and access is allowed";
            break;
         case REDIRECTION_USE_PROXY: 
            result = "The requested resource must be accessed through the proxy given by the location field";
            break;
         case REDIRECTION_TEMPORARY_REDIRECT: 
            result = "The requested resource resides temporarily under a different URI";
            break;
            
         case CLIENT_ERROR_BAD_REQUEST: 
            result = "The request could not be understood by the server due to malformed syntax";
            break;
         case CLIENT_ERROR_UNAUTHORIZED: 
            result = "The request requires user authentication";
            break;
         case CLIENT_ERROR_PAYMENT_REQUIRED: 
            result = "This code is reserved for future use";
            break;
         case CLIENT_ERROR_FORBIDDEN: 
            result = "The server understood the request, but is refusing to fulfill it";
            break;
         case CLIENT_ERROR_NOT_FOUND: 
            result = "The server has not found anything matching the request URI";
            break;
         case CLIENT_ERROR_METHOD_NOT_ALLOWED: 
            result = "The method specified in the Request-Line is not allowed for the resource identified by the request URI";
            break;
         case CLIENT_ERROR_NOT_ACCEPTABLE: 
            result = "The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request";
            break;
         case CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED:
            result = "This code is similar to Unauthorized, but indicates that the client must first authenticate itself with the proxy";
            break;
         case CLIENT_ERROR_REQUEST_TIMEOUT: 
            result = "The client did not produce a request within the time that the server was prepared to wait";
            break;
         case CLIENT_ERROR_CONFLICT: 
            result = "The request could not be completed due to a conflict with the current state of the resource";
            break;
         case CLIENT_ERROR_GONE: 
            result = "The requested resource is no longer available at the server and no forwarding address is known";
            break;
         case CLIENT_ERROR_LENGTH_REQUIRED: 
            result = "The server refuses to accept the request without a defined content length";
            break;
         case CLIENT_ERROR_PRECONDITION_FAILED: 
            result = "The precondition given in one or more of the request header fields evaluated to false when it was tested on the server";
            break;
         case CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE: 
            result = "The server is refusing to process a request because the request entity is larger than the server is willing or able to process";
            break;
         case CLIENT_ERROR_REQUEST_URI_TOO_LONG: 
            result = "The server is refusing to service the request because the request URI is longer than the server is willing to interpret";
            break;
         case CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE: 
            result = "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method";
            break;
         case CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE: 
            result = "For byte ranges, this means that the first byte position were greater than the current length of the selected resource";
            break;
         case CLIENT_ERROR_EXPECTATION_FAILED: 
            result = "The expectation given in the request header could not be met by this server";
            break;
         case CLIENT_ERROR_UNPROCESSABLE_ENTITY:
            result = "The server understands the content type of the request entity and the syntax of the request entity is correct but was unable to process the contained instructions";
            break;
         case CLIENT_ERROR_LOCKED:
            result = "The source or destination resource of a method is locked";
            break;
         case CLIENT_ERROR_FAILED_DEPENDENCY:
            result = "The method could not be performed on the resource because the requested action depended on another action and that action failed";
            break;
            
         case SERVER_ERROR_INTERNAL: 
            result = "The server encountered an unexpected condition which prevented it from fulfilling the request";
            break;
         case SERVER_ERROR_NOT_IMPLEMENTED: 
            result = "The server does not support the functionality required to fulfill the request";
            break;
         case SERVER_ERROR_BAD_GATEWAY: 
            result = "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request";
            break;
         case SERVER_ERROR_SERVICE_UNAVAILABLE: 
            result = "The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
            break;
         case SERVER_ERROR_GATEWAY_TIMEOUT: 
            result = "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request";
            break;
         case SERVER_ERROR_HTTP_VERSION_NOT_SUPPORTED: 
            result = "The server does not support, or refuses to support, the HTTP protocol version that was used in the request message";
            break;
         case SERVER_ERROR_INSUFFICIENT_STORAGE:
            result = "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request";
            break;
      }
      
      return result;
   }
   
   /**
    * Indicates if the status is equal to a given one.
    * @param status  The status to compare to.
    * @return        True if the status is equal to a given one.
    */
   public boolean equals(Status status)
   {
      return getHttpCode() == status.getHttpCode();
   }

}
