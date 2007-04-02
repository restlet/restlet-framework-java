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

    public void addConnections(Connection ... connections) {
        for (Connection connection : connections) {
            this.connections.add(connection);
        }
    }

    public void removeConnections(Connection ... connections) {
        for (Connection connection : connections) {
            this.connections.remove(connection);
        }
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }
}