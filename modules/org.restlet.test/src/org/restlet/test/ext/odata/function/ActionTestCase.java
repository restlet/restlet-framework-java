package org.restlet.test.ext.odata.function;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.test.RestletTestCase;

/**
 * Test case for actions in Restlet.
 */
public class ActionTestCase extends RestletTestCase {

    /** Inner component. */
    private Component component = new Component();

    /** OData service used for all tests. */
    private FunctionService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.CLAP);
        component.getDefaultHost().attach("/Unit.svc",
                new UnitApplication());
        component.start();

        service = new FunctionService();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        super.tearDown();
    }
    /**
     * Tests the actions.
     */
    public void testAction() {
    	FunctionService service = new FunctionService();
  		List<Double> values = null;
  		try {
  			List<Double> doubleList = new ArrayList<Double>();
  			doubleList.add(240.0);
  			doubleList.add(450.0);
  			 values = service.convertDoubleArray("1", "2", doubleList, new Double(1D));  			
  		} catch (Exception e) {
			Context.getCurrentLogger().warning(
                    "Exception occurred while calling a function: " + e.getMessage());
  			Assert.fail();
  		} 	
  		assertNotNull(values);
  		assertTrue(values.size()>0);
  		assertEquals("20.0", values.get(0));
  		assertEquals("65.6", values.get(1));
      }

}
