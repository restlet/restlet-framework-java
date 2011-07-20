// [ifdef nodejs] uncomment
//var jsclass = require("jsclass");
//var util = require("util");
//var http = require("http");
//var libxmljs = require("libxmljs");
// [enddef]

String.prototype.equalsIgnoreCase = function(arg) {               
    return (new String(this.toLowerCase())
             ==(new String(arg)).toLowerCase());
};
String.prototype.equals = function(arg) {
	return (this.toString()==arg.toString());
};


var Context = new JS.Class({
	initialize: function() {
		this.clientDispatcher = null;
	},
	getClientDispatcher: function() {
		return this.clientDispatcher;
	},
	setClientDispatcher: function(clientDispatcher) {
		this.clientDispatcher = clientDispatcher;
	}
});

var Protocol = new JS.Class({
	initialize: function(schemeName,name,technicalName,description,
						defaultPort,confidential,version) {
		this.schemeName = schemeName;
		this.name = name;
		this.technicalName = technicalName;
        this.description = description;
        this.defaultPort = defaultPort;
        this.confidential = confidential;
        this.version = version;
	}
});

Protocol.extend({
	HTTP: new Protocol("http", "HTTP",
        "HyperText Transport Protocol", 80, false, "1.1"),
    HTTPS: new Protocol("https", "HTTPS", "HTTP",
        "HyperText Transport Protocol (Secure)", 443, true, "1.1")
});

var ClientInfo = new JS.Class({
	initialize: function() {
		/*
        this.address = null;
        this.agent = null;
        this.port = -1;
        this.acceptedCharacterSets = null;
        this.acceptedEncodings = null;
        this.acceptedLanguages = null;
        this.acceptedMediaTypes = null;
        this.forwardedAddresses = null;
        this.from = null;
	 */
		this.acceptedMediaTypes = [];
		if (arguments.length==1 && arguments[0] instanceof MediaType) {
			this.acceptedMediaTypes.push(arguments[0]);
		}
	},
	getAcceptedMediaTypes: function() {
		return this.acceptedMediaTypes;
	}
});

var Message = new JS.Class({
	initialize: function() {
    	this.attributes = {};
    	this.cacheDirectives = null;
    	this.date = null;
    	this.entity = null;
    	this.entityText = null;
    	this.recipientsInfo = null;
	},
	getEntity: function() {
		return this.entity;
	},
	setEntity: function(entity) {
		this.entity = entity;
	},
	getAttributes: function() {
		return this.attributes;
	}, 
	setAttributes: function(attributes) {
		this.attributes = attributes;
	} 
});

var Reference = new JS.Class({
	initialize: function(url) {
		this.url = url;
		var tmp = this.url;
		var index = tmp.indexOf("://");
		if (index!=-1) {
			this.protocol = tmp.substring(index);
			tmp = tmp.substring(index+3);
		}
		index = tmp.indexOf(":");
		if (index!=-1) {
			this.host = tmp.substring(0, index);
			tmp = tmp.substring(index+1);
		}
		index = tmp.indexOf("/");
		if (index!=-1) {
			this.port = parseInt(tmp.substring(0, index));
			tmp = tmp.substring(index);
		} else if (this.protocol=="http") {
			this.port = 80;
		} else if (this.protocol=="https") {
			this.port = 443;
		}
		this.path = tmp;
	},
	getUrl: function() {
		return this.url;
	},
	getScheme: function() {
		return this.scheme;
	},
	getPort: function() {
		return this.port;
	},
	getHost: function() {
		return this.host;
	},
	getPath: function() {
		return this.path;
	}
});

var Request = new JS.Class(Message, {
	initialize: function(method, url) {
		this.callSuper();
		this.method = method;
		this.clientInfo = new ClientInfo();
		if (typeof url == "string") {
			this.reference = new Reference(url);
		} else if (url instanceof Reference) {
			this.reference = reference;
		}

/*		private volatile ChallengeResponse challengeResponse;
    private volatile ClientInfo clientInfo;
    private volatile Conditions conditions;
    private volatile Series<Cookie> cookies;
    private volatile Reference hostRef;
    private volatile boolean loggable;
    private volatile int maxForwards;
    private volatile Method method;
    private volatile Reference originalRef;
    private volatile Protocol protocol;
    private volatile ChallengeResponse proxyChallengeResponse;
    private volatile List<Range> ranges;
    private volatile Reference referrerRef;
    private volatile Reference resourceRef;
    private volatile Reference rootRef;*/
	},
	getMethod: function() {
		return this.method;
	},
	setMethod: function(method) {
		this.method = method;
	},
	getClientInfo: function() {
		return this.clientInfo;
	},
	setClientInfo: function(clientInfo) {
		this.clientInfo = clientInfo;
	},
	getReference: function() {
		return this.reference;
	}
});

var Response = new JS.Class(Message, {
	initialize: function(request) {
		this.callSuper();
        this.age = 0;
        this.allowedMethods = null;
        this.autoCommitting = true;
        this.challengeRequests = null;
        this.cookieSettings = null;
        this.committed = false;
        this.dimensions = null;
        this.locationRef = null;
        this.proxyChallengeRequests = null;
        this.request = request;
        this.retryAfter = null;
        this.serverInfo = null;
        this.status = Status.SUCCESS_OK;
	},
    /*this.age = 0;
    this.allowedMethods = null;
    this.autoCommitting = true;
    this.challengeRequests = null;
    this.cookieSettings = null;
    this.committed = false;
    this.dimensions = null;
    this.locationRef = null;
    this.proxyChallengeRequests = null;*/
	getRequest: function() {
		return this.request;
	},
	setRequest: function(request) {
		this.request = request;
	},
	getRetryAfter: function() {
	    return this.retryAfter;
	},
	setRetryAfter: function(retryAfter) {
	    this.retryAfter = retryAfter;
	},
	getServerInfo: function() {
	    return this.serverInfo;
	},
	setServerInfo: function(serverInfo) {
	    this.serverInfo = serverInfo;
	},
	getStatus: function() {
		return this.status;
	},
	setStatus: function(status) {
		this.status = status;
	}
});

var Method = new JS.Class({
	initialize: function(name, description, uri, safe, idempotent) {
		this.name = name;
		this.description = description;
		this.uri = uri;
		this.safe = safe;
		this.idempotent = idempotent;
	},
	getName: function() {
		return this.name;
	}
});

Method.extend({
    BASE_HTTP: "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html",
    CONNECT: new Method("CONNECT",
        "Used with a proxy that can dynamically switch to being a tunnel",
        Method.BASE_HTTP + "#sec9.9", false, false),
	DELETE: new Method("DELETE",
	    "Requests that the origin server deletes the resource identified by the request URI",
	    Method.BASE_HTTP + "#sec9.7", false, true),
	GET: new Method("GET",
        "Retrieves whatever information (in the form of an entity) that is identified by the request URI",
        Method.BASE_HTTP + "#sec9.3", true, true),
	HEAD: new Method("HEAD",
        "Identical to GET except that the server must not return a message body in the response",
        Method.BASE_HTTP + "#sec9.4", true, true),
	OPTIONS: new Method("OPTIONS",
        "Requests for information about the communication options available on the request/response chain identified by the URI",
        Method.BASE_HTTP + "#sec9.2", true, true),
	POST: new Method("POST",
        "Requests that the origin server accepts the entity enclosed in the request as a new subordinate of the resource identified by the request URI",
        Method.BASE_HTTP + "#sec9.5", false, false),
	PUT: new Method("PUT",
        "Requests that the enclosed entity be stored under the supplied request URI",
        Method.BASE_HTTP + "#sec9.6", false, true),
	TRACE: new Method("TRACE",
        "Used to invoke a remote, application-layer loop-back of the request message",
        Method.BASE_HTTP + "#sec9.8", true, true)
});

var HeaderConstants = new JS.Class({});

HeaderConstants.extend({
	EXPECT_CONTINUE: "100-continue",
    // --- Cache directives ---
	CACHE_NO_CACHE: "no-cache",
	CACHE_NO_STORE: "no-store",
	CACHE_MAX_AGE: "max-age",
	CACHE_MAX_STALE: "max-stale",
	CACHE_MIN_FRESH: "min-fresh",
	CACHE_NO_TRANSFORM: "no-transform",
	CACHE_ONLY_IF_CACHED: "only-if-cached",
	CACHE_PUBLIC: "public",
	CACHE_PRIVATE: "private",
	CACHE_MUST_REVALIDATE: "must-revalidate",
	CACHE_PROXY_MUST_REVALIDATE: "proxy-revalidate",
	CACHE_SHARED_MAX_AGE: "s-maxage",
	// --- Header names ---
	HEADER_ACCEPT: "Accept",
	HEADER_ACCEPT_CHARSET: "Accept-Charset",
	HEADER_ACCEPT_ENCODING: "Accept-Encoding",
	HEADER_ACCEPT_LANGUAGE: "Accept-Language",
	HEADER_ACCEPT_RANGES: "Accept-Ranges",
	HEADER_AGE: "Age",
	HEADER_ALLOW: "Allow",
	HEADER_AUTHENTICATION_INFO: "Authentication-Info",
	HEADER_AUTHORIZATION: "Authorization",
	HEADER_CACHE_CONTROL: "Cache-Control",
	HEADER_CONNECTION: "Connection",
	HEADER_CONTENT_DISPOSITION: "Content-Disposition",
	HEADER_CONTENT_ENCODING: "Content-Encoding",
	HEADER_CONTENT_LANGUAGE: "Content-Language",
	HEADER_CONTENT_LENGTH: "Content-Length",
	HEADER_CONTENT_LOCATION: "Content-Location",
	HEADER_CONTENT_MD5: "Content-MD5",
	HEADER_CONTENT_RANGE: "Content-Range",
	HEADER_CONTENT_TYPE: "Content-Type",
	HEADER_COOKIE: "Cookie",
	HEADER_DATE: "Date",
	HEADER_ETAG: "ETag",
	HEADER_EXPECT: "Expect",
	HEADER_EXPIRES: "Expires",
	HEADER_FROM: "From",
	HEADER_HOST: "Host",
	HEADER_IF_MATCH: "If-Match",
	HEADER_IF_MODIFIED_SINCE: "If-Modified-Since",
	HEADER_IF_NONE_MATCH: "If-None-Match",
	HEADER_IF_RANGE: "If-Range",
	HEADER_IF_UNMODIFIED_SINCE: "If-Unmodified-Since",
	HEADER_LAST_MODIFIED: "Last-Modified",
	HEADER_LOCATION: "Location",
	HEADER_MAX_FORWARDS: "Max-Forwards",
	HEADER_PRAGMA: "Pragma",
	HEADER_PROXY_AUTHENTICATE: "Proxy-Authenticate",
	HEADER_PROXY_AUTHORIZATION: "Proxy-Authorization",
	HEADER_RANGE: "Range",
	HEADER_REFERRER: "Referer",
	HEADER_RETRY_AFTER: "Retry-After",
	HEADER_SERVER: "Server",
	HEADER_SET_COOKIE: "Set-Cookie",
	HEADER_SET_COOKIE2: "Set-Cookie2",
	HEADER_SLUG: "Slug",
	HEADER_TRAILER: "Trailer",
	HEADER_TRANSFER_ENCODING: "Transfer-Encoding",
	HEADER_TRANSFER_EXTENSION: "TE",
	HEADER_UPGRADE: "Upgrade",
	HEADER_USER_AGENT: "User-Agent",
	HEADER_VARY: "Vary",
	HEADER_VIA: "Via",
	HEADER_WARNING: "Warning",
	HEADER_WWW_AUTHENTICATE: "WWW-Authenticate",
	HEADER_X_FORWARDED_FOR: "X-Forwarded-For",
	HEADER_X_HTTP_METHOD_OVERRIDE: "X-HTTP-Method-Override",
    // --- Attribute names ---
	ATTRIBUTE_HEADERS: "org.restlet.http.headers",
	ATTRIBUTE_VERSION: "org.restlet.http.version",
	ATTRIBUTE_HTTPS_CLIENT_CERTIFICATES: "org.restlet.https.clientCertificates",
	ATTRIBUTE_HTTPS_CIPHER_SUITE: "org.restlet.https.cipherSuite",
	ATTRIBUTE_HTTPS_KEY_SIZE: "org.restlet.https.keySize",
	ATTRIBUTE_HTTPS_SSL_SESSION_ID: "org.restlet.https.sslSessionId"
});

var CharacterSet = new JS.Class({
	initialize: function(name) {
		this.name = name;
	},
	getName: function() {
		return this.name;
	}
});

var ContentType = new JS.Class({
	initialize: function(value) {
		var index = -1;
		if ((index = value.indexOf(";"))!=-1) {
			this.mediaType = new MediaType(value.substring(0,index));
			this.characterSet = new CharacterSet(value.substring(index+1));
		} else {
			this.mediaType = new MediaType(value);
		}
	},
	getMediaType: function() {
		return this.mediaType;
	},
	getCharacterSet: function() {
		return this.characterSet;
	} 
});

var Parameter = new JS.Class({
	initialize: function(name, value) {
		this.name = name;
		this.value = value;
	},
	getName: function() {
		return this.name;
	},
	getValue: function() {
		return this.value;
	}
});

var HeaderReaderUtils = new JS.Class({});

HeaderReaderUtils.extend({
	
});

var HeaderWriterUtils = new JS.Class({});

HeaderWriterUtils.extend({
	
});

var HeaderUtils = new JS.Class({});

HeaderUtils.extend({
	addEntityHeaders: function(entity, headers) {
        if (entity == null || !entity.isAvailable()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_LENGTH, "0", headers);
        } else if (entity.getAvailableSize() != Representation.UNKNOWN_SIZE) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_LENGTH,
                    Long.toString(entity.getAvailableSize()), headers);
        }

        if (entity != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_ENCODING,
                    EncodingWriter.write(entity.getEncodings()), headers);
        	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_LANGUAGE,
                    LanguageWriter.write(entity.getLanguages()), headers);

            if (entity.getLocationRef() != null) {
            	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_LOCATION, entity
                        .getLocationRef().getTargetRef().toString(), headers);
            }

            if (entity.getRange() != null) {
            	HeaderUtils.HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_RANGE,
                        RangeWriter.write(entity.getRange(), entity.getSize()),
                        headers);
            }

            if (entity.getMediaType() != null) {
                var contentType = entity.getMediaType().toString();

                // Specify the character set parameter if required
                if ((entity.getMediaType().getParameters()
                        .getFirstValue("charset") == null)
                        && (entity.getCharacterSet() != null)) {
                    contentType = contentType + "; charset="
                            + entity.getCharacterSet().getName();
                }

                HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_TYPE, contentType,
                        headers);
            }

            if (entity.getExpirationDate() != null) {
            	HeaderUtils.addHeader(HeaderConstants.HEADER_EXPIRES,
                        DateWriter.write(entity.getExpirationDate()), headers);
            }

            if (entity.getModificationDate() != null) {
            	HeaderUtils.addHeader(HeaderConstants.HEADER_LAST_MODIFIED,
                        DateWriter.write(entity.getModificationDate()), headers);
            }

            if (entity.getTag() != null) {
            	HeaderUtils.addHeader(HeaderConstants.HEADER_ETAG,
                        TagWriter.write(entity.getTag()), headers);
            }

            if (entity.getDisposition() != null
                    && !Disposition.TYPE_NONE.equals(entity.getDisposition()
                            .getType())) {
            	HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_DISPOSITION,
                        DispositionWriter.write(entity.getDisposition()),
                        headers);
            }
        }
	},
	addExtensionHeaders: function(existingHeaders, additionalHeaders) {
        if (additionalHeaders != null) {
            for (var cpt=0;cpt<additionalHeaders.length;cpt++) {
            	var param = additionalHeaders[cpt];
                if (param.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_ACCEPT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_CHARSET)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_RANGES)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ALLOW)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AUTHENTICATION_INFO)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CACHE_CONTROL)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONNECTION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_DISPOSITION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LENGTH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_MD5)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_TYPE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_DATE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ETAG)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_EXPECT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_EXPIRES)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_FROM)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_HOST)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_MODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_NONE_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_UNMODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_LAST_MODIFIED)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_MAX_FORWARDS)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_PROXY_AUTHENTICATE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_PROXY_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_REFERRER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_RETRY_AFTER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SERVER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SET_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SET_COOKIE2)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_USER_AGENT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_VARY)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_VIA)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_WARNING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_WWW_AUTHENTICATE)) {
                    // Standard headers that can't be overridden
                    /*Context.getCurrentLogger()
                            .warning(
                                    "Addition of the standard header \""
                                            + param.getName()
                                            + "\" is not allowed. Please use the equivalent property in the Restlet API.");*/
                } else if (param.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_PRAGMA)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRAILER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRANSFER_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRANSFER_EXTENSION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_UPGRADE)) {
                    // Standard headers that shouldn't be overridden
                    /*Context.getCurrentLogger()
                            .info("Addition of the standard header \""
                                    + param.getName()
                                    + "\" is discouraged as a future version of the Restlet API will directly support it.");*/
                    existingHeaders.add(param);
                } else {
                    existingHeaders.add(param);
                }
            }
        }
	},
	addGeneralHeaders: function(message, headers) {
		/*HeaderUtils.addHeader(HeaderConstants.HEADER_CACHE_CONTROL,
                CacheDirectiveWriter.write(message.getCacheDirectives()),
                headers);
        if (message.getDate() == null) {
            message.setDate(new Date());
        }
        HeaderUtils.addHeader(HeaderConstants.HEADER_DATE,
                DateWriter.write(message.getDate()), headers);
        HeaderUtils.addHeader(HeaderConstants.HEADER_VIA,
                RecipientInfoWriter.write(message.getRecipientsInfo()), headers);
        HeaderUtils.addHeader(HeaderConstants.HEADER_WARNING,
                WarningWriter.write(message.getWarnings()), headers);*/
	},
	addHeader: function(headerName, headerValue, headers) {
        if ((headerName != null) && (headerValue != null)
                && (headerValue.length() > 0)) {
            try {
                headers.push(new Parameter(headerName, headerValue));
            } catch (err) {
            	console.log(err);
                /*Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to format the " + headerName + " header", t);*/
            }
        }
	},
	addNotModifiedEntityHeaders: function(entity, headers) {
        if (entity != null) {
            if (entity.getTag() != null) {
                HeaderUtils.addHeader(HeaderConstants.HEADER_ETAG,
                        TagWriter.write(entity.getTag()), headers);
            }

            if (entity.getLocationRef() != null) {
                HeaderUtils.addHeader(HeaderConstants.HEADER_CONTENT_LOCATION,
                        entity.getLocationRef().getTargetRef().toString(),
                        headers);
            }
        }
	},
	addRequestHeaders: function(request, headers) {
        var clientInfo = request.getClientInfo();

        /*if (!clientInfo.getAcceptedMediaTypes().isEmpty()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT,
                    PreferenceWriter.write(clientInfo.getAcceptedMediaTypes()),
                    headers);
        } else {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT, MediaType.ALL.getName(),
                    headers);
        }

        if (!clientInfo.getAcceptedCharacterSets().isEmpty()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT_CHARSET,
                    PreferenceWriter.write(clientInfo
                            .getAcceptedCharacterSets()), headers);
        }

        if (!clientInfo.getAcceptedEncodings().isEmpty()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT_ENCODING,
                    PreferenceWriter.write(clientInfo.getAcceptedEncodings()),
                    headers);
        }

        if (!clientInfo.getAcceptedLanguages().isEmpty()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT_LANGUAGE,
                    PreferenceWriter.write(clientInfo.getAcceptedLanguages()),
                    headers);
        }

        if (clientInfo.getFrom() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_FROM, request.getClientInfo()
                    .getFrom(), headers);
        }

        // Manually add the host name and port when it is potentially
        // different from the one specified in the target resource reference.
        var hostRef = (request.getResourceRef().getBaseRef() != null) ? request
                .getResourceRef().getBaseRef() : request.getResourceRef();

        if (hostRef.getHostDomain() != null) {
            var host = hostRef.getHostDomain();
            var hostRefPortValue = hostRef.getHostPort();

            if ((hostRefPortValue != -1)
                    && (hostRefPortValue != request.getProtocol()
                            .getDefaultPort())) {
                host = host + ':' + hostRefPortValue;
            }

            HeaderUtils.addHeader(HeaderConstants.HEADER_HOST, host, headers);
        }

        var conditions = request.getConditions();
        HeaderUtils.addHeader(HeaderConstants.HEADER_IF_MATCH,
                TagWriter.write(conditions.getMatch()), headers);
        HeaderUtils.addHeader(HeaderConstants.HEADER_IF_NONE_MATCH,
                TagWriter.write(conditions.getNoneMatch()), headers);

        if (conditions.getModifiedSince() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_IF_MODIFIED_SINCE,
                    DateWriter.write(conditions.getModifiedSince()), headers);
        }

        if (conditions.getRangeTag() != null
                && conditions.getRangeDate() != null) {
            //Context.getCurrentLogger()
            //        .log(Level.WARNING,
            //                "Unable to format the HTTP If-Range header due to the presence of both entity tag and modification date.");
        } else if (conditions.getRangeTag() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_IF_RANGE,
                    TagWriter.write(conditions.getRangeTag()), headers);
        } else if (conditions.getRangeDate() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_IF_RANGE,
                    DateWriter.write(conditions.getRangeDate()), headers);
        }

        if (conditions.getUnmodifiedSince() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_IF_UNMODIFIED_SINCE,
                    DateWriter.write(conditions.getUnmodifiedSince()), headers);
        }

        if (request.getMaxForwards() > -1) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_MAX_FORWARDS,
                    Integer.toString(request.getMaxForwards()), headers);
        }

        if (!request.getRanges().isEmpty()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_RANGE,
                    RangeWriter.write(request.getRanges()), headers);
        }

        if (request.getReferrerRef() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_REFERRER, request.getReferrerRef()
                    .toString(), headers);
        }

        if (request.getClientInfo().getAgent() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_USER_AGENT, request
                    .getClientInfo().getAgent(), headers);
        } else {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_USER_AGENT, Engine.VERSION_HEADER,
                    headers);
        }

        // ----------------------------------
        // 3) Add supported extension headers
        // ----------------------------------

        if (request.getCookies().size() > 0) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_COOKIE,
                    CookieWriter.write(request.getCookies()), headers);
        }

        // -------------------------------------
        // 4) Add user-defined extension headers
        // -------------------------------------
        var additionalHeaders = request
                .getAttributes()[HeaderConstants.ATTRIBUTE_HEADERS];
        HeaderUtils.addExtensionHeaders(headers, additionalHeaders);

        // ---------------------------------------
        // 5) Add authorization headers at the end
        // ---------------------------------------

        // [ifndef gwt]
        // Add the security headers. NOTE: This must stay at the end because
        // the AWS challenge scheme requires access to all HTTP headers
        //TODO:
        /*ChallengeResponse challengeResponse = request.getChallengeResponse();
        if (challengeResponse != null) {
            addHeader(
                    HeaderConstants.HEADER_AUTHORIZATION,
                    org.restlet.engine.security.AuthenticatorUtils
                            .formatResponse(challengeResponse, request, headers),
                    headers);
        }

        ChallengeResponse proxyChallengeResponse = request
                .getProxyChallengeResponse();
        if (proxyChallengeResponse != null) {
            addHeader(HeaderConstants.HEADER_PROXY_AUTHORIZATION,
                    org.restlet.engine.security.AuthenticatorUtils
                            .formatResponse(proxyChallengeResponse, request,
                                    headers), headers);
        }*/
        // [enddef]
	},
	addResponseHeaders: function(response, headers) {
        if (response.getServerInfo().isAcceptingRanges()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ACCEPT_RANGES, "bytes", headers);
        }

        if (response.getAge() > 0) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_AGE,
                    Integer.toString(response.getAge()), headers);
        }

        if (response.getStatus().equals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED)
                || Method.OPTIONS.equals(response.getRequest().getMethod())) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_ALLOW,
                    MethodWriter.write(response.getAllowedMethods()), headers);
        }

        if (response.getLocationRef() != null) {
            // The location header must contain an absolute URI.
        	HeaderUtils.addHeader(HeaderConstants.HEADER_LOCATION, response
                    .getLocationRef().getTargetRef().toString(), headers);
        }

        //TODO:
        /*if (response.getProxyChallengeRequests() != null) {
            for (ChallengeRequest challengeRequest : response
                    .getProxyChallengeRequests()) {
                addHeader(HeaderConstants.HEADER_PROXY_AUTHENTICATE,
                        org.restlet.engine.security.AuthenticatorUtils
                                .formatRequest(challengeRequest, response,
                                        headers), headers);
            }
        }*/

        if (response.getRetryAfter() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_RETRY_AFTER,
                    DateWriter.write(response.getRetryAfter()), headers);
        }

        if ((response.getServerInfo() != null)
                && (response.getServerInfo().getAgent() != null)) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_SERVER, response.getServerInfo()
                    .getAgent(), headers);
        } else {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_SERVER, Engine.VERSION_HEADER,
                    headers);
        }

        // Send the Vary header only to none-MSIE user agents as MSIE seems
        // to support partially and badly this header (cf issue 261).
        if (!((response.getRequest().getClientInfo().getAgent() != null) && response
                .getRequest().getClientInfo().getAgent().contains("MSIE"))) {
            // Add the Vary header if content negotiation was used
        	HeaderUtils.addHeader(HeaderConstants.HEADER_VARY,
                    DimensionWriter.write(response.getDimensions()), headers);
        }

        // Set the security data
        //TODO:
        /*if (response.getChallengeRequests() != null) {
            for (ChallengeRequest challengeRequest : response
                    .getChallengeRequests()) {
                addHeader(HeaderConstants.HEADER_WWW_AUTHENTICATE,
                        org.restlet.engine.security.AuthenticatorUtils
                                .formatRequest(challengeRequest, response,
                                        headers), headers);
            }
        }*/

        // ----------------------------------
        // 3) Add supported extension headers
        // ----------------------------------

        // Add the Authentication-Info header
        if (response.getAuthenticationInfo() != null) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_AUTHENTICATION_INFO,
                    org.restlet.engine.security.AuthenticatorUtils
                            .formatAuthenticationInfo(response
                                    .getAuthenticationInfo()), headers);
        }

        // Cookies settings should be written in a single header, but Web
        // browsers does not seem to support it.
        //TODO:
        /*for (CookieSetting cookieSetting : response.getCookieSettings()) {
        	HeaderUtils.addHeader(HeaderConstants.HEADER_SET_COOKIE,
                    CookieSettingWriter.write(cookieSetting), headers);
        }*/

        // -------------------------------------
        // 4) Add user-defined extension headers
        // -------------------------------------

        var additionalHeaders = response
                .getAttributes()[HeaderConstants.ATTRIBUTE_HEADERS];
        HeaderUtils.addExtensionHeaders(headers, additionalHeaders);
	},
	extractEntityHeaders: function(headers, representation) {
		console.log("### extractEntityHeaders - representation = "+representation);
	    var result = (representation == null) ? new EmptyRepresentation()
	            : representation;
	    var entityHeaderFound = false;
	
	    if (headers != null) {
	        for (var cpt = 0; cpt<headers.length; cpt++) {
	        	var header = headers[cpt];
	        	console.log("- header : "+header.getName()+" - "+header.getValue());
	            if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_TYPE)) {
	            	console.log("contenttype");
	                var contentType = new ContentType(header.getValue());
	                result.setMediaType(contentType.getMediaType());
	
	                if ((result.getCharacterSet() == null)
	                        || (contentType.getCharacterSet() != null)) {
	                    result.setCharacterSet(contentType.getCharacterSet());
	                }
	
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_LENGTH)) {
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_EXPIRES)) {
	                /*result.setExpirationDate(HeaderReader.readDate(
	                        header.getValue(), false));*/
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_ENCODING)) {
	                /*new EncodingReader(header.getValue()).addValues(result
	                        .getEncodings());*/
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_LANGUAGE)) {
	                /*new LanguageReader(header.getValue()).addValues(result
	                        .getLanguages());*/
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_LAST_MODIFIED)) {
	                /*result.setModificationDate(HeaderReader.readDate(
	                        header.getValue(), false));*/
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_ETAG)) {
	                /*result.setTag(Tag.parse(header.getValue()));*/
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_LOCATION)) {
	                result.setLocationRef(header.getValue());
	                entityHeaderFound = true;
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_DISPOSITION)) {
	                /*try {
	                    result.setDisposition(new DispositionReader(header
	                            .getValue()).readValue());
	                    entityHeaderFound = true;
	                } catch (IOException ioe) {
	                    Context.getCurrentLogger().log(
	                            Level.WARNING,
	                            "Error during Content-Disposition header parsing. Header: "
	                                    + header.getValue(), ioe);
	                }*/
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_RANGE)) {
	                // [ifndef gwt]
	                /*org.restlet.engine.header.RangeReader.update(
	                        header.getValue(), result);*/
	                entityHeaderFound = true;
	                // [enddef]
	            } else if (header.getName().equalsIgnoreCase(
	                    HeaderConstants.HEADER_CONTENT_MD5)) {
	                // [ifndef gwt]
	                /*result.setDigest(new org.restlet.data.Digest(
	                        org.restlet.data.Digest.ALGORITHM_MD5,
	                        org.restlet.engine.util.Base64.decode(header
	                                .getValue())));*/
	                entityHeaderFound = true;
	                // [enddef]
	            }
	        }
	    }
	
	    // If no representation was initially expected and no entity header
	    // is found, then do not return any representation
	    if ((representation == null) && !entityHeaderFound) {
	        result = null;
	    }
	
	    return result;
	},
	copyResponseTransportHeaders: function(headers, response) {
	},
	getContentLength: function(headers) {
	}
});

var Status = new JS.Class({
    initialize: function(code, reasonPhrase, description, uri) {
    	this.code = code;
    	if (typeof reasonPhrase=="undefined" || reasonPhrase==null) {
    		this.reasonPhrase = this.getReasonPhrase();
    	} else {
        	this.reasonPhrase = reasonPhrase;
    	}
    	if (typeof description=="undefined" || description==null) {
    		this.description = this.getDescription();
    	} else {
    		this.description = description;
    	}
    	if (typeof uri=="undefined" || uri==null) {
    		this.uri = this.getUri();
    	} else {
    		this.uri = uri;
    	}
    },
    getCode: function() {
        return this.code;
    },
    getDescription: function() {
        var result = this.description;
        // [ifndef browserlight]

        if (result == null) {
            switch (this.code) {
            case 100:
                result = "The client should continue with its request";
                break;
            case 101:
                result = "The server is willing to change the application protocol being used on this connection";
                break;
            case 102:
                result = "Interim response used to inform the client that the server has accepted the complete request, but has not yet completed it";
                break;
            case 110:
                result = "MUST be included whenever the returned response is stale";
                break;
            case 111:
                result = "MUST be included if a cache returns a stale response because an attempt to revalidate the response failed, due to an inability to reach the server";
                break;
            case 112:
                result = "SHOULD be included if the cache is intentionally disconnected from the rest of the network for a period of time";
                break;
            case 113:
                result = "MUST be included if the cache heuristically chose a freshness lifetime greater than 24 hours and the response's age is greater than 24 hours";
                break;
            case 199:
                result = "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user";
                break;

            case 200:
                result = "The request has succeeded";
                break;
            case 201:
                result = "The request has been fulfilled and resulted in a new resource being created";
                break;
            case 202:
                result = "The request has been accepted for processing, but the processing has not been completed";
                break;
            case 203:
                result = "The returned meta-information is not the definitive set as available from the origin server";
                break;
            case 204:
                result = "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated meta-information";
                break;
            case 205:
                result = "The server has fulfilled the request and the user agent should reset the document view which caused the request to be sent";
                break;
            case 206:
                result = "The server has fulfilled the partial get request for the resource";
                break;
            case 207:
                result = "Provides status for multiple independent operations";
                break;
            case 214:
                result = "MUST be added by an intermediate cache or proxy if it applies any transformation changing the content-coding (as specified in the Content-Encoding header) or media-type (as specified in the Content-Type header) of the response, or the entity-body of the response, unless this Warning code already appears in the response";
                break;
            case 299:
                result = "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action";
                break;

            case 300:
                result = "The requested resource corresponds to any one of a set of representations";
                break;
            case 301:
                result = "The requested resource has been assigned a new permanent URI";
                break;
            case 302:
                result = "The requested resource can be found under a different URI";
                break;
            case 303:
                result = "The response to the request can be found under a different URI";
                break;
            case 304:
                result = "The client has performed a conditional GET request and the document has not been modified";
                break;
            case 305:
                result = "The requested resource must be accessed through the proxy given by the location field";
                break;
            case 307:
                result = "The requested resource resides temporarily under a different URI";
                break;

            case 400:
                result = "The request could not be understood by the server due to malformed syntax";
                break;
            case 401:
                result = "The request requires user authentication";
                break;
            case 402:
                result = "This code is reserved for future use";
                break;
            case 403:
                result = "The server understood the request, but is refusing to fulfill it";
                break;
            case 404:
                result = "The server has not found anything matching the request URI";
                break;
            case 405:
                result = "The method specified in the request is not allowed for the resource identified by the request URI";
                break;
            case 406:
                result = "The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request";
                break;
            case 407:
                result = "This code is similar to Unauthorized, but indicates that the client must first authenticate itself with the proxy";
                break;
            case 408:
                result = "The client did not produce a request within the time that the server was prepared to wait";
                break;
            case 409:
                result = "The request could not be completed due to a conflict with the current state of the resource";
                break;
            case 410:
                result = "The requested resource is no longer available at the server and no forwarding address is known";
                break;
            case 411:
                result = "The server refuses to accept the request without a defined content length";
                break;
            case 412:
                result = "The precondition given in one or more of the request header fields evaluated to false when it was tested on the server";
                break;
            case 413:
                result = "The server is refusing to process a request because the request entity is larger than the server is willing or able to process";
                break;
            case 414:
                result = "The server is refusing to service the request because the request URI is longer than the server is willing to interpret";
                break;
            case 415:
                result = "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method";
                break;
            case 416:
                result = "For byte ranges, this means that the first byte position were greater than the current length of the selected resource";
                break;
            case 417:
                result = "The expectation given in the request header could not be met by this server";
                break;
            case 422:
                result = "The server understands the content type of the request entity and the syntax of the request entity is correct but was unable to process the contained instructions";
                break;
            case 423:
                result = "The source or destination resource of a method is locked";
                break;
            case 424:
                result = "The method could not be performed on the resource because the requested action depended on another action and that action failed";
                break;

            case 500:
                result = "The server encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            case 501:
                result = "The server does not support the functionality required to fulfill the request";
                break;
            case 502:
                result = "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request";
                break;
            case 503:
                result = "The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
                break;
            case 504:
                result = "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request";
                break;
            case 505:
                result = "The server does not support, or refuses to support, the protocol version that was used in the request message";
                break;
            case 507:
                result = "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request";
                break;

            case 1000:
                result = "The connector failed to connect to the server";
                break;
            case 1001:
                result = "The connector failed to complete the communication with the server";
                break;
            case 1002:
                result = "The connector encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            }
        }
        // [enddef]

        return result;
    },
    getName: function() {
        return this.getReasonPhrase();
    },
    getReasonPhrase: function() {
        var result = this.reasonPhrase;
        // [ifndef browserlight]

        if (result == null) {
            switch (this.code) {
            case 100:
                result = "Continue";
                break;
            case 101:
                result = "Switching Protocols";
                break;
            case 102:
                result = "Processing";
                break;
            case 110:
                result = "Response is stale";
                break;
            case 111:
                result = "Revalidation failed";
                break;
            case 112:
                result = "Disconnected operation";
                break;
            case 113:
                result = "Heuristic expiration";
                break;
            case 199:
                result = "Miscellaneous warning";
                break;

            case 200:
                result = "OK";
                break;
            case 201:
                result = "Created";
                break;
            case 202:
                result = "Accepted";
                break;
            case 203:
                result = "Non-Authoritative Information";
                break;
            case 204:
                result = "No Content";
                break;
            case 205:
                result = "Reset Content";
                break;
            case 206:
                result = "Partial Content";
                break;
            case 207:
                result = "Multi-Status";
                break;
            case 214:
                result = "Transformation applied";
                break;
            case 299:
                result = "Miscellaneous persistent warning";
                break;

            case 300:
                result = "Multiple Choices";
                break;
            case 301:
                result = "Moved Permanently";
                break;
            case 302:
                result = "Found";
                break;
            case 303:
                result = "See Other";
                break;
            case 304:
                result = "Not Modified";
                break;
            case 305:
                result = "Use Proxy";
                break;
            case 307:
                result = "Temporary Redirect";
                break;

            case 400:
                result = "Bad Request";
                break;
            case 401:
                result = "Unauthorized";
                break;
            case 402:
                result = "Payment Required";
                break;
            case 403:
                result = "Forbidden";
                break;
            case 404:
                result = "Not Found";
                break;
            case 405:
                result = "Method Not Allowed";
                break;
            case 406:
                result = "Not Acceptable";
                break;
            case 407:
                result = "Proxy Authentication Required";
                break;
            case 408:
                result = "Request Timeout";
                break;
            case 409:
                result = "Conflict";
                break;
            case 410:
                result = "Gone";
                break;
            case 411:
                result = "Length Required";
                break;
            case 412:
                result = "Precondition Failed";
                break;
            case 413:
                result = "Request Entity Too Large";
                break;
            case 414:
                result = "Request URI Too Long";
                break;
            case 415:
                result = "Unsupported Media Type";
                break;
            case 416:
                result = "Requested Range Not Satisfiable";
                break;
            case 417:
                result = "Expectation Failed";
                break;
            case 422:
                result = "Unprocessable Entity";
                break;
            case 423:
                result = "Locked";
                break;
            case 424:
                result = "Failed Dependency";
                break;

            case 500:
                result = "Internal Server Error";
                break;
            case 501:
                result = "Not Implemented";
                break;
            case 502:
                result = "Bad Gateway";
                break;
            case 503:
                result = "Service Unavailable";
                break;
            case 504:
                result = "Gateway Timeout";
                break;
            case 505:
                result = "Version Not Supported";
                break;
            case 507:
                result = "Insufficient Storage";
                break;

            case 1000:
                result = "Connection Error";
                break;
            case 1001:
                result = "Communication Error";
                break;
            case 1002:
                result = "Internal Connector Error";
                break;
            }
        }
        // [enddef]

        return result;
    },
    getUri: function() {
        var result = this.uri;
        // [ifndef browserlight]

        if (result == null) {
            switch (this.code) {
            case 100:
                result = Status.BASE_HTTP + "#sec10.1.1";
                break;
            case 101:
                result = Status.BASE_HTTP + "#sec10.1.2";
                break;
            case 102:
                result = Status.BASE_WEBDAV + "#STATUS_102";
                break;
            case 110:
            case 111:
            case 112:
            case 113:
            case 199:
            case 214:
            case 299:
                result = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46";
                break;

            case 200:
                result = Status.BASE_HTTP + "#sec10.2.1";
                break;
            case 201:
                result = Status.BASE_HTTP + "#sec10.2.2";
                break;
            case 202:
                result = Status.BASE_HTTP + "#sec10.2.3";
                break;
            case 203:
                result = Status.BASE_HTTP + "#sec10.2.4";
                break;
            case 204:
                result = Status.BASE_HTTP + "#sec10.2.5";
                break;
            case 205:
                result = Status.BASE_HTTP + "#sec10.2.6";
                break;
            case 206:
                result = Status.BASE_HTTP + "#sec10.2.7";
                break;
            case 207:
                result = Status.BBASE_WEBDAV + "#STATUS_207";
                break;

            case 300:
                result = Status.BASE_HTTP + "#sec10.3.1";
                break;
            case 301:
                result = Status.BASE_HTTP + "#sec10.3.2";
                break;
            case 302:
                result = Status.BASE_HTTP + "#sec10.3.3";
                break;
            case 303:
                result = Status.BASE_HTTP + "#sec10.3.4";
                break;
            case 304:
                result = Status.BASE_HTTP + "#sec10.3.5";
                break;
            case 305:
                result = Status.BASE_HTTP + "#sec10.3.6";
                break;
            case 307:
                result = Status.BASE_HTTP + "#sec10.3.8";
                break;

            case 400:
                result = Status.BASE_HTTP + "#sec10.4.1";
                break;
            case 401:
                result = Status.BASE_HTTP + "#sec10.4.2";
                break;
            case 402:
                result = Status.BASE_HTTP + "#sec10.4.3";
                break;
            case 403:
                result = Status.BASE_HTTP + "#sec10.4.4";
                break;
            case 404:
                result = Status.BASE_HTTP + "#sec10.4.5";
                break;
            case 405:
                result = Status.BASE_HTTP + "#sec10.4.6";
                break;
            case 406:
                result = Status.BASE_HTTP + "#sec10.4.7";
                break;
            case 407:
                result = Status.BASE_HTTP + "#sec10.4.8";
                break;
            case 408:
                result = Status.BASE_HTTP + "#sec10.4.9";
                break;
            case 409:
                result = Status.BASE_HTTP + "#sec10.4.10";
                break;
            case 410:
                result = Status.BASE_HTTP + "#sec10.4.11";
                break;
            case 411:
                result = Status.BASE_HTTP + "#sec10.4.12";
                break;
            case 412:
                result = Status.BASE_HTTP + "#sec10.4.13";
                break;
            case 413:
                result = Status.BASE_HTTP + "#sec10.4.14";
                break;
            case 414:
                result = Status.BASE_HTTP + "#sec10.4.15";
                break;
            case 415:
                result = Status.BASE_HTTP + "#sec10.4.16";
                break;
            case 416:
                result = Status.BASE_HTTP + "#sec10.4.17";
                break;
            case 417:
                result = Status.BASE_HTTP + "#sec10.4.18";
                break;
            case 422:
                result = Status.BBASE_WEBDAV + "#STATUS_422";
                break;
            case 423:
                result = Status.BBASE_WEBDAV + "#STATUS_423";
                break;
            case 424:
                result = Status.BBASE_WEBDAV + "#STATUS_424";
                break;

            case 500:
                result = Status.BASE_HTTP + "#sec10.5.1";
                break;
            case 501:
                result = Status.BASE_HTTP + "#sec10.5.2";
                break;
            case 502:
                result = Status.BASE_HTTP + "#sec10.5.3";
                break;
            case 503:
                result = Status.BASE_HTTP + "#sec10.5.4";
                break;
            case 504:
                result = Status.BASE_HTTP + "#sec10.5.5";
                break;
            case 505:
                result = Status.BASE_HTTP + "#sec10.5.6";
                break;
            case 507:
                result = Status.BASE_WEBDAV + "#STATUS_507";
                break;

            case 1000:
                result = Status.BASE_RESTLET
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_CONNECTION";
                break;
            case 1001:
                result = Status.BASE_RESTLET
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_COMMUNICATION";
                break;
            case 1002:
                result = Status.BBASE_RESTLET
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_INTERNAL";
                break;
            }
        }

        // [enddef]
        return result;
    },
    isClientError: function() {
        return Status.isClientError(this.getCode());
    },
    isConnectorError: function() {
        return Status.isConnectorError(this.getCode());
    },
    isError: function() {
        return Status.isError(this.getCode());
    },
    isGlobalError: function() {
        return Status.isGlobalError(this.getCode());
    },
    isInformational: function() {
        return Status.isInformational(this.getCode());
    },
    isRecoverableError: function() {
        return Status.isConnectorError()
                || equals(Status.CLIENT_ERROR_REQUEST_TIMEOUT)
                || equals(Status.SERVER_ERROR_GATEWAY_TIMEOUT)
                || equals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
    },
    isRedirection: function() {
        return Status.isRedirection(this.getCode());
    },
    isServerError: function() {
        return Status.isServerError(this.getCode());
    },
    isSuccess: function() {
        return Status.isSuccess(this.getCode());
    },
    toString: function() {
        return this.getReasonPhrase() + " (" + this.code + ")"
                + ((this.getDescription() == null) ? "" : " - " + this.getDescription());
    }
});

Status.extend({
	BASE_HTTP: "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html",
	BASE_RESTLET: "http://www.restlet.org/documentation/",
           /* + Engine.MAJOR_NUMBER
            + '.'
            + Engine.MINOR_NUMBER
            + "/"
            + Edition.CURRENT.getShortName().toLowerCase() + "/api/";*/
	BASE_WEBDAV: "http://www.webdav.org/specs/rfc2518.html",
	CLIENT_ERROR_BAD_REQUEST: new Status(400),
	CLIENT_ERROR_CONFLICT: new Status(409),
	CLIENT_ERROR_EXPECTATION_FAILED: new Status(417),
	CLIENT_ERROR_FAILED_DEPENDENCY: new Status(424),
	CLIENT_ERROR_FORBIDDEN: new Status(403),
	CLIENT_ERROR_GONE: new Status(410),
	CLIENT_ERROR_LENGTH_REQUIRED: new Status(411),
	CLIENT_ERROR_LOCKED: new Status(423),
	CLIENT_ERROR_METHOD_NOT_ALLOWED: new Status(405),
	CLIENT_ERROR_NOT_ACCEPTABLE: new Status(406),
	CLIENT_ERROR_NOT_FOUND: new Status(404),
	CLIENT_ERROR_PAYMENT_REQUIRED: new Status(402),
	CLIENT_ERROR_PRECONDITION_FAILED: new Status(412),
	CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED: new Status(407),
	CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE: new Status(413),
	CLIENT_ERROR_REQUEST_TIMEOUT: new Status(408),
	CLIENT_ERROR_REQUEST_URI_TOO_LONG: new Status(414),
	CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE: new Status(416),
	CLIENT_ERROR_UNAUTHORIZED: new Status(401),
	CLIENT_ERROR_UNPROCESSABLE_ENTITY: new Status(422),
	CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE: new Status(415),
	CONNECTOR_ERROR_COMMUNICATION: new Status(1001),
	CONNECTOR_ERROR_CONNECTION: new Status(1000),
	CONNECTOR_ERROR_INTERNAL: new Status(1002),
	INFO_CONTINUE: new Status(100),
	INFO_DISCONNECTED_OPERATION: new Status(112),
	INFO_HEURISTIC_EXPIRATION: new Status(113),
	INFO_MISC_WARNING: new Status(199),
	INFO_PROCESSING: new Status(102),
	INFO_REVALIDATION_FAILED: new Status(111),
	INFO_STALE_RESPONSE: new Status(110),
	INFO_SWITCHING_PROTOCOL: new Status(101),
	REDIRECTION_FOUND: new Status(302),
	REDIRECTION_MULTIPLE_CHOICES: new Status(300),
	REDIRECTION_NOT_MODIFIED: new Status(304),
	REDIRECTION_PERMANENT: new Status(301),
	REDIRECTION_SEE_OTHER: new Status(303),
	REDIRECTION_TEMPORARY: new Status(307),
	REDIRECTION_USE_PROXY: new Status(305),
	SERVER_ERROR_BAD_GATEWAY: new Status(502),
	SERVER_ERROR_GATEWAY_TIMEOUT: new Status(504),
	SERVER_ERROR_INSUFFICIENT_STORAGE: new Status(507),
	SERVER_ERROR_INTERNAL: new Status(500),
	SERVER_ERROR_NOT_IMPLEMENTED: new Status(501),
	SERVER_ERROR_SERVICE_UNAVAILABLE: new Status(503),
	SERVER_ERROR_VERSION_NOT_SUPPORTED: new Status(505),
	SUCCESS_ACCEPTED: new Status(202),
	SUCCESS_CREATED: new Status(201),
	SUCCESS_MISC_PERSISTENT_WARNING: new Status(299),
	SUCCESS_MULTI_STATUS: new Status(207),
	SUCCESS_NO_CONTENT: new Status(204),
	SUCCESS_NON_AUTHORITATIVE: new Status(203),
	SUCCESS_OK: new Status(200),
	SUCCESS_PARTIAL_CONTENT: new Status(206),
	SUCCESS_RESET_CONTENT: new Status(205),
	SUCCESS_TRANSFORMATION_APPLIED: new Status(214),
    checkReasonPhrase: function(reasonPhrase) {
        if (reasonPhrase != null) {
            if (reasonPhrase.contains("\n") || reasonPhrase.contains("\r")) {
                throw new Error(
                        "Reason phrase of the status must not contain CR or LF characters.");
            }
        }

        return reasonPhrase;
    },
    isClientError: function(code) {
        return (code >= 400) && (code <= 499);
    },
    isConnectorError: function(code) {
        return (code >= 1000) && (code <= 1099);
    },
    isError: function(code) {
        return Status.isClientError(code)
        		|| Status.isServerError(code)
                || Status.isConnectorError(code);
    },
    isGlobalError: function(code) {
        return (code >= 600) && (code <= 699);
    },
    isInformational: function(code) {
        return (code >= 100) && (code <= 199);
    },
    isRedirection: function(code) {
        return (code >= 300) && (code <= 399);
    },
    isServerError: function(code) {
        return (code >= 500) && (code <= 599);
    },
    isSuccess: function(code) {
        return (code >= 200) && (code <= 299);
    },
    valueOf: function(code) {
        var result = null;

        switch (code) {
        case 100:
            result = INFO_CONTINUE;
            break;
        case 101:
            result = INFO_SWITCHING_PROTOCOL;
            break;
        case 102:
            result = INFO_PROCESSING;
            break;
        case 110:
            result = INFO_STALE_RESPONSE;
            break;
        case 111:
            result = INFO_REVALIDATION_FAILED;
            break;
        case 112:
            result = INFO_DISCONNECTED_OPERATION;
            break;
        case 113:
            result = INFO_HEURISTIC_EXPIRATION;
            break;
        case 199:
            result = INFO_MISC_WARNING;
            break;

        case 200:
            result = SUCCESS_OK;
            break;
        case 201:
            result = SUCCESS_CREATED;
            break;
        case 202:
            result = SUCCESS_ACCEPTED;
            break;
        case 203:
            result = SUCCESS_NON_AUTHORITATIVE;
            break;
        case 204:
            result = SUCCESS_NO_CONTENT;
            break;
        case 205:
            result = SUCCESS_RESET_CONTENT;
            break;
        case 206:
            result = SUCCESS_PARTIAL_CONTENT;
            break;
        case 207:
            result = SUCCESS_MULTI_STATUS;
            break;
        case 214:
            result = SUCCESS_TRANSFORMATION_APPLIED;
            break;
        case 299:
            result = SUCCESS_MISC_PERSISTENT_WARNING;
            break;

        case 300:
            result = REDIRECTION_MULTIPLE_CHOICES;
            break;
        case 301:
            result = REDIRECTION_PERMANENT;
            break;
        case 302:
            result = REDIRECTION_FOUND;
            break;
        case 303:
            result = REDIRECTION_SEE_OTHER;
            break;
        case 304:
            result = REDIRECTION_NOT_MODIFIED;
            break;
        case 305:
            result = REDIRECTION_USE_PROXY;
            break;
        case 307:
            result = REDIRECTION_TEMPORARY;
            break;

        case 400:
            result = CLIENT_ERROR_BAD_REQUEST;
            break;
        case 401:
            result = CLIENT_ERROR_UNAUTHORIZED;
            break;
        case 402:
            result = CLIENT_ERROR_PAYMENT_REQUIRED;
            break;
        case 403:
            result = CLIENT_ERROR_FORBIDDEN;
            break;
        case 404:
            result = CLIENT_ERROR_NOT_FOUND;
            break;
        case 405:
            result = CLIENT_ERROR_METHOD_NOT_ALLOWED;
            break;
        case 406:
            result = CLIENT_ERROR_NOT_ACCEPTABLE;
            break;
        case 407:
            result = CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED;
            break;
        case 408:
            result = CLIENT_ERROR_REQUEST_TIMEOUT;
            break;
        case 409:
            result = CLIENT_ERROR_CONFLICT;
            break;
        case 410:
            result = CLIENT_ERROR_GONE;
            break;
        case 411:
            result = CLIENT_ERROR_LENGTH_REQUIRED;
            break;
        case 412:
            result = CLIENT_ERROR_PRECONDITION_FAILED;
            break;
        case 413:
            result = CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE;
            break;
        case 414:
            result = CLIENT_ERROR_REQUEST_URI_TOO_LONG;
            break;
        case 415:
            result = CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
            break;
        case 416:
            result = CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE;
            break;
        case 417:
            result = CLIENT_ERROR_EXPECTATION_FAILED;
            break;
        case 422:
            result = CLIENT_ERROR_UNPROCESSABLE_ENTITY;
            break;
        case 423:
            result = CLIENT_ERROR_LOCKED;
            break;
        case 424:
            result = CLIENT_ERROR_FAILED_DEPENDENCY;
            break;

        case 500:
            result = SERVER_ERROR_INTERNAL;
            break;
        case 501:
            result = SERVER_ERROR_NOT_IMPLEMENTED;
            break;
        case 502:
            result = SERVER_ERROR_BAD_GATEWAY;
            break;
        case 503:
            result = SERVER_ERROR_SERVICE_UNAVAILABLE;
            break;
        case 504:
            result = SERVER_ERROR_GATEWAY_TIMEOUT;
            break;
        case 505:
            result = SERVER_ERROR_VERSION_NOT_SUPPORTED;
            break;
        case 507:
            result = SERVER_ERROR_INSUFFICIENT_STORAGE;
            break;

        case 1000:
            result = CONNECTOR_ERROR_CONNECTION;
            break;
        case 1001:
            result = CONNECTOR_ERROR_COMMUNICATION;
            break;
        case 1002:
            result = CONNECTOR_ERROR_INTERNAL;
            break;

        default:
            result = new Status(code);
        }

        return result;
    }
});

var Restlet = new JS.Class(Restlet, {
	setContext: function(context) {
		this.context = context;
	},
	setProtocols: function(protocols) {
		this.protocols = protocols;
	},
    isStarted: function() {
        return this.started;
    },
    isStopped: function() {
        return !this.started;
    },
    start: function() {
        this.started = true;
    },
    stop: function() {
        this.started = false;
    },
    handle: function(request, response) {
        if (isStopped()) {
            try {
                start();
            } catch (err) {
                // Occurred while starting the Restlet
                //getContext().getLogger().log(Level.WARNING, UNABLE_TO_START, e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

            if (!isStarted()) {
                // No exception raised but the Restlet somehow couldn't be
                // started
                //getContext().getLogger().log(Level.WARNING, UNABLE_TO_START);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
    }
});

var Connector = new JS.Class(Restlet, {
	initialize: function(context, protocols) {
		this.context = context;
		if (typeof protocols != "undefined" && protocols!=null) {
			this.protocols = protocols;
		} else {
			this.protocols = [];
		}
	},
	getProtocols: function() {
		return this.protocols;
	}
});

var Engine = new JS.Class({
	createHelper: function(restlet) {
		// [ifndef nodejs]
		return new XhrHttpClientHelper();
		// [enddef]
		// [ifdef nodejs] uncomment
		//return new NodeJsHttpClientHelper();
		// [enddef]
	}
});

Engine.extend({
	getInstance: function() {
		if (Engine.instance==null) {
			Engine.instance = new Engine();
		}
		return Engine.instance;
	}
});

var Call = new JS.Class({
    /*private volatile String clientAddress;
    private volatile int clientPort;
    private volatile boolean confidential;
    private volatile String hostDomain;
    private volatile int hostPort;
    private volatile String method;
    private volatile Protocol protocol;
    $$ private volatile String reasonPhrase;
    $$ private final Series<Parameter> requestHeaders;
    private volatile String requestUri;
    $$ private final Series<Parameter> responseHeaders;
    private volatile String serverAddress;
    private volatile int serverPort;
    $$ private volatile int statusCode;
    private volatile String version;*/
	getReasonPhrase: function() {
		return this.reasonPhrase;
	},
	setReasonPhrase: function(reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	},
	getRequestHeaders: function() {
		return this.requestHeaders;
	},
	setRequestHeaders: function(requestHeaders) {
		this.requestHeaders = requestHeaders;
	},
	getResponseHeaders: function() {
		console.log("call - getResponseHeaders - "+this.responseHeaders.length);
		return this.responseHeaders;
	},
	setResponseHeaders: function(responseHeaders) {
		console.log("call - setResponseHeaders - "+this.responseHeaders+" | "+responseHeaders.length);
		this.responseHeaders = responseHeaders;
	},
	getStatusCode: function() {
		return this.statusCode;
	},
	setStatusCode: function(statusCode) {
		this.statusCode = statusCode;
	}
});

var ClientCall = new JS.Class(Call, {
	getResponseEntity: function(response) {
		console.log("> getResponseEntity");
        var result = response.getEntity();
        //TODO:
        // boolean available = false;
        var size = Representation.UNKNOWN_SIZE;

        // Compute the content length
        var responseHeaders = this.getResponseHeaders();
        console.log("responseHeaders = "+responseHeaders);
        /*var transferEncoding = responseHeaders.getFirstValue(
                HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        if ((transferEncoding != null)
                && !"identity".equalsIgnoreCase(transferEncoding)) {
            size = Representation.UNKNOWN_SIZE;
        } else {
            size = getContentLength();
        }*/

        /*if (!getMethod().equals(Method.HEAD.getName())
                && !response.getStatus().isInformational()
                && !response.getStatus()
                        .equals(Status.REDIRECTION_NOT_MODIFIED)
                && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                && !response.getStatus().equals(Status.SUCCESS_RESET_CONTENT)) {
            // Make sure that an InputRepresentation will not be instantiated
            // while the stream is closed.
            InputStream stream = getUnClosedResponseEntityStream(getResponseEntityStream(size));
            // [ifndef gwt] line
            java.nio.channels.ReadableByteChannel channel = getResponseEntityChannel(size);
            // [ifdef gwt] line uncomment
            // InputStream channel = null;

            if (stream != null) {
                result = getRepresentation(stream);
            } else if (channel != null) {
                result = getRepresentation(channel);
            }
        }*/

        if (result != null) {
            result.setSize(size);

            // Informs that the size has not been specified in the header.
            if (size == Representation.UNKNOWN_SIZE) {
                /*getLogger()
                        .fine("The length of the message body is unknown. The entity must be handled carefully and consumed entirely in order to surely release the connection.");*/
            }
        }
        console.log("responseHeaders = "+responseHeaders.length);
        result = HeaderUtils.extractEntityHeaders(responseHeaders, result);

        return result;
    }
});

// [ifndef nodejs]
var XhrHttpClientCall = new JS.Class(ClientCall, {
	initialize: function() {
		this.xhr = this.createXhrObject();
	},
	createXhrObject: function() {
	    if (window.XMLHttpRequest)
	        return new XMLHttpRequest();
	 
	    if (window.ActiveXObject) {
	        var names = [
	            "Msxml2.XMLHTTP.6.0",
	            "Msxml2.XMLHTTP.3.0",
	            "Msxml2.XMLHTTP",
	            "Microsoft.XMLHTTP"
	        ];
	        for(var i in names) {
	            try{ return new ActiveXObject(names[i]); }
	            catch(e){}
	        }
	    }
	    //window.alert("Votre navigateur ne prend pas en charge l'objet XMLHTTPRequest.");
	    return null; // not supported
	},
	sendRequest: function(request, callback) {
		var currentThis = this;
		var response = new Response(request);
		var url = request.getReference().getUrl();
		var method = request.getMethod().getName();
		var clientInfo = request.getClientInfo();
		console.log("clientInfo = "+clientInfo);
		var acceptedMediaTypes = clientInfo.getAcceptedMediaTypes();
		var acceptHeader = "";
		for (var i=0;i<acceptedMediaTypes.length;i++) {
			if (i>0) {
				acceptHeader += ",";
			}
			acceptHeader += acceptedMediaTypes[i].getType();
		}
		var headers = {};
		if (acceptHeader!="") {
			headers["accept"] = acceptHeader;
		}
		var data = "";
		if (request.getEntity()!=null) {
			data = request.getEntity().getText();
		}
		this.lowLevelSendRequest(url, method, headers, data, function(xhr) {
			currentThis.extractResponseHeaders(xhr);

			var representation = new Representation();
			/*representation = HeaderUtils.extractEntityHeaders(
								currentThis.getResponseHeaders(xhr), representation);*/
			representation.write(xhr);
			var status = new Status(xhr.status);
			response.setStatus(status);
			response.setEntity(representation);
			callback(response);
		});
	},
	extractResponseHeaders: function(xhr) {
		console.log("> extractResponseHeaders");
		var headersString = xhr.getAllResponseHeaders();
		var headers = [];
		var headerEntries = headersString.split("\n");
		for (var cpt=0;cpt<headerEntries.length;cpt++) {
			var headerEntry = headerEntries[cpt];
			var index = headerEntry.indexOf(":");
			if (headerEntry!="" && index!=-1) {
				var header = new Parameter(
						headerEntry.substring(0, index),
						headerEntry.substring(index+1));
				headers.push(header);
			}
		}
		//this.responseHeaders = headers;
		console.log("> this.responseHeaders = "+headers.length);
		console.log("b1");
		this.setResponseHeaders(headers);
		console.log("a1");
	},
	lowLevelSendRequest: function(url,httpMethod,headers,data,onResponseCallback) {
		var currentThis = this;
		currentThis.xhr.open(httpMethod, url);
		currentThis.xhr.onreadystatechange = function() {
			if (this.readyState==4) {
				onResponseCallback(currentThis.xhr);
			}
		};

		if (headers!=null) {
			for (var headerName in headers) {
				currentThis.xhr.setRequestHeader(headerName,headers[headerName]);
			}
		}
		  
		if (data!=null && data!="") {
			currentThis.xhr.send("" + data);
		} else {
			currentThis.xhr.send();
		}
	}
});
// [enddef]

// [ifdef nodejs] uncomment
//var NodeJsHttpClientCall = new JS.Class({
//	initialize: function() {
//	},
//	sendRequest: function(request, callback) {
//		var currentThis = this;
//		var response = new Response(request);
//		//var url = request.getReference().getUrl();
//		var port = request.getReference().getPort();
//		var host = request.getReference().getHost();
//		var path = request.getReference().getPath();
//		var method = request.getMethod().getName();
//		var clientInfo = request.getClientInfo();
//		console.log("clientInfo = "+clientInfo);
//		var acceptedMediaTypes = clientInfo.getAcceptedMediaTypes();
//		var acceptHeader = "";
//		for (var i=0;i<acceptedMediaTypes.length;i++) {
//			if (i>0) {
//				acceptHeader += ",";
//			}
//			acceptHeader += acceptedMediaTypes[i].getType();
//		}
//		var headers = {};
//		if (acceptHeader!="") {
//			headers["accept"] = acceptHeader;
//		}
//			headers["accept"] = "application/json";
//headers["host"] = "localhost:8080";
//console.log("http://"+host+":"+port+path);
//		var data = "";
//		if (request.getEntity()!=null) {
//			data = request.getEntity().getText();
//		}
//
//		var client = http.createClient(port, host);
//		var clientRequest = client.request(method,
//			path, headers);
//		clientRequest.end();
//
//		clientRequest.on('response', function (clientResponse) {
//			console.log("on response");
//console.log('STATUS: ' + clientResponse.statusCode);
//  console.log('HEADERS: ' + JSON.stringify(clientResponse.headers));
//			var representation = new Representation();
//			//representation.write(xhr);
//			response.setEntity(representation);
//			callback(response);
//			clientResponse.on('data', function (chunk) {
//				console.log("on data");
//				console.log("chunk = "+chunk);
//				representation.write({responseText: chunk, responseXML: null});
//			});
//		});
//	}
//});
// [enddef]

var ClientAdapter = new JS.Class({
	initialize: function(context) {
		
	},
    readResponseHeaders: function(httpCall, response) {
    	console.log("readResponseHeaders");
        try {
            var responseHeaders = httpCall.getResponseHeaders();
        	console.log("responseHeaders = "+responseHeaders.length);

            // Put the response headers in the call's attributes map
        	console.log("response = "+response);
        	console.log("response = "+response.getAttributes);
        	console.log("response = "+response.getAttributes());
            response.getAttributes()[HeaderConstants.ATTRIBUTE_HEADERS] = responseHeaders;

            HeaderUtils.copyResponseTransportHeaders(responseHeaders, response);
        } catch (err) {
        	console.log(err);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, err);
        }
    },
    toSpecific: function(client, request) {
        // Create the low-level HTTP client call
        var result = client.create(request);

        // Add the headers
        if (result != null) {
            HeaderUtils.addGeneralHeaders(request, result.getRequestHeaders());

            if (request.getEntity() != null) {
                HeaderUtils.addEntityHeaders(request.getEntity(),
                        result.getRequestHeaders());
            }

            // NOTE: This must stay at the end because the AWS challenge
            // scheme requires access to all HTTP headers
            HeaderUtils.addRequestHeaders(request, result.getRequestHeaders());
        }

        return result;
    },
    updateResponse: function(response, status, httpCall) {
    	console.log("updateResponse");
        // Send the request to the client
        response.setStatus(status);

        // Get the server address
        //TODO:
        //response.getServerInfo().setAddress(httpCall.getServerAddress());
        //response.getServerInfo().setPort(httpCall.getServerPort());

        // Read the response headers
        this.readResponseHeaders(httpCall, response);

        // Set the entity
        response.setEntity(httpCall.getResponseEntity(response));

        // Release the representation's content for some obvious cases
        if (response.getEntity() != null) {
            if (response.getEntity().getSize() == 0) {
                response.getEntity().release();
            } else if (response.getRequest().getMethod().equals(Method.HEAD)) {
                response.getEntity().release();
            } else if (response.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
                response.getEntity().release();
            } else if (response.getStatus()
                    .equals(Status.SUCCESS_RESET_CONTENT)) {
                response.getEntity().release();
                response.setEntity(null);
            } else if (response.getStatus().equals(
                    Status.REDIRECTION_NOT_MODIFIED)) {
                response.getEntity().release();
            } else if (response.getStatus().isInformational()) {
                response.getEntity().release();
                response.setEntity(null);
            }
        }
    },
    commit: function(httpCall, request, callback) {
        if (httpCall != null) {
            // Send the request to the client
        	var currentThis = this;
            httpCall.sendRequest(request, function(response) {
                try {
                	currentThis.updateResponse(response,
                            new Status(httpCall.getStatusCode(), null,
                                    httpCall.getReasonPhrase(), null),
                            httpCall);
                    callback(response);
                } catch (err) {
                	console.log(err);
                    // Unexpected exception occurred
                    if ((response.getStatus() == null)
                            || !response.getStatus().isError()) {
                        response.setStatus(
                                Status.CONNECTOR_ERROR_INTERNAL, err);
                        callback(response);
                    }
                }
            });
        }
    }
});

var HttpClientHelper = new JS.Class({
    //public abstract ClientCall create(Request request);
	getAdapter: function() {
        if (this.adapter == null) {
            this.adapter = new ClientAdapter(/*this.getContext()*/);
        }

        return this.adapter;
	},
    handle: function(request, callback) {
        try {
            var clientCall = this.getAdapter().toSpecific(this, request);
            this.getAdapter().commit(clientCall, request, callback);
        } catch (err) {
            /*getLogger().log(Level.INFO,
                    "Error while handling an HTTP client call", e);*/
        	var response = new Response(request);
        	console.log(err);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, err);
            response.setEntity(new Representation());
            callback(response);
        }
    }
});

// [ifndef nodejs]
var XhrHttpClientHelper = new JS.Class(HttpClientHelper, {
	initialize: function(client) {
		this.client = client;
	},
	create: function(request) {
		return new XhrHttpClientCall();
	}
});
// [enddef]

// [ifdef nodejs] uncomment
//var NodeJsHttpClientHelper = new JS.Class(HttpClientHelper, {
//	initialize: function(client) {
//		this.client = client;
//	},
//	create: function(request) {
//		return new NodeJsHttpClientCall();
//	}
//});
// [enddef]

var Client = new JS.Class(Connector, {
	initialize: function(context, protocols, helper) {
		this.callSuper();
		//TODO:
		/*this.setContext(context);
		this.setProtocols(protocols);*/
		
		this.configureHelper(helper);
	},
	configureHelper: function(helper) {
		if (this.helper!=null) {
			this.helper = helper;
			return;
		}
		if (this.protocols!=null && this.protocols.length>0) {
			if (Engine.getInstance()!=null) {
				this.helper = Engine.getInstance().createHelper(this);
            } else {
                this.helper = null;
            }
        } else {
            this.helper = null;
		}
	},
	getHelper: function() {
		return this.helper;
	},
	handle: function(request, callback) {
        //this.callSuper(request, callback);

        if (this.getHelper()!=null) {
            this.getHelper().handle(request, callback);
        } else {
            /*StringBuilder sb = new StringBuilder();
            sb.append("No available client connector supports the required protocol: ");
            sb.append("'").append(request.getProtocol().getName()).append("'.");
            sb.append(" Please add the JAR of a matching connector to your classpath.");
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, sb.toString());*/
        }
    }
});

var MediaType = new JS.Class({
	initialize: function(type) {
		this.type = type;
    },
	getType: function() {
		return this.type;
	}
});

MediaType.extend({
	APPLICATION_JSON: new MediaType("application/json"),
	TEXT_JSON: new MediaType("text/json"),
	APPLICATION_XML: new MediaType("application/xml"),
	TEXT_XML: new MediaType("text/xml")
});

var Variant = new JS.Class({
	setMediaType: function(mediaType) {
		this.mediaType = mediaType;
	},
	getMediaType: function() {
		return this.mediaType;
	},
	getCharacterSet: function() {
		return this.characterSet;
	},
	setCharacterSet: function(characterSet) {
		this.characterSet = characterSet;
	}
    /*private volatile CharacterSet characterSet;
    private volatile List<Encoding> encodings;
    private volatile Reference locationRef;
    private volatile List<Language> languages;
    private volatile MediaType mediaType;*/
});

var RepresentationInfo = new JS.Class(Variant, {
    getModificationDate: function() {
    	return this.modificationDate;
    },
    setModificationDate: function(date) {
    	this.modificationDate = date;
    },
    getTag: function() {
    	return this.tag;
    },
    setTag: function(tag) {
    	this.tag = tag;
    }
});

var Representation = new JS.Class(RepresentationInfo, {
	initialize: function() {
	},
    getAvailable: function() {
    	return this.available;
    },
    setAvailable: function(available) {
    	this.available = available;
    },
    getDisposition: function() {
    	return this.disposition;
    },
    setDisposition: function(disposition) {
    	this.disposition = disposition;
    },
    getExpirationDate: function() {
    	return this.expirationDate;
    },
    setExpirationDate: function(expirationDate) {
    	this.expirationDate = expirationDate;
    },
    getIsTransient: function() {
    	return this.isTransient;
    },
    setIsTransient: function(isTransient) {
    	this.isTransient = isTransient;
    },
    getRange: function() {
    	return this.range;
    },
    setRange: function(range) {
    	this.range = range;
    },
    getSize: function() {
    	return this.size;
    },
    setSize: function(size) {
    	this.size = size;
    },
    getText: function() {
		return this.text;
	},
	getXml: function() {
		return this.xml;
	},
	write: function(content) {
		if (typeof content=="string") {
			this.text = content;
        // [ifndef nodejs]
		} else if (content instanceof Document) {
		// [enddef]
		// [ifdef nodejs] uncomment
		//} else if (content instanceof libxmljs.Document) {
		// [enddef]
			this.xml = content;
		} else {
			this.text = content.responseText;
			this.xml = content.responseXML;
		}
	}
});

var JsonRepresentation = new JS.Class(Representation, { 
	initialize: function(content) {
		if (typeof this.text == "undefined") {
			this.text = null;
		}
		this.obj = null;
		this.representation = null;
		if (typeof content == "string") {
			this.text = content;
		} else if (content instanceof Representation) {
			this.representation = content;
		} else if (typeof content == "object") {
			this.obj = content;
		}
		this.setMediaType(MediaType.APPLICATION_JSON);
	},
	getText: function() {
		if (this.obj!=null) {
			return JSON.stringify(this.obj);
		} else {
			return "";
		}
	},
	getObject: function() {
		if (this.text!=null) {
	        // [ifndef nodejs]
			return jsonParse(this.text);
			// [enddef]
			// [ifdef nodejs] uncomment
			//return JSON.parse(this.text);
			// [enddef]
		} else if (this.representation!=null) {
	        // [ifndef nodejs]
			return jsonParse(this.representation.getText());
			// [enddef]
			// [ifdef nodejs] uncomment
			//return JSON.parse(this.representation.getText());
			// [enddef]
		} else {
			return null;
		}
	}
});

var DomRepresentation = new JS.Class(Representation, { 
	initialize: function(content) {
		if (typeof this.text == "undefined") {
			this.text = null;
		}
		if (typeof this.xml == "undefined") {
			this.xml = null;
		}
		this.representation = null;
		if (typeof content == "string") {
			this.text = content;
		} else if (content instanceof Representation) {
			this.representation = content;
		} else if (typeof content == "object") {
	        // [ifndef nodejs]
			if (content instanceof Document) {
			// [enddef]
			// [ifdef nodejs] uncomment
			//if (content instanceof libxmljs.Document) {
			// [enddef]
				this.xml = content;
			} else {
				this.obj = content;
			}
		}
		this.setMediaType(MediaType.APPLICATION_XML);
	},
	getText: function() {
		if (this.xml!=null) {
	        // [ifndef nodejs]
			var document = this.xml.documentElement; 
			if (document.xml==undefined){ 
				return (new XMLSerializer()).serializeToString(this.xml); 
			} else {
				return document.xml; 
			}
			// [enddef]
			// [ifdef nodejs] uncomment
			//return this.xml.toString();
			// [enddef]
		}
		return "";
	},
	getXml: function() {
		if (this.representation!=null) {
	        // [ifndef nodejs]
			return this.representation.getXml();
			// [enddef]
			// [ifdef nodejs] uncomment
			//return libxmljs.parseXmlString(this.representation.getText());
			// [enddef]
		} else if (this.text!=null) {
	        // [ifndef nodejs]
			if (window.ActiveXObject) {
				var doc = new ActiveXObject("Microsoft.XMLDOM"); 
				document.async="false"; 
				document.loadXML(this.text);
				return document;
			} else { 
				var parser = new DOMParser(); 
				return parser.parseFromString(this.text, "text/xml"); 
			} 
			// [enddef]
			// [ifdef nodejs] uncomment
			//return libxmljs.parseXmlString(this.text);
			// [enddef]
		} else {
			return this.xml;
		}
	}
});

// [ifndef nodejs]
var XmlRepresentation = new JS.Class(DomRepresentation, {
	initialize: function(content, objectName) {
		if (typeof this.text == "undefined") {
			this.text = null;
		}
		this.representation = null;
		if (typeof content == "string") {
			this.text = content;
		} else if (content instanceof Representation) {
			this.representation = content;
		} else if (typeof content == "object") {
			this.obj = content;
		}
		this.objectName = objectName;
		this.setMediaType(MediaType.APPLICATION_XML);
	},
	createDocument: function(namespaceURL, rootTagName) {
	  if (document.implementation && document.implementation.createDocument) { 
	    return document.implementation.createDocument(namespaceURL, rootTagName, null); 
	  } else {
		var doc = new ActiveXObject("MSXML2.DOMDocument"); 
	    if (rootTagName) { 
	      var prefix = ""; 
	      var tagname = rootTagName; 
	      var p = rootTagName.indexOf(':'); 
	      if (p != -1) { 
	        prefix = rootTagName.substring(0, p); 
	        tagname = rootTagName.substring(p+1); 
	      }
	      if (namespaceURL) { 
	        if (!prefix) prefix = "a0"; 
	      } else prefix = ""; 
	      var text = "<" + (prefix?(prefix+":"):"") +  tagname + 
	             (namespaceURL 
	             ?(" xmlns:" + prefix + '="' + namespaceURL +'"') 
	             :"") + 
	             "/>"; 
	      doc.loadXML(text); 
	    }
	    return doc; 
	  } 
	},
	serializeObject: function(document, element, obj) {
		for (var elt in obj) {
			var eltElement = document.createElement(elt);
			if (typeof obj[elt]!="object") {
				var textElement = document.createTextNode(obj[elt]);
				eltElement.appendChild(textElement);
			} else {
				this.serializeObject(document, eltElement, obj[elt]);
			}
			element.appendChild(eltElement);
		}
	},
	getObjectName: function() {
		if (this.objectName!=null) {
			return this.objectName;
		} else {
			return "object";
		}
	},
	getText: function() {
		var document = this.createDocument("", this.getObjectName());
		this.serializeObject(document, document.childNodes[0], this.obj);
		if (document.xml==undefined){ 
			return (new XMLSerializer()).serializeToString(document); 
		} else {
			return document.xml; 
		}
	},
	unserializeObject: function(obj, element) {
		var children = element.childNodes;
		for (var cpt=0; cpt<children.length; cpt++) {
			var child = children[cpt];
			if (child.childNodes.length==1 && child.childNodes[0].nodeName=="#text") {
				obj[child.nodeName] = child.childNodes[0].nodeValue;
			} else {
				obj[child.nodeName] = {};
				this.unserializeObject(obj[child.nodeName], child);
			}
		}
	},
	getObject: function() {
		if (this.obj!=null) {
			return this.obj;
		}

		var xml = null;
		if (window.ActiveXObject) {
			var doc = new ActiveXObject("Microsoft.XMLDOM"); 
			document.async = "false"; 
			document.loadXML(this.text);
			xml = document;
		} else { 
			var parser = new DOMParser(); 
			xml = parser.parseFromString(this.text, "text/xml"); 
		} 

		var obj = {};
		this.unserializeObject(obj, xml.childNodes[0]);
		return obj;
	}
});
// [enddef]

var ClientResource = new JS.Class({
	initialize: function(url) {
		this.request = new Request(null, url);
	},
	getRequest: function() {
		return this.request;
	},
	setRequest: function(request) {
		this.request = request;
	},
	getResponse: function() {
		return this.response;
	},
	setResponse: function(response) {
		this.response = response;
	},
	createClientInfo: function(mediaType) {
		var clientInfo = null;
		if (mediaType!=null) {
			clientInfo = new ClientInfo(mediaType);
		} else {
			clientInfo = new ClientInfo();
		}
		return clientInfo;
	},
	"get": function(callback, mediaType) {
		var clientInfo = this.createClientInfo(mediaType);
		this.handle(Method.GET, null, clientInfo, callback);
	},
	"post": function(representation, callback, mediaType) {
		var clientInfo = this.createClientInfo(mediaType);
		this.handle(Method.POST, representation, clientInfo, callback);
	},
	"put": function(representation, callback, mediaType) {
		var clientInfo = this.createClientInfo(mediaType);
		this.handle(Method.PUT, representation, clientInfo, callback);
	},
	"delete": function(callback, mediaType) {
		var clientInfo = this.createClientInfo(mediaType);
		this.handle(Method.DELETE, null, clientInfo, callback);
	},
	createRequest: function() {
		return this.request;
	},
	createResponse: function(request) {
		return new Response(request);
	},
	getNext: function() {
		var result = this.next;

		if (result == null) {
            result = this.createNext();

            if (result != null) {
                this.setNext(result);
                this.nextCreated = true;
            }
        }
		return result;
	},
	setNext: function(next) {
		this.next = next;
	},
	createNext: function() {
        /*var result = null;

        if ((result == null) && (this.getContext() != null)) {
            // Try using directly the client dispatcher
            result = this.getContext().getClientDispatcher();
        }

        if (result == null) {
            var rProtocol = this.getProtocol();
            var rReference = this.getReference();
            var protocol = (rProtocol != null) ? rProtocol
                    : (rReference != null) ? rReference.getSchemeProtocol()
                            : null;

            if (protocol != null) {
                result = new Client(protocol);
            }
        }

        return result;*/
		return new Client(new Context(),/*protocol*/[Protocol.HTTP]);
	},
	handle: function(method, entity, clientInfo, callback) {
        var request = this.createRequest(this.getRequest());
        request.setMethod(method);
        request.setEntity(entity);
        request.setClientInfo(clientInfo);

        this.handleRequest(request, callback);
	},
	handleRequest: function(request, callback) {
        //var response = this.createResponse(request);
        var next = this.getNext();

        if (next != null) {
            // Effectively handle the call
        	this.handleNext(request, callback/*, null, 0*/, next);
        } else {
        	//console
            /*getLogger()
                    .warning(
                            "Unable to process the call for a client resource. No next Restlet has been provided.");*/
        }
	},
	handleNext: function(request, callback, next) {
		var currentThis = this;
		next.handle(request, function(response) {
			currentThis.setResponse(response);
			callback(response.getEntity());
		});
	}
});

//[ifdef nodejs] uncomment
//exports = {
//	ClientResource: ClientResource,
//	MediaType: MediaType
//};
//[enddef]