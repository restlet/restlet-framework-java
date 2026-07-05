/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Product}. */
class ProductTestCase {

    @Test
    void constructor_setsAllProperties() {
        Product product = new Product("Restlet", "2.5", "a framework");
        assertEquals("Restlet", product.getName());
        assertEquals("2.5", product.getVersion());
        assertEquals("a framework", product.getComment());
    }

    @Test
    void constructor_withNullComment_returnsNullComment() {
        Product product = new Product("Restlet", "2.5", null);
        assertNull(product.getComment());
    }

    @Test
    void settersUpdateState() {
        Product product = new Product("Restlet", "2.5", "a framework");
        product.setName("Jetty");
        product.setVersion("9.0");
        product.setComment("a server");
        assertEquals("Jetty", product.getName());
        assertEquals("9.0", product.getVersion());
        assertEquals("a server", product.getComment());
    }
}
