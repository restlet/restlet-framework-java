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

import com.noelios.restlet.ext.jxta.Connection;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class ConnectionManager {

    private List<Connection> connections = new ArrayList<Connection>();

    public void addConnections(Connection... connections) {
        for (Connection connection : connections) {
            this.connections.add(connection);
        }
    }

    public void removeConnections(Connection... connections) {
        for (Connection connection : connections) {
            this.connections.remove(connection);
        }
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }
}