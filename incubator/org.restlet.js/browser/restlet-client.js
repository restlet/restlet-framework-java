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
		//TODO: to be generic
		return new XhrHttpClientHelper();
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

var XhrHttpClientCall = new JS.Class({
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
			var representation = new Representation();
			representation.write(xhr);
			response.setEntity(representation);
			callback(response);
		});
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

var HttpClientHelper = new JS.Class(HttpClientHelper, {
});

var XhrHttpClientHelper = new JS.Class(HttpClientHelper, {
	initialize: function(client) {
		this.client = client;
	},
	createClientCall: function(request) {
		return new XhrHttpClientCall();
	},
	handle: function (request, callback) {
		if (this.clientCall==null) {
			this.clientCall = this.createClientCall(request);
		}
		this.clientCall.sendRequest(request, callback);
	}
});

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

var Representation = new JS.Class({
	initialize: function() {
	},
	setMediaType: function(mediaType) {
		this.mediaType = mediaType;
	},
	getMediaType: function() {
		return this.mediaType;
	},
	getText: function() {
		return this.text;
	},
	getXml: function() {
		return this.xml;
	},
	write: function(xhr) {
		this.text = xhr.responseText;
		this.xml = xhr.responseXML;
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
			return jsonParse(this.text);
		} else if (this.representation!=null) {
			return jsonParse(this.representation.getText());
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
			this.obj = content;
		}
		this.setMediaType(MediaType.APPLICATION_XML);
	},
	getText: function() {
		
	},
	getXml: function() {
		if (this.representation!=null) {
			return this.representation.getXml();
		} else {
			return this.xml;
		}
	}
});

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

