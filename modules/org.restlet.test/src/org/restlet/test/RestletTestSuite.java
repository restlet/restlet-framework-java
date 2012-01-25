/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.restlet.test.component.ComponentXmlConfigTestCase;
import org.restlet.test.component.ComponentXmlTestCase;
import org.restlet.test.connector.FileClientTestCase;
import org.restlet.test.connector.RestartTestCase;
import org.restlet.test.connector.RiapTestCase;
import org.restlet.test.data.AuthenticationInfoTestCase;
import org.restlet.test.data.ClientInfoTestCase;
import org.restlet.test.data.CookieTestCase;
import org.restlet.test.data.FileReferenceTestCase;
import org.restlet.test.data.FormTestCase;
import org.restlet.test.data.LanguageTestCase;
import org.restlet.test.data.MediaTypeTestCase;
import org.restlet.test.data.ProductTokenTestCase;
import org.restlet.test.data.RangeTestCase;
import org.restlet.test.data.RecipientInfoTestCase;
import org.restlet.test.data.ReferenceTestCase;
import org.restlet.test.data.StatusTestCase;
import org.restlet.test.engine.EngineTestSuite;
import org.restlet.test.ext.atom.AtomTestCase;
import org.restlet.test.ext.crypto.CryptoTestSuite;
import org.restlet.test.ext.emf.EmfTestSuite;
import org.restlet.test.ext.freemarker.FreeMarkerTestCase;
import org.restlet.test.ext.gwt.GwtConverterTestCase;
import org.restlet.test.ext.html.HtmlTestSuite;
import org.restlet.test.ext.jaxb.JaxbBasicConverterTestCase;
import org.restlet.test.ext.jaxb.JaxbIntegrationConverterTestCase;
import org.restlet.test.ext.odata.ODataTestSuite;
import org.restlet.test.ext.sip.SipTests;
import org.restlet.test.ext.spring.SpringTestSuite;
import org.restlet.test.ext.velocity.VelocityTestCase;
import org.restlet.test.ext.wadl.WadlTestSuite;
import org.restlet.test.ext.xml.ResolvingTransformerTestCase;
import org.restlet.test.ext.xml.RestletXmlTestCase;
import org.restlet.test.ext.xml.TransformerTestCase;
import org.restlet.test.jaxrs.JaxRsTestSuite;
import org.restlet.test.regression.RegressionTestSuite;
import org.restlet.test.representation.AppendableRepresentationTestCase;
import org.restlet.test.representation.DigesterRepresentationTestCase;
import org.restlet.test.representation.RangeRepresentationTestCase;
import org.restlet.test.resource.ResourceTestSuite;
import org.restlet.test.routing.FilterTestCase;
import org.restlet.test.routing.RedirectTestCase;
import org.restlet.test.routing.RouteListTestCase;
import org.restlet.test.routing.ValidatorTestCase;
import org.restlet.test.security.DigestVerifierTestCase;
import org.restlet.test.security.HttpBasicTestCase;
import org.restlet.test.security.HttpDigestTestCase;
import org.restlet.test.security.RoleTestCase;
import org.restlet.test.security.SecurityTestCase;
import org.restlet.test.service.ServiceTestSuite;
import org.restlet.test.util.TemplateTestCase;

/**
 * Suite of unit tests for the Restlet RI.
 * 
 * @author Jerome Louvel
 */
public class RestletTestSuite extends TestSuite {

    /**
     * JUnit constructor.
     * 
     * @return The unit test.
     */
    public static Test suite() {
        return new RestletTestSuite();
    }

    /** Constructor. */
    public RestletTestSuite() {
        addTest(ResourceTestSuite.suite());
        addTest(ServiceTestSuite.suite());
        addTestSuite(AppendableRepresentationTestCase.class);
        addTestSuite(AtomTestCase.class);
        addTestSuite(AuthenticationInfoTestCase.class);
        addTestSuite(CallTestCase.class);
        addTestSuite(ComponentXmlConfigTestCase.class);
        addTestSuite(CookieTestCase.class);
        addTestSuite(ClientInfoTestCase.class);
        addTestSuite(FileClientTestCase.class);
        addTestSuite(FileReferenceTestCase.class);
        addTestSuite(FilterTestCase.class);
        addTestSuite(FormTestCase.class);
        addTestSuite(FreeMarkerTestCase.class);
        addTestSuite(GwtConverterTestCase.class);
        addTestSuite(JaxbBasicConverterTestCase.class);
        addTestSuite(JaxbIntegrationConverterTestCase.class);
        addTestSuite(LanguageTestCase.class);
        addTestSuite(MediaTypeTestCase.class);
        addTestSuite(ProductTokenTestCase.class);
        addTestSuite(ReferenceTestCase.class);
        addTestSuite(ResolvingTransformerTestCase.class);
        addTestSuite(RestartTestCase.class);
        addTestSuite(RestletXmlTestCase.class);
        addTestSuite(RiapTestCase.class);
        addTestSuite(RouteListTestCase.class);
        addTestSuite(DigestVerifierTestCase.class);
        addTestSuite(RecipientInfoTestCase.class);
        addTestSuite(RoleTestCase.class);
        addTestSuite(StatusTestCase.class);
        addTestSuite(TemplateTestCase.class);
        addTestSuite(TransformerTestCase.class);
        addTestSuite(ValidatorTestCase.class);
        addTestSuite(VelocityTestCase.class);
        addTest(RegressionTestSuite.suite());
        addTest(CryptoTestSuite.suite());
        addTest(EmfTestSuite.suite());
        addTest(HtmlTestSuite.suite());
        addTest(ODataTestSuite.suite());
        addTest(WadlTestSuite.suite());

        // Tests based on extension only supported by the JEE edition.
        // [ifdef jee]
        addTestSuite(org.restlet.test.ext.xdb.ChunkedInputStreamTestCase.class);
        addTestSuite(org.restlet.test.ext.xdb.ChunkedOutputStreamTestCase.class);
        addTestSuite(org.restlet.test.ext.xdb.InputEntityStreamTestCase.class);
        // [enddef]

        // TODO Fix Zip client test case
        // addTestSuite(ZipClientTestCase.class);

        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        addTestSuite(ComponentXmlTestCase.class);
        addTestSuite(DigesterRepresentationTestCase.class);
        addTestSuite(HeaderTestCase.class);
        addTestSuite(HttpBasicTestCase.class);
        addTestSuite(HttpDigestTestCase.class);
        addTestSuite(RangeTestCase.class);
        addTestSuite(RangeRepresentationTestCase.class);
        addTestSuite(RedirectTestCase.class);
        addTestSuite(SecurityTestCase.class);
        addTestSuite(TemplateFilterTestCase.class);

        addTest(JaxRsTestSuite.suite());
        addTest(SipTests.suite());
        addTest(SpringTestSuite.suite());
        addTest(EngineTestSuite.suite());
        // [enddef]
    }

}
