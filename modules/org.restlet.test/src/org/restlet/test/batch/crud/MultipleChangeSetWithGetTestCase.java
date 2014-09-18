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
import org.restlet.data.Status;
import org.restlet.ext.odata.Query;
import org.restlet.ext.odata.batch.request.BatchRequest;
import org.restlet.ext.odata.batch.request.impl.ChangeSetRequestImpl;
import org.restlet.ext.odata.batch.request.impl.CreateEntityRequest;
import org.restlet.ext.odata.batch.request.impl.DeleteEntityRequest;
import org.restlet.ext.odata.batch.request.impl.GetEntityRequest;
import org.restlet.ext.odata.batch.request.impl.UpdateEntityRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.response.ChangeSetResponse;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.crud.Cafe;

/**
 * The Class MultipleChangeSetWithGetTestCase.
 * Batch---
 * ---Get entity
 * ---changeset1 :Create
 * ---changeset2 :Update
 * ---changeset3 :Delete
 * Batch ends 
 * 
 */
public class MultipleChangeSetWithGetTestCase extends RestletTestCase {

	/** The Constant cafeName. */
	private static final String cafeName = "TestName";

	/** The Constant cafeId. */
	private static final String cafeId = "40";

	/** The Constant cafeZipCode. */
	private static final int cafeZipCode = 111111;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(MultipleChangeSetTestCase.class.getName());

	/** The Constant cafeCity. */
	private static final String cafeCity = "TestCity";

	/** The Constant cafeNameUpdated. */
	private static final String cafeNameUpdated = "TestName-updated";

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

	
	@Override
	protected void tearDown() throws Exception {
		component.stop();
		component = null;
		super.tearDown();
	}

	/**
	 * Test multiple operations with get.
	 */
	public void testMultipleOperationsWithGet() {
		CafeService service = new CafeService();

		// create & delete
		Cafe cafe = new Cafe();
		cafe.setId(cafeId);
		cafe.setName(cafeName);
		cafe.setCity(cafeCity);
		cafe.setZipCode(cafeZipCode);

		//create & update
		Cafe cafeU = new Cafe();
		cafe.setName(cafeNameUpdated);

		try {

			BatchRequest br = service.createBatchRequest();
			Query<Cafe> getQuery = service.createQuery("/Cafes('40')",Cafe.class);
			GetEntityRequest getEntityRequest = new GetEntityRequest(getQuery);
			ChangeSetRequestImpl changeSetRequest1 = new ChangeSetRequestImpl();
			CreateEntityRequest createEntityRequest = new CreateEntityRequest(service,cafe);
			changeSetRequest1.addRequest(createEntityRequest);
			ChangeSetRequestImpl changeSetRequest2 = new ChangeSetRequestImpl();
			UpdateEntityRequest updateEntityRequest = new UpdateEntityRequest(service, cafeU);
			changeSetRequest2.addRequest(updateEntityRequest);
			ChangeSetRequestImpl changeSetRequest3 = new ChangeSetRequestImpl();
			DeleteEntityRequest deleteEntityRequest = new DeleteEntityRequest(service, cafe);
			changeSetRequest3.addRequest(deleteEntityRequest);
			List<BatchResponse> responses = br.addRequest(getEntityRequest).addRequest(changeSetRequest1).addRequest(changeSetRequest2).addRequest(changeSetRequest3).execute();
			dumpResponse(responses);
			

			//Assert for response-create & delete
			Query<Cafe> createquery = service.createCafeQuery("/Cafes");
			Cafe cafe1=createquery.iterator().next();			
			assertEquals(cafeName, cafe1.getName());
			assertEquals(cafeId, cafe1.getId());
			assertEquals(cafeZipCode, cafe1.getZipCode());
			Response latestResponse = createquery.getService().getLatestResponse();
			latestResponse = createquery.getService().getLatestResponse();
			assertTrue(latestResponse.getStatus().isSuccess());
			Query<Cafe> deletequery = service.deleteCafeQuery(("/Cafes('40')"));
			latestResponse = deletequery.getService().getLatestResponse();
			assertEquals(Status.SUCCESS_OK,latestResponse.getStatus());
			
			//Assert for response-create & update		
			Query<Cafe> createquery2 = service.createCafeQuery("/Cafes");
			Cafe cafe2=createquery2.iterator().next();			
			assertEquals(cafeName, cafe2.getName());
			assertEquals(cafeId, cafe2.getId());
			assertEquals(cafeZipCode, cafe2.getZipCode());
			latestResponse = createquery.getService().getLatestResponse();
			latestResponse = createquery.getService().getLatestResponse();
			assertTrue(latestResponse.getStatus().isSuccess());
			Query<Cafe> updatequery = service.updateCafeQuery("/Cafes('40')");
			latestResponse = updatequery.getService().getLatestResponse();
			assertEquals(Status.SUCCESS_OK,latestResponse.getStatus());
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