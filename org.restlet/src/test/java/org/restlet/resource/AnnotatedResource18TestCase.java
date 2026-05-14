/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource18TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource18.class);
    }

    @Test
    void testQuery() throws IOException, ClassNotFoundException {
        MyBean myBean = new MyBean("test", "description");
        Representation rep =
                clientResource.post(
                        new ObjectRepresentation<>(myBean), MediaType.APPLICATION_JAVA_OBJECT);
        assertNotNull(rep);
        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, rep.getMediaType());
        ObjectRepresentation<MyBean> jr =
                new ObjectRepresentation<>(rep, MyBean.class.getClassLoader());
        assertNotNull(jr.getObject());
        Assertions.assertEquals("test", jr.getObject().getName());
    }
}
