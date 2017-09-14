/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.spring;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.test.RestletTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test case for the Spring extension.
 * 
 * @author Jerome Louvel
 */
public class SpringTestCase extends RestletTestCase {

    public void testSpring() throws Exception {
        // Load the Spring container
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "org/restlet/test/ext/spring/SpringTestCase.xml");

        // Start the Restlet component
        Component component = (Component) ctx.getBean("component");
        component.start();
        Thread.sleep(500);
        component.stop();
        ctx.close();
    }

    public void testSpringServerProperties() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "org/restlet/test/ext/spring/SpringTestCase.xml");
        Server server = (Server) ctx.getBean("server");

        assertEquals("value1", server.getContext().getParameters()
                .getFirstValue("key1"));
        assertEquals("value2", server.getContext().getParameters()
                .getFirstValue("key2"));
        ctx.close();
    }

}
