package org.restlet.test.batch.crud;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import junit.framework.Assert;

import org.restlet.Component;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.ext.odata.Query;
import org.restlet.ext.odata.batch.request.BatchRequest;
import org.restlet.ext.odata.batch.request.impl.GetEntityRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.response.ChangeSetResponse;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.crud.Cafe;

/**
 * Test case for RestletBatch service for GET operation on entities.
 */
public class GetCafeTestCase extends RestletTestCase {

	/** The Constant cafeName. */
	private static final String cafeName = "TestName";

	/** The Constant cafeId. */
	private static final String cafeId = "40";

	/** The Constant cafeZipCode. */
	private static final int cafeZipCode = 111111;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(GetCafeTestCase.class
			.getName());

	/** The Constant cafeCity. */
	private static final String cafeCity = "TestCity";

	/** Inner component. */
	private Component component = new Component();

	/** OData service used for all tests. */
	@SuppressWarnings("unused")
	private CafeService service;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.test.RestletTestCase#setUp()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.test.RestletTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		component.stop();
		component = null;
		super.tearDown();
	}

	/**
	 * Test method for GET operation on simple entities.
	 */
	public void testGet() {

		// Get
		CafeService service = new CafeService();
		Cafe cafeG = new Cafe();
		cafeG.setId(cafeId);
		cafeG.setName(cafeName);
		cafeG.setCity(cafeCity);
		cafeG.setZipCode(cafeZipCode);
		try {
			BatchRequest br = service.createBatchRequest();
			// get request
			Query<Cafe> getQuery = service.createQuery("/Cafes('40')",
					Cafe.class);
			GetEntityRequest getEntityRequest = new GetEntityRequest(getQuery);
			List<BatchResponse> responses = br.addRequest(getEntityRequest)
					.execute();
			dumpResponse(responses);
			Assert.assertTrue(true);
			// Assert for response.
			Query<Cafe> getquery = service.getCafeQuery("/Cafes");
			Cafe cafe2 = getquery.iterator().next();
			assertEquals(cafeName, cafe2.getName());
			assertEquals(cafeId, cafe2.getId());
			assertEquals(cafeZipCode, cafe2.getZipCode());
			Response latestResponse = getQuery.getService().getLatestResponse();
			latestResponse = getquery.getService().getLatestResponse();
			assertTrue(latestResponse.getStatus().isSuccess());
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Assert.fail();
		}
	}

	/**
	 * Dump response.
	 * 
	 * @param responses
	 *            the responses
	 */
	@SuppressWarnings("unchecked")
	public static void dumpResponse(List<BatchResponse> responses) {
		for (BatchResponse batchResponse : responses) {
			Object entity = batchResponse.getEntity();
			if (batchResponse instanceof ChangeSetResponse) {
				LOGGER.info("Dumping changeset");
				dumpResponse((List<BatchResponse>) entity);
				LOGGER.info("Done with changeset");
			} else {
				LOGGER.info("Status =" + batchResponse.getStatus());
				LOGGER.info("Entity = " + entity);
				MultivaluedMap<String, String> headers = batchResponse
						.getHeaders();
				if (headers != null) {
					Set<String> keySet = headers.keySet();
					LOGGER.info("Headers : ");
					for (String key : keySet) {
						List<String> value = headers.get(key);
						LOGGER.info("Key =" + key + "/t" + "value = " + value);
					}
				}
			}
		}
	}

}