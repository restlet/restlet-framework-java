/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import java.io.IOException;

public class MyResource06 extends ServerResource {

    @Post("txt:xml")
    public String storeXml(String entity) throws IOException {
        return entity + "1";
    }

    @Post("txt:json")
    public String storeJson(String entity) throws IOException {
        return entity + "2";
    }

}
