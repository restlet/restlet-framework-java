/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.net.Socket;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;

/**
 * Base client helper based on NIO blocking sockets. Here is the list of
 * parameters that are supported. They should be set in the Server's context
 * before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <td>*</td>
 * <td>*</td>
 * <td>*</td>
 * <td>*.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class BaseClientHelper extends BaseHelper<Client> {

    public BaseClientHelper(Client connector) {
        super(connector);
    }

    @Override
    protected Connection<?> createConnection(BaseHelper<Client> helper,
            Socket socket) throws IOException {
        return null;
    }

    @Override
    public void handle(Request request) {
    }

    @Override
    public void handle(Response response) {
    }

}
