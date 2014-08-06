package org.restlet.ext.raml;

import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.raml.internal.RamlConverter;
import org.restlet.ext.raml.internal.model.Definition;
import org.restlet.routing.Router;

public class Bidon extends RamlApplication {

	public static void main(String[] args) throws Exception {
		Raml raml = new RamlDocumentBuilder()
				.build("file:///home/cyp/Bureau/hello_world.raml");
		Definition def = RamlConverter.convert(raml);
		new JacksonRepresentation<Definition>(def).write(System.out);
		System.out.println();
		Component c = new Component();
		c.getServers().add(Protocol.HTTP, 8082);

		c.getDefaultHost().attach("/v1", new Bidon());

		c.start();
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router();
		return router;
	}
}
