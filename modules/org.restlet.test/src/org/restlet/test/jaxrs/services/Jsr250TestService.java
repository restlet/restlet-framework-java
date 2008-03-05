package org.restlet.test.jaxrs.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

@ProduceMime("text/plain")
@Path("jsr250test")
public class Jsr250TestService {
    
    /**
     * This field is set after {@link #init()} was called.
     */
    private boolean initiated = false;

    /**
     * This static field contains the value of {@link #toString()} of the last
     * destroyed instance of this class, see {@link #preDeytroy()}.
     */
    public static String LastDestroyed;

    @PostConstruct
    private void init() {
        initiated = true;
    }

    @GET
    public Boolean get() {
        return initiated;
    }

    @PreDestroy
    private void preDeytroy() {
        LastDestroyed = this.toString();
    }
}
