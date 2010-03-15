package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource5 extends ServerResource {

    @Post("xml:xml")
    public String storeXml(String entity) {
        return entity;
    }

    @Post("json:json")
    public String storeJson(String entity) {
        return entity;
    }

}
