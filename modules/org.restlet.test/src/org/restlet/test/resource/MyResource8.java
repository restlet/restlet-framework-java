package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource8 extends ServerResource {

    @Post("xml:xml")
    public String storeForm(String entity) {
        return entity + "1";
    }

    @Post("xml:json|html")
    public String storeXml(String entity) {
        return entity + "2";
    }

}
