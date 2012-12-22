package org.restlet.test.resource;

public class Anno9Server {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        InternalConnectorTestCase anno9 = new AnnotatedResource9TestCase();
        anno9.initServer();
    }

}
