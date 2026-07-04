/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;
import org.restlet.data.Method;

/** Unit tests for {@link MethodReader}. */
class MethodReaderTestCase {

    @Test
    void readValue_parsesMethod() throws Exception {
        MethodReader reader = new MethodReader("GET");
        assertEquals(Method.GET, reader.readValue());
    }

    @Test
    void addValues_appendsParsedMethodToCollection() {
        Header header = new Header("Allow", "POST");
        List<Method> collection = new ArrayList<>();

        MethodReader.addValues(header, collection);

        assertEquals(List.of(Method.POST), collection);
    }
}
