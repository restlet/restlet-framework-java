/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

@Status(value = 400)
public class MyException02 extends Throwable {

    private static final long serialVersionUID = 1L;

    private String customProperty;

    public MyException02(String customProperty) {
        this.customProperty = customProperty;
    }

    public MyException02(String customProperty, Throwable cause) {
        super(cause);
        this.customProperty = customProperty;
    }

    public String getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(String customProperty) {
        this.customProperty = customProperty;
    }
}
