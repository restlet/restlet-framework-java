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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource15TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource15.class);
    }

    /**
     * The Java serialization (ObjectRepresentation) works well when the target class is correctly
     * available at runtime. The usage of annotation like @Post on a generic method leads to the
     * DefaultConverter to handle Object class and not Serializable interface. (cf
     * AbstractGenericAnnotatedServerResource class).
     */
    @Test
    void shouldFailBecauseServerResourceAnnotationResidesOnGenericServerResource() {
        MyBean myBean = new MyBean("test", "description");
        ResourceException resourceException =
                assertThrows(
                        ResourceException.class,
                        () -> clientResource.post(myBean, MediaType.APPLICATION_JAVA_OBJECT));
        assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, resourceException.getStatus());
    }
}
