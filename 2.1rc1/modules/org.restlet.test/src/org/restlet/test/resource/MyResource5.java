package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource5 extends ServerResource {

    @Post("txt:xml")
    public String storeXml(String entity) {
        return entity;
    }

    @Post("txt:json")
    public String storeJson(String entity) {
        return entity;
    }

}
