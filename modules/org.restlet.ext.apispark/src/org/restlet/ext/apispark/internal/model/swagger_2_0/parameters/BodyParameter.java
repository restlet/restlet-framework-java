package org.restlet.ext.apispark.internal.model.swagger_2_0.parameters;

import org.restlet.ext.apispark.internal.model.swagger_2_0.Model;

// @JsonPropertyOrder({ "name", "in", "description", "required", "type", "items", "collectionFormat"})
public class BodyParameter extends AbstractParameter implements Parameter {;
Model schema;

public BodyParameter() {
	super.setIn("body");
}

public BodyParameter schema(Model schema) {
	this.setSchema(schema);
	return this;
}
public BodyParameter description(String description) {
	this.setDescription(description);
	return this;
}
public BodyParameter name(String name) {
	this.setName(name);
	return this;
}

public void setSchema(Model schema) {
	this.schema = schema;
}
public Model getSchema() {
	return schema;
}
}