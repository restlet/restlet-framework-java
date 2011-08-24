var assert = require("assert");
var restlet = require('../restlet-client');

var clientResource = new restlet.ClientResource("../resource/contact/1");
clientResource.get(function(representation) {
  console.log("representation = "+representation);
  var content = representation.getText();
  console.log("content = "+content);
  var jsonRepresentation = new restlet.JsonRepresentation(representation);
  var contact = jsonRepresentation.getObject();
  assert.equal(1, contact.id);
  assert.equal("lastName1", contact.lastName);
  assert.equal("firstName1", contact.firstName);
}, restlet.MediaType.APPLICATION_JSON);
