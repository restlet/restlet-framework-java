/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

public class MyResource08 extends ServerResource {

    @Post("xml|json:xml|json")
    public String store1(String entity) {
        return entity + "1";
    }

    @Post("form|html:form|html")
    public String store2(String entity) {
        return entity + "2";
    }

}
