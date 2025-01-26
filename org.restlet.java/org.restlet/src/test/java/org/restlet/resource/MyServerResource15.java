/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

/**
 * Sample server resource implementing abstract generic class.
 * 
 * @author Jerome Louvel
 */
public class MyServerResource15 extends AbstractGenericAnnotatedServerResource<MyBean> {

    @Override
    public MyBean addResponse(MyBean representation) {
        return representation;
    }
}
