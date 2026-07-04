/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.xml.transform.Source;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

/** Unit tests for {@link ContextResolver}. */
class ContextResolverTestCase {

    private Context clapContext() {
        Context context = new Context();
        context.setClientDispatcher(new Client(Protocol.CLAP));
        return context;
    }

    @Test
    void resolve_withNullContext_returnsNull() {
        ContextResolver resolver = new ContextResolver(null);
        assertNull(resolver.resolve("clap://class/org/restlet/ext/xml/xslt/one/1st.xml", null));
    }

    @Test
    void resolve_withAbsoluteHrefAndNoBase_returnsSource() {
        ContextResolver resolver = new ContextResolver(clapContext());

        Source result = resolver.resolve("clap://class/org/restlet/ext/xml/xslt/one/1st.xml", null);

        assertNotNull(result);
    }

    @Test
    void resolve_withRelativeHrefAndBase_returnsSource() {
        ContextResolver resolver = new ContextResolver(clapContext());

        Source result =
                resolver.resolve("1st.xml", "clap://class/org/restlet/ext/xml/xslt/one/base.xsl");

        assertNotNull(result);
    }

    @Test
    void resolve_withUnresolvableHref_returnsNull() {
        ContextResolver resolver = new ContextResolver(clapContext());

        Source result = resolver.resolve("clap://class/does/not/exist.xml", null);

        assertNull(result);
    }
}
