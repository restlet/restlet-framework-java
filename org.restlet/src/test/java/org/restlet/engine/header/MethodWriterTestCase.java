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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.restlet.data.Method;

class MethodWriterTestCase {

    @Test
    void write_singleMethod_writesMethodName() {
        Set<Method> methods = new LinkedHashSet<>();
        methods.add(Method.GET);
        assertEquals("GET", MethodWriter.write(methods));
    }

    @Test
    void write_multipleMethods_joinsWithSeparator() {
        Set<Method> methods = new LinkedHashSet<>();
        methods.add(Method.GET);
        methods.add(Method.POST);
        assertEquals("GET, POST", MethodWriter.write(methods));
    }

    @Test
    void append_method_writesTokenizedName() {
        MethodWriter writer = new MethodWriter();
        writer.append(Method.PUT);
        assertTrue(writer.toString().contains("PUT"));
    }
}
