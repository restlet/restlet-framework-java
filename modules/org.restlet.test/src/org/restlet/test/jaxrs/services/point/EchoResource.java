package org.restlet.test.jaxrs.services.point;

import java.awt.Point;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(value=EchoResource.path)
public interface EchoResource {
	public static final String path = "echo";
	
	@GET
	@Produces( "application/xml" )
	@Path( "point" )
	@Consumes(MediaType.APPLICATION_JSON)
	Point echoPoint( @HeaderParam( "point" )  Point point );


	@POST
	String echo( String input );
}
