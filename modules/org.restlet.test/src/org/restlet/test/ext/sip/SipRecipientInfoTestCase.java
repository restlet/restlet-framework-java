/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.sip;

import org.junit.Test;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.internal.SipRecipientInfoReader;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the Via header.
 * 
 * @author Thierry Boileau
 */
@Deprecated
public class SipRecipientInfoTestCase extends RestletTestCase {

    @Test
    public void testParsing() throws Exception {
        String str = "SIP/2.0/UDP 192.0.2.1:5060 ;received=192.0.2.207;branch=z9hG4bK77asjd";
        SipRecipientInfoReader r = new SipRecipientInfoReader(str);
        SipRecipientInfo s = r.readValue();

        assertEquals(Protocol.SIP, s.getProtocol());
        assertEquals("UDP", s.getTransport());
        assertEquals("192.0.2.1:5060", s.getName());

        assertEquals(2, s.getParameters().size());

        Parameter parameter = s.getParameters().get(0);

        assertEquals("received", parameter.getName());
        assertEquals("192.0.2.207", parameter.getValue());
        parameter = s.getParameters().get(1);
        assertEquals("branch", parameter.getName());
        assertEquals("z9hG4bK77asjd", parameter.getValue());
        assertNull(s.getComment());

        str = "SIP/2.0/UDP 192.0.2.1:5060 ;received=192.0.2.207;branch=z9hG4bK77asjd (this is a comment)";
        r = new SipRecipientInfoReader(str);
        s = r.readValue();

        assertEquals(Protocol.SIP, s.getProtocol());
        assertEquals("UDP", s.getTransport());
        assertEquals("192.0.2.1:5060", s.getName());

        assertEquals(2, s.getParameters().size());
        parameter = s.getParameters().get(0);
        assertEquals("received", parameter.getName());
        assertEquals("192.0.2.207", parameter.getValue());
        parameter = s.getParameters().get(1);
        assertEquals("branch", parameter.getName());
        assertEquals("z9hG4bK77asjd", parameter.getValue());
        assertEquals("this is a comment", s.getComment());

        str = "SIP/2.0/TCP 127.0.0.1:5061;branch=z9hG4bK-6503-1-0";
        r = new SipRecipientInfoReader(str);
        s = r.readValue();

        assertEquals(Protocol.SIP, s.getProtocol());
        assertEquals("TCP", s.getTransport());
        assertEquals("127.0.0.1:5061", s.getName());

        assertEquals(1, s.getParameters().size());
        parameter = s.getParameters().get(0);
        assertEquals("branch", parameter.getName());
        assertEquals("z9hG4bK-6503-1-0", parameter.getValue());

        str = "SIP/2.0/TCP [fe80::223:dfff:fe7f:7b1a%en0]:5061;branch=z9hG4bK-409-1-0";
        r = new SipRecipientInfoReader(str);
        s = r.readValue();

        assertEquals(Protocol.SIP, s.getProtocol());
        assertEquals("TCP", s.getTransport());
        assertEquals("[fe80::223:dfff:fe7f:7b1a%en0]:5061", s.getName());
        assertEquals(1, s.getParameters().size());
        parameter = s.getParameters().get(0);
        assertEquals("branch", parameter.getName());
        assertEquals("z9hG4bK-409-1-0", parameter.getValue());
        assertNull(s.getComment());
    }

}
