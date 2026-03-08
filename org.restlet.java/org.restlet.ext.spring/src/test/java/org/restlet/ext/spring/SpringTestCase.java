/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.engine.Engine;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test case for the Spring extension.
 * 
 * @author Jerome Louvel
 */
public class SpringTestCase {

    public static int TEST_PORT = 1337; // referenced in SpringTestCase.xml

    @Test
    public void testSpring() throws Exception {
        // Start the Restlet component
        Component component = (Component) ctx.getBean("component");
        component.start();
        assertTrue(component.isStarted());
        component.stop();
        assertFalse(component.isStarted());
    }

    @Test
    public void testSpringServerProperties() {
        Server server = (Server) ctx.getBean("server");

        assertEquals("value1", server.getContext().getParameters()
                .getFirstValue("key1"));
        assertEquals("value2", server.getContext().getParameters()
                .getFirstValue("key2"));
    }

    private ClassPathXmlApplicationContext ctx;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        ctx = new ClassPathXmlApplicationContext("org/restlet/ext/spring/SpringTestCase.xml");
    }

    @AfterEach
    void cleanUp() {
        Engine.clearThreadLocalVariables();
        ctx.close();
    }

}
