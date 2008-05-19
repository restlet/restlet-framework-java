/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.example.spring.book.rest.ch7;

import org.restlet.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple demo application that can be run either as a standalone application
 * (http://localhost:3000/v1/) or inside a servlet container
 * (http://localhost:8080/v1/).
 * 
 * @author Konstantin Laufer (laufer@cs.luc.edu)
 */
public class Main {

    public static void main(String... args) throws Exception {
        // Load the Spring application context
        ApplicationContext springContext = new ClassPathXmlApplicationContext(
                new String[] {
                        "org/restlet/example/spring/book/rest/ch7/config/applicationContext-router.xml",
                        "org/restlet/example/spring/book/rest/ch7/config/applicationContext-server.xml" });

        // Obtain the Restlet component from the Spring context and start it
        ((Component) springContext.getBean("top")).start();
    }

}
