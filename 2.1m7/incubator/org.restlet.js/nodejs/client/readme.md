# Restlet

The node.js port of the Restlet framework, a tool allowing developing and consuming RESTful applications.
Only the client side is available for the moment. Server-side implementation is under development. 



## Description

The Restlet framework is a tool that allows developing and consuming RESTful applications. It provides a convenient
abstraction upon the REST principles. The tool is primarly developed with the Java language but concepts it leverages
are language-independant. For more information, see the website of the project: <a href="http://www.restlet.org/">http://www.restlet.org/</a>. 


## Requires

node 0.4.x



## Installation

To install the Restlet client for Node.js, npm can be used.

    npm install restlet-client

If you want to use the provided XML support, you must install the libxmljs module that corresponds to a wrapping upon
the libxml2 library and is used by the DomRepresentation class. This means that you need to the library installed on your
system and also the module through npm.

Under Debian / Ubuntu, this can be done with the following command:

    $ sudo apt-get install libxml2 libxml2-dev scons
    $ npm install libxmljs


## Require module.

To use the Restlet client module, you need to include it using the require function of Node.js as described in following code:

    var restlet = require('./lib/restlet-client');
    
## Root API

The Restlet edition for JavaScript only targets the client side since it's executed within a Web browser. It implements and adapts
the main Restlet artifacts to the JavaScript environment. The four main classes in this context are:

Request: represents the current requests and allows specifying target address, method and content of the requestClient:
corresponds to the artifact that actually executes the requestResponse: represents the response of the current request and
gives access to received contentRepresentation: corresponds to exchanged content

Following code describes how to use these artifacts to execute a REST request.

    var request = new Request(Method.GET, "../resource/contact/1");
    var client = new Client(new Context(), [Protocol.HTTP]);
    client.handle(request, function(response) {
        var representation = response.getEntity();
        var content = representation.getText();
        // content = "{\"id\":\"1\",\"lastName\":\"last name\",\"firstName\":\"first name\"}");
    });

As you can see, the API is very similar to the Java one. The only difference consists in the callback
mechanism used. As a matter of fact, networking in browsers is necessarily asynchronous and JavaScript allows
directly specifying function to methods. Restlet JavaScript API follows style of classic JavaScript API.

Following code provides another example to execute a PUT method from a browser using Restlet:

    var clientResource = new ClientResource("../resource/contact/1");
    var contact = {
        id: "1",
        lastName: "lastName",
        firstName: "firstName"
    }
    var jsonRepresentation = new JsonRepresentation(contact);
    clientResource.put(jsonRepresentation, function(representation) {
        var jsonRepresentation = new JsonRepresentation(representation);
        var obj = jsonRepresentation.getObject();
        // obj corresponds to same object as contact
    }, MediaType.APPLICATION_JSON);

Internally the Restlet edition for JavaScript (node.js) uses a client connector based on the http module of Node.js.
This connector is named NodeJsHttpClientHelper and is based on the NodeJsHttpClientCall class that actually uses this http module.
For the moment, the connector detection is hardcoded and this connector is allways used. This will be improved in later version
of the edition.

## ClientResource

The ClientResource class abstracts and simplifies the use of previously described artifacts. It directly accepts and
provides representations for exchanged content. This class follows same API of the Java-based Restlet with previously
described adaptations for the JavaScript language.

The following example of code describes how to execute a GET method:

    var clientResource = new ClientResource("../resource/contact/1");
    clientResource.get(function(representation) {
        var content = representation.getText();
        (...)
    }, MediaType.APPLICATION_JSON);

The following example of code describes how to execute a PUT method:

    var clientResource = new ClientResource("../resource/contact/1");
    var contact = {
        id: "1",
        lastName: "lastName",
        firstName: "firstName"
    };

    var jsonRepresentation = new JsonRepresentation(contact);
    clientResource.put(jsonRepresentation, function(representation) {
        var jsonRepresentation = new JsonRepresentation(representation);
        var obj = jsonRepresentation.getObject();
        (...)
    }, MediaType.APPLICATION_JSON);

## Content negociation

Content negociation is supported by both Client and ClientResource classes using the last parameter mediaType
like for Java-based Restlet.

Like with Java-based Restlet, all supported media types are listed as static fields of the MediaType class.

Following code describes how to set expected media types for a request:

    var jsonRepresentation = new JsonRepresentation(contact);
    clientResource.put(jsonRepresentation, function(representation) {
        (...)
    }, MediaType.APPLICATION_JSON);

## Representation support

Two representations are provided for this edition in order to manage JSON and XML-based content:

JsonRepresentation: parses and produces JSON content based on JavaScript objects
DomRepresentation: parses and produces XML content based on DOM documents (libxmljs)

Using representations follows the same rules as for the Java editions but representations don't use streams. You simply
have to provide the initial object to the constructor and then get the converted content using methods like getText,
getObject or getXML according to representations.

Following code describes how to use the JSON representation to manage JSON content (from json-representation-tests.html file):

    // Object to JSON text

    var contact = {
        id: "1",
        lastName: "last name",
        firstName: "first name"
    }
    var representation = new JsonRepresentation(contact);
    String jonsString = representation.getText()
    // Produces this string {"id":"1","lastName":"last name","firstName":"first name"}

    // JSON text to objects

    var jsonString = "{\"id\":\"1\",\"lastName\":\"last name\",\"firstName\":\"first name\"}";
    var representation = new JsonRepresentation(jsonString);
    var obj = representation.getObject();
    // Products the same object as contact above

Following code describes how to use the DOM representation to manage XML content with libxmljs support :

    // XML document to XML text

    var document = new libxmljs.Document(function(n) {
        n.node('contact', function(n) {
            n.node('id', "1");
            n.node('lastName', "lastName1");
            n.node('firstName', "firstName1");
        });
    });

    var representation = new DomRepresentation(document);
    var xmlString = representation.getText();
    // Produces the string <contact><id>1</id><lastName>last name</lastName>
    // <firstName>first name</firstName></contact>

    // XML text to XML document

    var xmlString = "<contact><id>1</id><lastName>last name</lastName>"
                    + "<firstName>first name</firstName></contact>";

    var representation = new DomRepresentation(xmlString);
    var xml = representation.getXml();
    var contactElement = xml.childNodes()[0];
    var idElement = contactElement.childNodes()[0];
    // idElement.childNodes[0] returns "1"
    var lastNameElement = contactElement.childNodes()[1];
    // lastNameElement.childNodes[0] returns "last name"
    var firstNameElement = contactElement.childNodes()[2];
    // firstNameElement.childNodes[0] returns "first name"

Representations can be specified on requests and retrieved from responses. ClientResource directly supports them
via its methods to execute requests. You can also notice that the representations accept representation as
parameter of their constructor. This allows parsing in a simple way the received data. Following code describes this aspect:

    var jsonRepresentation = new JsonRepresentation(contact);
    clientResource.put(jsonRepresentation, function(representation) {
        var jsonRepresentation = new JsonRepresentation(representation);
        var obj = jsonRepresentation.getObject();
        (...)
    });

## License 

See this page : <a href="http://www.restlet.org/about/legal">http://www.restlet.org/about/legal</a>