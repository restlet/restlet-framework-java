/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
public abstract class AbstractAnnotatedResourceWithFinderTestCase
        extends AbstractAnnotatedResourceTestCase {

    @Override
    protected void configureClientResource(final ClientResource clientResource) {
        Finder finder = new Finder();
        clientResource.setNext(finder);
        configureFinder(finder);
    }

    abstract void configureFinder(final Finder finder);
}
