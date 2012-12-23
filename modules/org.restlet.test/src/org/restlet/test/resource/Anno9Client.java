package org.restlet.test.resource;

public class Anno9Client {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        AnnotatedResource9TestCase anno9 = new AnnotatedResource9TestCase();
        anno9.initClient();
        anno9.testSI();

    }

}
