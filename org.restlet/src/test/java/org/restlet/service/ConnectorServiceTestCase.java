/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Protocol;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link ConnectorService}. */
class ConnectorServiceTestCase {

    @Test
    void defaultConstructor_hasEmptyProtocolLists() {
        ConnectorService service = new ConnectorService();

        assertTrue(service.getClientProtocols().isEmpty());
        assertTrue(service.getServerProtocols().isEmpty());
    }

    @Test
    void setClientProtocols_replacesListContents() {
        ConnectorService service = new ConnectorService();
        service.getClientProtocols().add(Protocol.FTP);

        service.setClientProtocols(Arrays.asList(Protocol.HTTP, Protocol.HTTPS));

        assertEquals(2, service.getClientProtocols().size());
        assertTrue(service.getClientProtocols().contains(Protocol.HTTP));
        assertTrue(service.getClientProtocols().contains(Protocol.HTTPS));
    }

    @Test
    void setClientProtocols_withNull_clearsList() {
        ConnectorService service = new ConnectorService();
        service.getClientProtocols().add(Protocol.HTTP);

        service.setClientProtocols(null);

        assertTrue(service.getClientProtocols().isEmpty());
    }

    @Test
    void setServerProtocols_replacesListContents() {
        ConnectorService service = new ConnectorService();
        service.getServerProtocols().add(Protocol.FTP);

        service.setServerProtocols(Arrays.asList(Protocol.HTTP));

        assertEquals(1, service.getServerProtocols().size());
        assertTrue(service.getServerProtocols().contains(Protocol.HTTP));
    }

    @Test
    void setServerProtocols_withNull_clearsList() {
        ConnectorService service = new ConnectorService();
        service.getServerProtocols().add(Protocol.HTTP);

        service.setServerProtocols(null);

        assertTrue(service.getServerProtocols().isEmpty());
    }

    @Test
    void setServerProtocols_withSameListReference_isNoOp() {
        ConnectorService service = new ConnectorService();
        service.getServerProtocols().add(Protocol.HTTP);
        List<Protocol> same = service.getServerProtocols();

        service.setServerProtocols(same);

        assertEquals(1, service.getServerProtocols().size());
    }

    @Test
    void beforeSendAndAfterSend_doNotThrow() {
        ConnectorService service = new ConnectorService();

        service.beforeSend(new StringRepresentation("test"));
        service.afterSend(new StringRepresentation("test"));
        service.beforeSend(null);
        service.afterSend(null);
    }

    @Test
    void getClientProtocols_returnsModifiableList() {
        ConnectorService service = new ConnectorService();

        service.getClientProtocols().addAll(new ArrayList<>(List.of(Protocol.HTTP)));

        assertEquals(1, service.getClientProtocols().size());
    }
}
