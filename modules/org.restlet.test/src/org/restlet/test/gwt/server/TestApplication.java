/*
 * Copyright 2008 Noelios Consulting.
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

package org.restlet.test.gwt.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Test application.
 * 
 * @author Rob Heittman
 */
public class TestApplication extends Application {

    @Override
    public Restlet createRoot() {
        return new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity(
                        "The Restlet server side is alive. Method called: "
                                + request.getMethod(), MediaType.TEXT_PLAIN);
            }
        };

    }

}
