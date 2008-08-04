/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Component;
import org.restlet.Server;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit test case for the Spring extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringTestCase extends TestCase {

    public void testSpring() throws Exception {
        // Load the Spring container
        final ClassPathResource resource = new ClassPathResource(
                "org/restlet/test/SpringTestCase.xml");
        final BeanFactory factory = new XmlBeanFactory(resource);

        // Start the Restlet component
        final Component component = (Component) factory.getBean("component");
        component.start();
        Thread.sleep(500);
        component.stop();
    }

    public void testSpringServerProperties() {
        final ClassPathResource resource = new ClassPathResource(
                "org/restlet/test/SpringTestCase.xml");
        final BeanFactory factory = new XmlBeanFactory(resource);

        final Server server = (Server) factory.getBean("server");

        assertEquals("value1", server.getContext().getParameters()
                .getFirstValue("key1"));
        assertEquals("value2", server.getContext().getParameters()
                .getFirstValue("key2"));

    }

}
