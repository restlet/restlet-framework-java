/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import java.util.Date;

import org.restlet.resource.Status;

@Status(value = 400, serialize = false)
public class MyException01 extends Throwable {

    private static final long serialVersionUID = 1L;

    private Date date;

    public MyException01() {
    }

    public MyException01(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
