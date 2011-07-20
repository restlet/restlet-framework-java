var assert = require("assert");
var restlet = require('restlet-client');

var contact = {
	id: 1,
	lastName: "lastName1",
	firstName: "firstName1"
};
var representation = new restlet.JsonRepresentation(contact);
var content = representation.getText();

assert.equal("{\"id\":1,\"lastName\":\"lastName1\",\"firstName\":\"firstName1\"}", content);

content = "{\"id\":1,\"lastName\":\"lastName1\",\"firstName\":\"firstName1\"}";
representation =  new restlet.JsonRepresentation(content);
contact = representation.getObject();
assert.equal(1, contact.id);
assert.equal("lastName1", contact.lastName);
assert.equal("firstName1", contact.firstName);
