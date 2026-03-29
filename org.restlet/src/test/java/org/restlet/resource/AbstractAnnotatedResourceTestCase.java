/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.restlet.engine.Engine;
import org.restlet.representation.ObjectRepresentation;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
public abstract class AbstractAnnotatedResourceTestCase {

    protected ClientResource clientResource;

    @BeforeEach
    protected void setUpEach() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().registerDefaultConverters();
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;

        this.clientResource = new ClientResource("http://local");
        configureClientResource(this.clientResource);
    }

    abstract void configureClientResource(final ClientResource clientResource);

    @AfterEach
    protected void tearDownEach() {
        Engine.clearThreadLocalVariables();
        clientResource.release();
        clientResource = null;
    }
}
