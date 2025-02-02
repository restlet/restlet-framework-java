/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.engine.header.ProductReader;
import org.restlet.engine.header.ProductWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test {@link org.restlet.data.Product}.
 *
 * @author Thierry Boileau
 */
public class ProductTokenTestCase {

    @ParameterizedTest(name = "{1} {2}")
    @MethodSource("mainProductTestCases")
    public void testMainProduct(final String userAgent, final String productName, final String productVersion, final String productComment) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent);
        Product product = clientInfo.getMainAgentProduct();

        assertEquals(productName, product.getName());
        assertEquals(productVersion, product.getVersion());
        assertEquals(productComment, product.getComment());
    }

    private static Stream<Arguments> mainProductTestCases() {
        return Stream.of(
                Arguments.of("Mozilla/4.0 (compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)",
                        "MSIE", "6.0", null),
                Arguments.of("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.8) Gecko/20051107 Camino/1.0b1",
                        "Camino", "1.0b1", null),
                Arguments.of("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.2) Gecko/20020508 Netscape6/6.1",
                        "Netscape6", "6.1", null),
                Arguments.of("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Iceweasel/2.0 (Debian-2.0+dfsg-1)",
                        "Iceweasel", "2.0", "Debian-2.0+dfsg-1"),
                Arguments.of("Mozilla/5.0 (compatible; Konqueror/3.5; Linux 2.6.15-1.2054_FC5; X11; i686; en_US) KHTML/3.5.4 (like Gecko)",
                        "Konqueror", "3.5.4", "like Gecko"),
                Arguments.of("Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)",
                        "MSIE", "5.5", null),
                Arguments.of("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
                        "MSIE", "6.0", null),
                Arguments.of("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/521.25 (KHTML, like Gecko) Safari/521.24",
                        "Safari", "521.24", null),
                Arguments.of("Opera/9.00 (Macintosh; PPC Mac OS X; U; en)",
                        "Opera", "9.00", null),
                Arguments.of("Wget/1.9",
                        "Wget", "1.9", null),
                Arguments.of("Restlet-Framework/2.2-SNAPSHOT",
                        "Restlet-Framework", "2.2-SNAPSHOT", null)
        );
    }

    @Test
    public void testProductTokens() {
        final String userAgent1 = "Mozilla/4.0 (compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)";
        final String userAgent2 = "Advanced Browser (http://www.avantbrowser.com)";
        final String userAgent3 = "Mozilla/5.0";
        final String userAgent4 = "Mozilla";
        final String userAgent5 = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.8) Gecko/20051107 Camino/1.0b1";
        final String userAgent6 = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Iceweasel/2.0 (Debian-2.0+dfsg-1)";
        final String userAgent7 = "Restlet-Framework/2.2-SNAPSHOT";

        List<Product> list = ProductReader.read(userAgent1);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("4.0", list.get(0).getVersion());
        assertEquals(
                "compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;",
                list.get(0).getComment());

        list = ProductReader.read(userAgent2);
        assertEquals(1, list.size());
        assertEquals(list.get(0).getName(), "Advanced Browser");
        assertNull(list.get(0).getVersion());
        assertEquals(list.get(0).getComment(), "http://www.avantbrowser.com");

        list = ProductReader.read(userAgent3);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("5.0", list.get(0).getVersion());
        assertNull(list.get(0).getComment());

        list = ProductReader.read(userAgent4);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertNull(list.get(0).getVersion());
        assertNull(list.get(0).getComment());

        list = ProductReader.read(userAgent5);
        assertEquals(3, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("5.0", list.get(0).getVersion());
        assertEquals("Macintosh; U; PPC Mac OS X; en-US; rv:1.8", list.get(0)
                .getComment());
        assertEquals("Gecko", list.get(1).getName());
        assertEquals("20051107", list.get(1).getVersion());
        assertNull(list.get(1).getComment());
        assertEquals("Camino", list.get(2).getName());
        assertEquals("1.0b1", list.get(2).getVersion());
        assertNull(list.get(2).getComment());

        list = ProductReader.read(userAgent6);
        assertEquals(3, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("5.0", list.get(0).getVersion());
        assertEquals("X11; U; Linux i686; en-US; rv:1.8.1", list.get(0)
                .getComment());
        assertEquals("Gecko", list.get(1).getName());
        assertEquals("20061024", list.get(1).getVersion());
        assertNull(list.get(1).getComment());
        assertEquals("Iceweasel", list.get(2).getName());
        assertEquals("2.0", list.get(2).getVersion());
        assertEquals("Debian-2.0+dfsg-1", list.get(2).getComment());

        list = ProductReader.read(userAgent7);
        assertEquals(1, list.size());
        assertEquals("Restlet-Framework", list.get(0).getName());
        assertEquals("2.2-SNAPSHOT", list.get(0).getVersion());
        assertNull(list.get(0).getComment());
    }

    @Test
    public void testWriteThenRead() {
        final List<Product> products = new ArrayList<>();
        products.add(new Product("Product", "1.2", null));
        products.add(new Product("Nre", "1.1m4", "This is a comment"));

        List<Product> list = ProductReader.read(ProductWriter.write(products));
        assertEquals(2, list.size());
        assertEquals("Product", list.get(0).getName());
        assertEquals("1.2", list.get(0).getVersion());
        assertNull(list.get(0).getComment());
        assertEquals("Nre", list.get(1).getName());
        assertEquals("1.1m4", list.get(1).getVersion());
        assertEquals("This is a comment", list.get(1).getComment());
    }

}
