package org.restlet.ext.jaxrs.examples;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * This demonstrates an easy representation.
 * 
 * @author Stephan Koops
 */
@Path("easy")
public class EasyRootResource {

    /**
     * Returns a HTML representation.
     * 
     * @return the person
     */
    @GET
    @ProduceMime("text/html")
    public String getHtml() {
        return "<html><head></head><body>\n"
                + "This is an easy resource (as html text). Very flexible, I think\n"
                + "</body></html>";
    }

    /**
     * Returns a plain text representation.
     * 
     * @return the person
     */
    @GET
    @ProduceMime("text/plain")
    public String getPlain() {
        return "This is an easy resource (as plain text)";
    }
}