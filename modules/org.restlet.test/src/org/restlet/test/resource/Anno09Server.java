package org.restlet.test.resource;

public class Anno09Server {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        InternalConnectorTestCase anno9 = new AnnotatedResource09TestCase();
        anno9.initServer();
    }

}
