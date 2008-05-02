/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.util.List;

import org.restlet.data.Product;

/**
 * Test {@link org.restlet.data.Product}.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class ProductTokenTestCase extends RestletTestCase {
    protected final static String DEFAULT_SCHEME = "http";

    protected final static String DEFAULT_SCHEMEPART = "//";

    /**
     * Tests.
     */
    public void testProductTokens() throws Exception {
        String userAgent1 = "Mozilla/4.0 (compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)";
        String userAgent2 = "Advanced Browser (http://www.avantbrowser.com)";
        String userAgent3 = "Mozilla/5.0";
        String userAgent4 = "Mozilla";
        String userAgent5 = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.8) Gecko/20051107 Camino/1.0b1";
        String userAgent6 = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Iceweasel/2.0 (Debian-2.0+dfsg-1)";

        Product product = Product.parse(userAgent1);
        assertEquals(product.getToken(), "Mozilla");
        assertEquals(product.getVersion(), "4.0");
        assertEquals(product.getComment(),
                "(compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)");

        product = Product.parse(userAgent2);
        assertEquals(product.getToken(), "Advanced Browser");
        assertNull(product.getVersion());
        assertEquals(product.getComment(), "(http://www.avantbrowser.com)");

        product = Product.parse(userAgent3);
        assertEquals(product.getToken(), "Mozilla");
        assertEquals(product.getVersion(), "5.0");
        assertNull(product.getComment());

        product = Product.parse(userAgent4);
        assertEquals(product.getToken(), "Mozilla");
        assertNull(product.getVersion());
        assertNull(product.getComment());

        List<Product> list = Product.parseUserAgent(userAgent1);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getToken(), "Mozilla");
        assertEquals(list.get(0).getVersion(), "4.0");
        assertEquals(list.get(0).getComment(),
                "(compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)");

        list = Product.parseUserAgent(userAgent2);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getToken(), "Advanced Browser");
        assertNull(list.get(0).getVersion());
        assertEquals(list.get(0).getComment(), "(http://www.avantbrowser.com)");

        list = Product.parseUserAgent(userAgent3);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getToken(), "Mozilla");
        assertEquals(list.get(0).getVersion(), "5.0");
        assertNull(list.get(0).getComment());

        list = Product.parseUserAgent(userAgent5);
        assertEquals(list.size(), 3);
        assertEquals(list.get(0).getToken(), "Mozilla");
        assertEquals(list.get(0).getVersion(), "5.0");
        assertEquals(list.get(0).getComment(),
                "(Macintosh; U; PPC Mac OS X; en-US; rv:1.8)");
        assertEquals(list.get(1).getToken(), "Gecko");
        assertEquals(list.get(1).getVersion(), "20051107");
        assertNull(list.get(1).getComment());
        assertEquals(list.get(2).getToken(), "Camino");
        assertEquals(list.get(2).getVersion(), "1.0b1");
        assertNull(list.get(2).getComment());

        list = Product.parseUserAgent(userAgent6);
        assertEquals(list.size(), 3);
        assertEquals(list.get(0).getToken(), "Mozilla");
        assertEquals(list.get(0).getVersion(), "5.0");
        assertEquals(list.get(0).getComment(),
                "(X11; U; Linux i686; en-US; rv:1.8.1)");
        assertEquals(list.get(1).getToken(), "Gecko");
        assertEquals(list.get(1).getVersion(), "20061024");
        assertNull(list.get(1).getComment());
        assertEquals(list.get(2).getToken(), "Iceweasel");
        assertEquals(list.get(2).getVersion(), "2.0");
        assertEquals(list.get(2).getComment(), "(Debian-2.0+dfsg-1)");
        

    }

}
