/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.resource;

import java.util.Date;

import org.restlet.resource.ServerResource;

/**
 * Sample server resource for modifier testing.
 * 
 * @author Jerome Louvel
 */
public class MyServerResource13 extends ServerResource implements MyResource13 {

    public LightContact retrieveLight() {
        return new LightContact("test@domain.com", "Scott", "Tiger");
    }

    public Contact retrieve() {
        return new Contact("test@domain.com", "Scott", "Tiger", new Date(),
                "test@perso.fr");
    }

    public FullContact retrieveFull() {
        return new FullContact("test@domain.com", "Scott", "Tiger", new Date(),
                "test@perso.fr", "1 Main Street", "Restlet city, 0102",
                "RESTland", "+123.456", "+789012");
    }

}
