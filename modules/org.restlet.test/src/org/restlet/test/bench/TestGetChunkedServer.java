/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.bench;

import java.io.IOException;

import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.InputRepresentation;

public class TestGetChunkedServer {

    public static void main(String[] args) throws Exception {

        Server server = new Server(Protocol.HTTP, 8554, new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    FileRepresentation fr = new FileRepresentation(
                            "file:///c:/test.mpg", MediaType.VIDEO_MPEG);
                    System.out.println("Size sent: " + fr.getSize());
                    InputRepresentation ir = new InputRepresentation(fr
                            .getStream(), fr.getMediaType());
                    response.setEntity(ir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.start();
    }

}
