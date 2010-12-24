package org.restlet.example.book.restlet.ch08.sec2.sub1;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource that simply redirects to the port 8111.
 */
public class OldServerResource extends ServerResource {

    @Get
    public String redirect() {
        // Sets the response status to 301 (Moved Permanently)
        redirectPermanent("http://localhost:8111/");

        System.out.println("Redirecting client to new location...");

        // Add explanation message entity 
        return "Resource moved... \n";
    }
}
