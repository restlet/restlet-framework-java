/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

/**
 * Reusable hello world Restlet.
 * 
 * @author Jerome Louvel
 */
public class HelloWorldRestlet extends Restlet {

    @Override
    public void handle(Request request, Response response) {
        response.setEntity("hello, world", MediaType.TEXT_PLAIN);
    }

}
