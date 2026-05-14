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
 * Sample server resource.
 *
 * @author Jerome Louvel
 */
public class MyServerResource01 extends ServerResource implements MyResource01 {

    private volatile MyBean myBean = new MyBean("myName", "myDescription");

    public boolean accept(MyBean bean) {
        return bean.equals(myBean);
    }

    public String describe() {
        return "MyDescription";
    }

    public String remove() {
        myBean = null;
        return "Done";
    }

    public MyBean represent() {
        return myBean;
    }

    public String store(MyBean bean) {
        myBean = bean;
        return "Done";
    }
}
