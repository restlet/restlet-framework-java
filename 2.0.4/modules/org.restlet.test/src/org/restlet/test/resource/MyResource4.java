package org.restlet.test.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MyResource4 extends ServerResource {

    @Get("xml")
    public String toXml() {
        return "<root/>";
    }

    @Get("json")
    public String toJson() {
        return "[\"root\"]";
    }

    @Get("html")
    public String toHtml() {
        return "<html><body>root</body></html>";
    }

}
