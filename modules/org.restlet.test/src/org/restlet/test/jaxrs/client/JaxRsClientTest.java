package org.restlet.test.jaxrs.client;

import java.awt.Point;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.ext.jaxrs.client.JaxRsClientResource;
import org.restlet.test.jaxrs.services.point.EchoResource;
import org.restlet.test.jaxrs.services.point.EchoResourceImpl;
import org.restlet.test.jaxrs.services.tests.JaxRsTestCase;

public class JaxRsClientTest extends JaxRsTestCase {

	
	@Override
	protected Application getApplication() {
		return new Application() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Set<Class<?>> getClasses() {
				return (Set) Collections.singleton(EchoResourceImpl.class);
			}
		};
	}
	
	public void testEchoString()throws Exception {
		final JaxRsClientTest clientTest = startSocketServerDaemon();
		
		EchoResource echoResource = JaxRsClientResource.createJaxRsClient("http://localhost:"+clientTest.getServerPort(), EchoResource.class);
		
		assertEquals("this is a test", echoResource.echo("this is a test"));
		
		clientTest.stopServer();
	}
	
	/*
	 * Shows the problem addressed in:
	 * https://github.com/restlet/restlet-framework-java/issues/441 
	 */
	public void testEchoPoint()throws Exception {
		final JaxRsClientTest clientTest = startSocketServerDaemon();
		
		EchoResource echoResource = JaxRsClientResource.createJaxRsClient("http://localhost:"+clientTest.getServerPort(), EchoResource.class);
		
		assertEquals(1, echoResource.echoPoint(new Point(1,2)).x);
		
		clientTest.stopServer();
	}

	private JaxRsClientTest startSocketServerDaemon() throws InterruptedException {
		final JaxRsClientTest clientTest = new JaxRsClientTest();
		setUseTcp(true);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					clientTest.startServer(clientTest.createApplication());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		t.setDaemon(true);
		t.start();
		
		//give the server a chance to come up before using it
		Thread.sleep(500);
		return clientTest;
	}


}
