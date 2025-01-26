/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MyResource04 extends ServerResource {

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
