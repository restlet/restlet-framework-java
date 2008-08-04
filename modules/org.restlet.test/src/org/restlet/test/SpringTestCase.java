/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
