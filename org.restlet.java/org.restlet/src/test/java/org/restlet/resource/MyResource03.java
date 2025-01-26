/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

/**
 * Sample server resource that sets the "existing" flag to false.
 * 
 * @author Jerome Louvel
 */
public class MyResource03 extends ServerResource implements MyResource01 {

    private volatile MyBean myBean = new MyBean("myName", "myDescription");

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setExisting(false);
    }

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
