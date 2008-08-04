/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.ClientInfo;
import org.restlet.data.Product;
import org.restlet.util.Engine;

/**
 * Test {@link org.restlet.data.Product}.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class ProductTokenTestCase extends RestletTestCase {

    public void testMainProduct() {

        final String userAgent1 = "Mozilla/4.0 (compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)";
        final String userAgent2 = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.8) Gecko/20051107 Camino/1.0b1";
        final String userAgent3 = "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.2) Gecko/20020508 Netscape6/6.1";
        final String userAgent4 = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Iceweasel/2.0 (Debian-2.0+dfsg-1)";
        final String userAgent5 = "Mozilla/5.0 (compatible; Konqueror/3.5; Linux 2.6.15-1.2054_FC5; X11; i686; en_US) KHTML/3.5.4 (like Gecko)";
        final String userAgent6 = "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)";
        final String userAgent7 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";
        final String userAgent8 = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/521.25 (KHTML, like Gecko) Safari/521.24";
        final String userAgent9 = "Opera/9.00 (Macintosh; PPC Mac OS X; U; en)";
        final String userAgent10 = "Wget/1.9";
        final String userAgent11 = "Noelios-Restlet-Engine/1.9-SNAPSHOT";

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent1);
        Product product = clientInfo.getMainAgentProduct();
        assertEquals("MSIE", product.getName());
        assertEquals("6.0", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent2);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Camino", product.getName());
        assertEquals("1.0b1", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent3);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Netscape6", product.getName());
        assertEquals("6.1", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent4);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Iceweasel", product.getName());
        assertEquals("2.0", product.getVersion());
        assertEquals("Debian-2.0+dfsg-1", product.getComment());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent5);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Konqueror", product.getName());
        assertEquals("3.5.4", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent6);
        product = clientInfo.getMainAgentProduct();
        assertEquals("MSIE", product.getName());
        assertEquals("5.5", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent7);
        product = clientInfo.getMainAgentProduct();
        assertEquals("MSIE", product.getName());
        assertEquals("6.0", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent8);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Safari", product.getName());
        assertEquals("521.24", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent9);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Opera", product.getName());
        assertEquals("9.00", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent10);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Wget", product.getName());
        assertEquals("1.9", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent11);
        product = clientInfo.getMainAgentProduct();
        assertEquals("Noelios-Restlet-Engine", product.getName());
        assertEquals("1.9-SNAPSHOT", product.getVersion());

        clientInfo = new ClientInfo();
        clientInfo.setAgent(userAgent7);
        final Map<String, String> map = clientInfo.getAgentAttributes();
        for (final Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry);
        }
    }

    /**
     * Tests.
     */
    public void testProductTokens() throws Exception {
        final String userAgent1 = "Mozilla/4.0 (compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;)";
        final String userAgent2 = "Advanced Browser (http://www.avantbrowser.com)";
        final String userAgent3 = "Mozilla/5.0";
        final String userAgent4 = "Mozilla";
        final String userAgent5 = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.8) Gecko/20051107 Camino/1.0b1";
        final String userAgent6 = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1) Gecko/20061024 Iceweasel/2.0 (Debian-2.0+dfsg-1)";

        List<Product> list = Engine.getInstance().parseUserAgent(userAgent1);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("4.0", list.get(0).getVersion());
        assertEquals(
                "compatible; MSIE 6.0; America Online Browser 1.1; rev1.1; Windows NT 5.1;",
                list.get(0).getComment());

        list = Engine.getInstance().parseUserAgent(userAgent2);
        assertEquals(1, list.size());
        assertEquals(list.get(0).getName(), "Advanced Browser");
        assertNull(list.get(0).getVersion());
        assertEquals(list.get(0).getComment(), "http://www.avantbrowser.com");

        list = Engine.getInstance().parseUserAgent(userAgent3);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertEquals("5.0", list.get(0).getVersion());
        assertNull(list.get(0).getComment());

        list = Engine.getInstance().parseUserAgent(userAgent4);
        assertEquals(1, list.size());
        assertEquals("Mozilla", list.get(0).getName());
        assertNull(list.get(0).getVersion());
        assertNull(list.get(0).getComment());

        list = Engine.getInstance().parseUserAgent(userAgent5);
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

        list = Engine.getInstance().parseUserAgent(userAgent6);
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

        final List<Product> products = new ArrayList<Product>();
        products.add(new Product("Product", "1.2", null));
        products.add(new Product("Nre", "1.1m4", "This is a comment"));

        list = Engine.getInstance().parseUserAgent(
                Engine.getInstance().formatUserAgent(products));
        assertEquals(2, list.size());
        assertEquals("Product", list.get(0).getName());
        assertEquals("1.2", list.get(0).getVersion());
        assertNull(list.get(0).getComment());
        assertEquals("Nre", list.get(1).getName());
        assertEquals("1.1m4", list.get(1).getVersion());
        assertEquals("This is a comment", list.get(1).getComment());

    }

}
