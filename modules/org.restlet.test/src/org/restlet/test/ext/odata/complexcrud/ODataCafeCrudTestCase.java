package org.restlet.test.ext.odata.complexcrud;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.odata.Query;
import org.restlet.test.RestletTestCase;

/**
 * Test case for OData service for CUD operation on complex entities and
 * collection.
 * 
 */
public class ODataCafeCrudTestCase extends RestletTestCase {

	/** Inner component. */
	private Component component = new Component();

	/** OData service used for all tests. */
	@SuppressWarnings("unused")
	private CafeService service;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		component.getServers().add(Protocol.HTTP, 8111);
		component.getClients().add(Protocol.CLAP);
		component.getDefaultHost().attach("/Cafe.svc",
				new CafeCrudApplication());
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
	 * Test method for crud operation on complex entities and collection.
	 */
	public void testCrudComplexEntity() {
		CafeService service = new CafeService();

		// create.
		Cafe cafe = buildCafeEntity();
		
		try {
			service.addEntity(cafe);
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot add entity due to: " + e.getMessage());
			Assert.fail();
		}
		
		Query<Cafe> query = service.createCafeQuery("/Cafes");
		Cafe cafe1 = query.iterator().next();
		assertEquals("TestName", cafe1.getName());
		assertEquals("30", cafe1.getId());
		assertEquals(111111, cafe1.getZipCode());
		assertNotNull(cafe1.getSpatial());
		Response latestResponse = query.getService().getLatestResponse();
		assertEquals(Status.SUCCESS_OK, latestResponse.getStatus());

		// // Update.
		cafe1.setName("TestName-update");
		
		try {
			service.updateEntity(cafe1);
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot update entity due to: " + e.getMessage());
			Assert.fail();
		}
		
		Query<Cafe> query3 = service.createCafeQuery("/Cafes('30')");
		
		Cafe cafe2 = query3.iterator().next();
		assertEquals("TestName-updated", cafe2.getName());
		assertNotNull(cafe1.getSpatial());
		latestResponse = query3.getService().getLatestResponse();
		assertEquals(Status.SUCCESS_OK, latestResponse.getStatus());

		// Delete
		try {
			service.deleteEntity(cafe2);
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Cannot delete entity due to: " + e.getMessage());
			Assert.fail();
		}
		latestResponse = query3.getService().getLatestResponse();
		assertEquals(Status.SUCCESS_NO_CONTENT, latestResponse.getStatus());
	}

	private Cafe buildCafeEntity() {
		Cafe cafe = new Cafe();
		cafe.setId("30");
		cafe.setName("TestName");
		cafe.setCity("TestCity");
		cafe.setZipCode(111111);

		Point point = new Point();
		point.setGeo_name("GEONAME");
		point.setGeo_type("LINESTRING");

		StructAny structAny = new StructAny();
		structAny.setName("md");
		structAny.setType("FLOAT");
		structAny.setUnit("meters");
		structAny.setUnitType("depth measure");
		structAny.setValues("[0.0,2670.9678]");

		List<StructAny> properties = new ArrayList<StructAny>();
		properties.add(structAny);

		List<java.lang.Double> x = new ArrayList<java.lang.Double>();
		x.add(7.29d);
		x.add(7.29d);
		List<java.lang.Double> y = new ArrayList<java.lang.Double>();
		y.add(65.32000000000001d);
		y.add(65.32000000000001d);

		point.setProperties(properties);
		point.setX(x);
		point.setY(y);
		cafe.setSpatial(point);
		return cafe;
	}

}
