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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Product;

class ProductReaderTestCase {

    @Test
    void read_nullInput_returnsEmptyList() {
        assertTrue(ProductReader.read(null).isEmpty());
    }

    @Test
    void read_tokenOnly_parsesName() {
        List<Product> products = ProductReader.read("Restlet");
        assertEquals(1, products.size());
        assertEquals("Restlet", products.getFirst().getName());
        assertNull(products.getFirst().getVersion());
    }

    @Test
    void read_tokenAndVersion_parsesBoth() {
        List<Product> products = ProductReader.read("Restlet/2.7");
        assertEquals(1, products.size());
        assertEquals("Restlet", products.getFirst().getName());
        assertEquals("2.7", products.getFirst().getVersion());
    }

    @Test
    void read_tokenVersionAndComment_parsesAll() {
        List<Product> products = ProductReader.read("Restlet/2.7 (Java)");
        assertEquals(1, products.size());
        assertEquals("Restlet", products.getFirst().getName());
        assertEquals("2.7", products.getFirst().getVersion());
        assertEquals("Java", products.getFirst().getComment());
    }

    @Test
    void read_multipleProducts_parsesEachOne() {
        List<Product> products = ProductReader.read("Restlet/2.7 (Java) Mozilla/5.0");
        assertEquals(2, products.size());
        assertEquals("Restlet", products.getFirst().getName());
        assertEquals("Mozilla", products.get(1).getName());
        assertEquals("5.0", products.get(1).getVersion());
    }
}
