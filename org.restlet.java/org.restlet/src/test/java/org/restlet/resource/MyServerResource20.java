/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import java.util.Date;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Sample server resource.
 * 
 * @author Jerome Louvel
 */
public class MyServerResource20 extends ServerResource implements MyResource20 {

    public static void main(String[] args) throws Exception {
        Server server = new Server(Protocol.HTTP, 8111);
        server.setNext(MyServerResource20.class);
        server.start();
    }

    public MyBean represent() throws MyException01 {
        throw new MyException01(new Date());
    }

    @Override
    public MyBean representAndSerializeException() throws MyException02 {
        throw new MyException02("my custom error");
    }

}
