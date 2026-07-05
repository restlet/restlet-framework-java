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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.engine.application.TunnelFilter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.routing.Filter;

/** Unit tests for {@link TunnelService}. */
class TunnelServiceTestCase {

    @Test
    void twoArgConstructor_enablesQueryTunnelOnly() {
        TunnelService service = new TunnelService(true, true);

        assertTrue(service.isEnabled());
        assertTrue(service.isMethodTunnel());
        assertTrue(service.isPreferencesTunnel());
        assertTrue(service.isQueryTunnel());
        assertFalse(service.isExtensionsTunnel());
        assertFalse(service.isUserAgentTunnel());
        assertTrue(service.isHeadersTunnel());
    }

    @Test
    void threeArgConstructor_setsEnabledFlag() {
        TunnelService service = new TunnelService(false, true, false);

        assertFalse(service.isEnabled());
        assertTrue(service.isMethodTunnel());
        assertFalse(service.isPreferencesTunnel());
    }

    @Test
    void fiveArgConstructor_setsQueryAndExtensionsTunnel() {
        TunnelService service = new TunnelService(true, true, true, false, true);

        assertFalse(service.isQueryTunnel());
        assertTrue(service.isExtensionsTunnel());
        assertFalse(service.isUserAgentTunnel());
    }

    @Test
    void sixArgConstructor_setsUserAgentTunnel() {
        TunnelService service = new TunnelService(true, true, true, true, false, true);

        assertTrue(service.isUserAgentTunnel());
        assertTrue(service.isHeadersTunnel());
    }

    @Test
    void sevenArgConstructor_setsHeadersTunnel() {
        TunnelService service = new TunnelService(true, true, true, true, false, true, false);

        assertFalse(service.isHeadersTunnel());
    }

    @Test
    void defaultParameterNames_areSet() {
        TunnelService service = new TunnelService(true, true);

        assertEquals("charset", service.getCharacterSetParameter());
        assertEquals("encoding", service.getEncodingParameter());
        assertEquals("language", service.getLanguageParameter());
        assertEquals("media", service.getMediaTypeParameter());
        assertEquals("method", service.getMethodParameter());
        assertEquals(HeaderConstants.HEADER_X_HTTP_METHOD_OVERRIDE, service.getMethodHeader());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        TunnelService service = new TunnelService(true, true);

        service.setCharacterSetParameter("cs");
        assertEquals("cs", service.getCharacterSetParameter());

        service.setEncodingParameter("enc");
        assertEquals("enc", service.getEncodingParameter());

        service.setLanguageParameter("lang");
        assertEquals("lang", service.getLanguageParameter());

        service.setMediaTypeParameter("mt");
        assertEquals("mt", service.getMediaTypeParameter());

        service.setMethodParameter("mth");
        assertEquals("mth", service.getMethodParameter());

        service.setMethodHeader("X-Method");
        assertEquals("X-Method", service.getMethodHeader());

        service.setMethodTunnel(false);
        assertFalse(service.isMethodTunnel());

        service.setPreferencesTunnel(false);
        assertFalse(service.isPreferencesTunnel());

        service.setQueryTunnel(false);
        assertFalse(service.isQueryTunnel());

        service.setExtensionsTunnel(true);
        assertTrue(service.isExtensionsTunnel());

        service.setUserAgentTunnel(true);
        assertTrue(service.isUserAgentTunnel());

        service.setHeadersTunnel(false);
        assertFalse(service.isHeadersTunnel());
    }

    @Test
    void allowClient_alwaysReturnsTrue() {
        TunnelService service = new TunnelService(true, true);

        assertTrue(service.allowClient(new ClientInfo()));
        assertTrue(service.allowClient(null));
    }

    @Test
    void createInboundFilter_returnsTunnelFilter() {
        TunnelService service = new TunnelService(true, true);

        Filter filter = service.createInboundFilter(new Context());

        assertNotNull(filter);
        assertTrue(filter instanceof TunnelFilter);
    }
}
