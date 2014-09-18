package org.restlet.test.ext.odata.streamcrud;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.odata.Query;
import org.restlet.ext.odata.streaming.StreamReference;
import org.restlet.test.RestletTestCase;

/**
 * Test case for OData service for CUD operation on entities.
 * 
 */
@SuppressWarnings("unused")
public class ODataCafeCrudStreamTestCase extends RestletTestCase {

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


    public void testStreamingCrudCafe() {
		CafeService service = new CafeService();

		//create.
		Cafe cafe = new Cafe();
		cafe.setId("30");
		cafe.setName("TestName");
		cafe.setCity("TestCity");
		cafe.setZipCode(111111); 
		String contentType="application/octet-stream";		
		String str = "TEST";
		// convert String into InputStream
		InputStream inputStream = new ByteArrayInputStream(str.getBytes());
		StreamReference attachment =null;
		try {			
			attachment = new StreamReference(contentType, inputStream);
			cafe.setAttachment(attachment);
			service.addEntity(cafe);
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Exception occurred while adding entity: " + e.getMessage());
			Assert.fail();
		}
		
		//read.
		Query<Cafe> query = service.createCafeQuery("/Cafes");
		Cafe cafe1 = query.iterator().next();
		Response latestResponse = query.getService().getLatestResponse();
		assertEquals(Status.SUCCESS_OK, latestResponse.getStatus());
		assertEquals("TestName", cafe1.getName());
        assertEquals("30", cafe1.getId());
        assertEquals(111111, cafe1.getZipCode());
        attachment = cafe1.getAttachment();  
        assertEquals(contentType, attachment.getContentType());
        try {
			inputStream=  attachment.getInputStream(service);
		} catch (IOException e) {
			Context.getCurrentLogger().warning(
                    "Exception occurred while reading input stream: " + e.getMessage());
			Assert.fail();
		}
        assertNotNull(inputStream);   
        
       
        //// Update.
        cafe1.setName("TestName-update");
        attachment = cafe1.getAttachment();
        str = "TEST-udate";
		// convert String into InputStream
		 inputStream = new ByteArrayInputStream(str.getBytes());       
        attachment.setInputStream(inputStream);
        //mandatory property if upadating stream
        attachment.setUpdateStreamData(true);
        cafe1.setAttachment(attachment);      
        
        try {
			service.updateEntity(cafe1);
		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Exception occurred while updating entity: " + e.getMessage());
			Assert.fail();
		}       
        
		
		// read.
		Query<Cafe> query3 = service.createCafeQuery("/Cafes('30')");
		Cafe cafe2 = query3.iterator().next();
		latestResponse = query3.getService().getLatestResponse();
		assertEquals(Status.SUCCESS_OK, latestResponse.getStatus());
		assertEquals("TestName-update", cafe2.getName());
		assertEquals("30", cafe2.getId());
		assertEquals(111111, cafe2.getZipCode());
		attachment = cafe2.getAttachment();
		assertEquals(contentType, attachment.getContentType());
		try {
			inputStream = attachment.getInputStream(service);
		} catch (IOException e) {
			Context.getCurrentLogger().warning(
                    "Exception occurred while fetching inputstream: " + e.getMessage());
			Assert.fail();
		}
		assertNotNull(inputStream);

		// Delete
        Query<Cafe> query4 = service.createCafeQuery("/Cafes");	
		Cafe cafe3 = query4.iterator().next();
		 try {
				service.deleteEntity(cafe3);
			} catch (Exception e) {
				Context.getCurrentLogger().warning(
	                    "Exception occurred while deleting entity: " + e.getMessage());
				Assert.fail();
			}
		 latestResponse = query4.getService().getLatestResponse();
		 assertEquals(Status.SUCCESS_NO_CONTENT, latestResponse.getStatus());
	
	
    }

}
