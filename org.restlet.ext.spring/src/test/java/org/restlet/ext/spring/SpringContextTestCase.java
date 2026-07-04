/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

/** Unit tests for {@link SpringContext}. */
class SpringContextTestCase {

    private Context restletContextWithClapDispatcher() {
        Context context = new Context();
        context.setClientDispatcher(new Client(Protocol.CLAP));
        return context;
    }

    @Test
    void getRestletContext_returnsConstructorArgument() {
        Context restletContext = new Context();
        SpringContext springContext = new SpringContext(restletContext);
        assertSame(restletContext, springContext.getRestletContext());
    }

    @Test
    void getPropertyConfigRefs_lazyInitializesToEmptyModifiableList() {
        SpringContext springContext = new SpringContext(new Context());
        assertNotNull(springContext.getPropertyConfigRefs());
        assertTrue(springContext.getPropertyConfigRefs().isEmpty());
        springContext.getPropertyConfigRefs().add("some-ref");
        assertEquals(1, springContext.getPropertyConfigRefs().size());
    }

    @Test
    void getXmlConfigRefs_lazyInitializesToEmptyModifiableList() {
        SpringContext springContext = new SpringContext(new Context());
        assertNotNull(springContext.getXmlConfigRefs());
        assertTrue(springContext.getXmlConfigRefs().isEmpty());
        springContext.getXmlConfigRefs().add("some-ref");
        assertEquals(1, springContext.getXmlConfigRefs().size());
    }

    @Test
    void refresh_withoutConfigRefs_refreshesEmptyContext() {
        SpringContext springContext = new SpringContext(new Context());
        springContext.refresh();
        assertTrue(springContext.isActive());
    }

    @Test
    void refresh_withPropertyConfigRef_loadsBeanDefinitionsFromProperties() {
        SpringContext springContext = new SpringContext(restletContextWithClapDispatcher());
        springContext
                .getPropertyConfigRefs()
                .add("clap://class/org/restlet/ext/spring/SpringContextTestCase.properties");

        springContext.refresh();

        assertNotNull(springContext.getBean("myPropertyBean"));
    }

    @Test
    void refresh_withXmlConfigRef_loadsBeanDefinitionsFromXml() {
        SpringContext springContext = new SpringContext(restletContextWithClapDispatcher());
        springContext
                .getXmlConfigRefs()
                .add("clap://class/org/restlet/ext/spring/SpringContextTestCase.xml");

        springContext.refresh();

        assertNotNull(springContext.getBean("myXmlBean"));
    }
}
