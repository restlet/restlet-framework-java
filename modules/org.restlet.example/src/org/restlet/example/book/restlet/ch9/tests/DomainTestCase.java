package org.restlet.example.book.restlet.ch9.tests;

import junit.framework.TestCase;

public class DomainTestCase extends TestCase {

    /** Domain objects. */
    private DomainObjects domainObjects;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainObjects = new DomainObjects();
    }

}
