/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

public class MyResource07 extends ServerResource {

    @Post("json:xml")
    public String storeJson(String entity) {
        return entity + "1";
    }

    @Post("xml:xml")
    public String storeXml(String entity) {
        return entity + "2";
    }

}
