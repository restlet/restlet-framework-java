var assert = require("assert");
var restlet = require('../restlet-client');

var request = new Request(Method.GET, "../resource/contact/1");
var client = new Client(new Context(), [Protocol.HTTP]);
client.handle(request, function(response) {
var representation = response.getEntity();
  console.log("representation = "+representation);
  var content = representation.getText();
  console.log("content = "+content);
  var jsonRepresentation = new restlet.JsonRepresentation(representation);
  var contact = jsonRepresentation.getObject();
  assert.equal(1, contact.id);
  assert.equal("lastName1", contact.lastName);
  assert.equal("firstName1", contact.firstName);
});
