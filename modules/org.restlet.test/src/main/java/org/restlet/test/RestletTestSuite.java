/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
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
import org.restlet.test.data.MethodTestCase;
import org.restlet.test.data.ProductTokenTestCase;
import org.restlet.test.data.RangeTestCase;
import org.restlet.test.data.RecipientInfoTestCase;
import org.restlet.test.data.ReferenceTestCase;
import org.restlet.test.data.StatusTestCase;
import org.restlet.test.engine.EngineTestSuite;
import org.restlet.test.ext.atom.AtomTestCase;
import org.restlet.test.ext.crypto.CryptoTestSuite;
import org.restlet.test.ext.crypto.DigestVerifierTestCase;
import org.restlet.test.ext.crypto.HttpDigestTestCase;
import org.restlet.test.ext.freemarker.FreeMarkerTestCase;
import org.restlet.test.ext.guice.GuiceSelfInjectingServerResourceModuleTestCase;
import org.restlet.test.ext.gwt.GwtConverterTestCase;
import org.restlet.test.ext.html.HtmlTestSuite;
import org.restlet.test.ext.jackson.JacksonTestCase;
import org.restlet.test.ext.jaxb.JaxbBasicConverterTestCase;
import org.restlet.test.ext.jaxb.JaxbIntegrationConverterTestCase;
import org.restlet.test.ext.json.JsonTestSuite;
import org.restlet.test.ext.odata.ODataTestSuite;
import org.restlet.test.ext.spring.SpringTestSuite;
import org.restlet.test.ext.velocity.VelocityTestCase;
import org.restlet.test.ext.xml.XmlTestSuite;
import org.restlet.test.regression.RegressionTestSuite;
import org.restlet.test.representation.AppendableRepresentationTestCase;
import org.restlet.test.representation.DigesterRepresentationTestCase;
import org.restlet.test.representation.RangeRepresentationTestCase;
import org.restlet.test.routing.FilterTestCase;
import org.restlet.test.routing.RedirectTestCase;
import org.restlet.test.routing.RouteListTestCase;
import org.restlet.test.routing.ValidatorTestCase;
import org.restlet.test.security.HttpBasicTestCase;
import org.restlet.test.security.RoleTestCase;
import org.restlet.test.security.SecurityTestCase;
import org.restlet.test.service.ServiceTestSuite;
import org.restlet.test.util.TemplateTestCase;

/**
 * Suite of unit tests for the Restlet RI.
 *
 * @author Jerome Louvel
 */
@SuppressWarnings("deprecation")
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
        addTestSuite(GuiceSelfInjectingServerResourceModuleTestCase.class);
        addTestSuite(GwtConverterTestCase.class);
        addTestSuite(JacksonTestCase.class);
        addTestSuite(JaxbBasicConverterTestCase.class);
        addTestSuite(JaxbIntegrationConverterTestCase.class);
        addTestSuite(LanguageTestCase.class);
        addTestSuite(MediaTypeTestCase.class);
        addTestSuite(ProductTokenTestCase.class);
        addTestSuite(ReferenceTestCase.class);
        addTestSuite(RestartTestCase.class);
        addTestSuite(RiapTestCase.class);
        addTestSuite(RouteListTestCase.class);
        addTestSuite(DigestVerifierTestCase.class);
        addTestSuite(RecipientInfoTestCase.class);
        addTestSuite(RoleTestCase.class);
        addTestSuite(StatusTestCase.class);
        addTestSuite(TemplateTestCase.class);
        addTestSuite(ValidatorTestCase.class);
        addTestSuite(VelocityTestCase.class);
        addTest(RegressionTestSuite.suite());
        addTest(CryptoTestSuite.suite());
        addTest(HtmlTestSuite.suite());
        addTestSuite(MethodTestCase.class);

        addTest(ODataTestSuite.suite());
        addTest(XmlTestSuite.suite());
        addTest(JsonTestSuite.suite());

        // [ifdef jse]
        addTest(org.restlet.test.resource.ResourceTestSuite.suite());
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

        addTest(SpringTestSuite.suite());
        addTest(EngineTestSuite.suite());
        // [enddef]
    }

}
