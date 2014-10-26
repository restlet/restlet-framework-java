package org.restlet.test.resource;

import org.restlet.resource.Status;

import java.util.Date;

@Status(value = 400, serializeProperties = true)
public class MyException02 extends Throwable {

    private static final long serialVersionUID = 1L;

    private String customProperty;

    public MyException02(String customProperty) {
        this.customProperty = customProperty ;
    }

    public String getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(String customProperty) {
        this.customProperty = customProperty;
    }
}
