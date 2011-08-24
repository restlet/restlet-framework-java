var assert = require("assert");
var restlet = require('../restlet-client');
var libxmljs = require("libxmljs");


var document = new libxmljs.Document(function(n) {
  n.node('contact', function(n) {
    n.node('id', "1");
    n.node('lastName', "lastName1");
    n.node('firstName', "firstName1");
  });
});
var representation = new restlet.DomRepresentation(document);
var content = representation.getText();

assert.equal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<contact><id>1</id><lastName>lastName1</lastName><firstName>firstName1</firstName></contact>\n", content);

content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><contact><id>1</id><lastName>lastName1</lastName><firstName>firstName1</firstName></contact>";
representation = new restlet.DomRepresentation(content);
var xml = representation.getXml();
var contactElement = xml.root();
var idElement = contactElement.childNodes()[0];
assert.equal("1", idElement.childNodes()[0]);
var lastNameElement = contactElement.childNodes()[1];
assert.equal("lastName1", lastNameElement.childNodes()[0]);
var firstNameElement = contactElement.childNodes()[2];
assert.equal("firstName1", firstNameElement.childNodes()[0]);

