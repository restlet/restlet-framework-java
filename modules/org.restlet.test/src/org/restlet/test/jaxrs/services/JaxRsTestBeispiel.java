/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.test.jaxrs.services;

import org.restlet.Component;
import org.restlet.test.jaxrs.services.tests.JaxRsTestCase;

/**
 * 
 * @author Stephan
 *
 */
public class JaxRsTestBeispiel
{
    /**
     * 
     * @param args
     * @throws Exception
     */
	public static void main(String[] args) throws Exception
	{
		JaxRsTestBeispiel beispiel = new JaxRsTestBeispiel();
		beispiel.run();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception
	{
		Component component = JaxRsTestCase.startServer(JaxRsTestCase.PROTOCOL, JaxRsTestCase.PORT, SimpleTrain.class);
		doUntilServerRunning();
		JaxRsTestCase.stopServer(component);
	}

	protected void doUntilServerRunning() throws Exception
	{
		Thread.sleep(300);
		System.out.println("Server mit Tastendruck beenden . . .");
		System.in.read();
	}
}