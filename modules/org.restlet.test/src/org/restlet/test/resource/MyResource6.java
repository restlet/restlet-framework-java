package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MyResource6 extends ServerResource {

    @Post("xml:xml")
    public String storeXml() throws IOException {
        return getRequestEntity().getText();
    }

    @Post("json:json")
    public String storeJson() throws IOException {
        return getRequestEntity().getText();
    }

}
