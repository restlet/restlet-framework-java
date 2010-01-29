package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource7 extends ServerResource {

    @Post("json:xml")
    public String storeJson(String entity) {
        return entity + "1";
    }

    @Post("xml:xml")
    public String storeXml(String entity) {
        return entity + "2";
    }

}
