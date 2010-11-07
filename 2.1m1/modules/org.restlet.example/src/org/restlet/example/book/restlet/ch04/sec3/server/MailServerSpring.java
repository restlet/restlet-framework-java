package org.restlet.example.book.restlet.ch04.sec3.server;

import org.restlet.Component;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Creates a Restlet component and application based on a Spring XML
 * configuration and starts them.
 */
public class MailServerSpring {

    public static void main(String[] args) throws Exception {
        // Load the Spring container
        ClassPathResource resource = new ClassPathResource(
                "org/restlet/example/book/restlet/ch04/sec3/server/component-spring.xml");
        BeanFactory factory = new XmlBeanFactory(resource);

        // Start the Restlet component
        Component component = (Component) factory.getBean("component");
        component.start();
    }

}
