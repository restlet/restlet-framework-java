package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource8 extends ServerResource {

    @Post("xml|json:xml")
    public String storeForm(String entity) {
        return entity + "1";
    }

    @Post("xml|json:json|html")
    public String store1(String entity) {
        return entity + "2";
    }

    @Post("form|json:json|html")
    public String store2(String entity) {
        return entity + "3";
    }

}
