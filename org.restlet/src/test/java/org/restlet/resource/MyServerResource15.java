/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
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
