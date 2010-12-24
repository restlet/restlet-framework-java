package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource6 extends ServerResource {

    @Post("txt:xml")
    public String storeXml(String entity) throws IOException {
        return entity + "1";
    }

    @Post("txt:json")
    public String storeJson(String entity) throws IOException {
        return entity + "2";
    }

}
