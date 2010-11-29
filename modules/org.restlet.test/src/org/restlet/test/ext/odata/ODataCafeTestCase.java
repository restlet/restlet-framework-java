package org.restlet.test.ext.odata;

import java.util.Iterator;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.odata.Query;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.cafe.Cafe;
import org.restlet.test.ext.odata.cafe.CafeService;
import org.restlet.test.ext.odata.cafe.Contact;
import org.restlet.test.ext.odata.cafe.Item;

/**
 * Test case for OData service.
 * 
 * @author Thierry Boileau
 * 
 */
public class ODataCafeTestCase extends RestletTestCase {

    /** Inner component. */
    private Component component = new Component();

    /** OData service used for all tests. */
    private CafeService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.CLAP);

        component.getDefaultHost().attach("/Cafe.svc",
                new org.restlet.test.ext.odata.cafe.CafeApplication());

        component.start();

        service = new CafeService();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        super.tearDown();
    }

    /**
     * Tests the parsing of Feed element.
     */
    public void testQueryCafes() {

        Query<Cafe> query = service.createCafeQuery("/Cafes");
        Iterator<Cafe> iterator = query.iterator();

        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());
    }

    /**
     * Tests the parsing of Feed element with expansion of the one to one
     * association "Contact".
     */
    public void testQueryCafesExpandContact() {
        Query<Cafe> query1 = service.createCafeQuery("/Cafes");
        Query<Cafe> query2 = query1.expand("Contact");

        Iterator<Cafe> iterator = query2.iterator();
        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        Contact contact = cafe.getContact();
        assertNotNull(contact);
        assertEquals("1", contact.getId());
        assertEquals("Agathe Zeblues", contact.getName());
        assertEquals("Chief", contact.getTitle());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());

        contact = cafe.getContact();
        assertNotNull(contact);
        assertEquals("2", contact.getId());
        assertEquals("Alphonse Denltas", contact.getName());
        assertEquals("Boss", contact.getTitle());
    }

    /**
     * Tests the parsing of Feed element with expansion of the one to many
     * association "Items".
     */
    public void testQueryCafesExpandItem() {
        Query<Cafe> query1 = service.createCafeQuery("/Cafes");
        Query<Cafe> query2 = query1.expand("Items");

        Iterator<Cafe> iterator = query2.iterator();
        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        Iterator<Item> iterator2 = cafe.getItems().iterator();
        assertTrue(iterator2.hasNext());
        Item item = iterator2.next();
        assertEquals("1", item.getId());
        assertEquals("Poulet au curry", item.getDescription());

        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("2", item.getId());
        assertEquals("Pate", item.getDescription());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());

        iterator2 = cafe.getItems().iterator();
        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("3", item.getId());
        assertEquals("Banana Split", item.getDescription());

        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("4", item.getId());
        assertEquals("Cotes du Rhone", item.getDescription());
    }

    /**
     * Tests the parsing of Entry element.
     */
    public void testQueryContact() {
        Query<Contact> query = service.createContactQuery("/Contacts('1')");
        Iterator<Contact> iterator = query.iterator();
        assertTrue(iterator.hasNext());
        Contact contact = iterator.next();
        assertNotNull(contact);
        assertEquals("1", contact.getId());
        assertEquals("Agathe Zeblues", contact.getName());
        assertEquals("Chief", contact.getTitle());
    }

    /**
     * Tests the server paging feature.
     */
    public void testServerPaging() {
        Query<Cafe> query1 = service.createCafeQuery("/Cafes");
        Query<Cafe> query2 = query1.skipToken("Skip1");

        Iterator<Cafe> iterator = query2.iterator();

        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());
    }
}
