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
import org.restlet.ext.odata.batch.request.impl.ChangeSetRequestImpl;
import org.restlet.ext.odata.batch.request.impl.UpdateEntityRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.response.ChangeSetResponse;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.crud.Cafe;

/**
 * Test case for RestletBatch service for UPDATE operation on entities.
 * 
 */
public class UpdateCafeTestCase extends RestletTestCase {


	/** The Constant cafeName. */
	private static final String cafeName = "TestName";

	/** The Constant cafeNameUpdated. */
	private static final String cafeNameUpdated = "TestName-updated";

	/** The Constant cafeId. */
	private static final String cafeId = "40";

	/** The Constant cafeZipCode. */
	private static final int cafeZipCode = 111111;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(UpdateCafeTestCase.class.getName());

	/** The Constant cafeCity. */
	private static final String cafeCity = "TestCity";

	/** Inner component. */
	private Component component = new Component();


	/** OData service used for all tests. */
	@SuppressWarnings("unused")
	private CafeService service;

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.restlet.test.RestletTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		component.stop();
		component = null;
		super.tearDown();
	}

	/**
	 * Test method for UPDATE operation on simple entities.
	 */
	public void testUpdate() {
		CafeService service = new CafeService();
		Cafe cafe = new Cafe();
		cafe.setId(cafeId);
		cafe.setName(cafeName);
		cafe.setCity(cafeCity);
		cafe.setZipCode(cafeZipCode);


		//update
		Cafe cafeU = new Cafe();
		cafeU.setId(cafeId);
		cafeU.setName(cafeName);
		cafeU.setCity(cafeCity);
		cafeU.setZipCode(cafeZipCode);
		cafeU.setName(cafeNameUpdated);
		try {
			BatchRequest br = service.createBatchRequest();
			ChangeSetRequestImpl changeSetRequest = new ChangeSetRequestImpl();
			UpdateEntityRequest updateEntityRequest = new UpdateEntityRequest(service,cafe);
			changeSetRequest.addRequest(updateEntityRequest);
			List<BatchResponse> responses = br.addRequest(changeSetRequest).execute();
			dumpResponse(responses);

			Query<Cafe> updatequery = service.updateCafeQuery("/Cafes('40')");
			Response latestResponse = updatequery.getService().getLatestResponse();
			assertTrue(latestResponse.getStatus().isSuccess());
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Assert.fail();
		}
	}

	/**
	 * Dump response.
	 *
	 * @param responses the responses
	 */
	@SuppressWarnings("unchecked")
	public static void dumpResponse(List<BatchResponse> responses) {
		for (BatchResponse batchResponse : responses) {
			Object entity = batchResponse.getEntity();
			if(batchResponse instanceof ChangeSetResponse){
				LOGGER.info("Dumping changeset");
				dumpResponse((List<BatchResponse>)entity);
				LOGGER.info("Done with changeset");
			}else{
				LOGGER.info("Status ="+ batchResponse.getStatus());
				LOGGER.info("Entity = "+ entity);
				MultivaluedMap<String, String> headers = batchResponse.getHeaders();
				if(headers!=null){
					Set<String> keySet = headers.keySet();
					LOGGER.info("Headers : ");
					for (String key : keySet) {
						List<String> value = headers.get(key);
						LOGGER.info("Key ="+ key + "/t"+"value = "+ value);
					}
				}
			}
		}	
	}


}