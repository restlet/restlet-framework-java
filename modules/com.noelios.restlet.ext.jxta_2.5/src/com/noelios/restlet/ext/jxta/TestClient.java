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

package com.noelios.restlet.ext.jxta;

import org.restlet.Client;
import org.restlet.data.Protocol;

/**
 * JXTA test client.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TestClient {

    public static void main(String[] args) throws Exception {
        // Create the HTTP client and listen on port 8182
        Client client = new Client(Protocol.HTTP);
        client.get("http://toto").getEntity().write(System.out);
    }

}
