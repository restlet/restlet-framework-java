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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Product;

class ProductWriterTestCase {

    @Test
    void write_nameOnly_writesNameOnly() {
        assertEquals("Restlet", ProductWriter.write(List.of(new Product("Restlet", null, null))));
    }

    @Test
    void write_nameAndVersion_joinsWithSlash() {
        assertEquals(
                "Restlet/2.7", ProductWriter.write(List.of(new Product("Restlet", "2.7", null))));
    }

    @Test
    void write_nameVersionAndComment_appendsParenthesizedComment() {
        assertEquals(
                "Restlet/2.7 (Java)",
                ProductWriter.write(List.of(new Product("Restlet", "2.7", "Java"))));
    }

    @Test
    void write_multipleProducts_joinsWithSpace() {
        String result =
                ProductWriter.write(
                        Arrays.asList(
                                new Product("Restlet", "2.7", null),
                                new Product("Mozilla", "5.0", null)));
        assertEquals("Restlet/2.7 Mozilla/5.0", result);
    }

    @Test
    void write_nullOrEmptyName_throwsIllegalArgumentException() {
        List<Product> list = List.of(new Product(null, "2.7", null));
        assertThrows(IllegalArgumentException.class, () -> ProductWriter.write(list));
        List<Product> list1 = List.of(new Product("", "2.7", null));
        assertThrows(IllegalArgumentException.class, () -> ProductWriter.write(list1));
    }
}
