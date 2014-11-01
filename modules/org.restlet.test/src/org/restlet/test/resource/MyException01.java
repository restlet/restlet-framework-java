package org.restlet.test.resource;

import java.util.Date;

import org.restlet.resource.Status;

@Status(value = 400, serialize = false)
public class MyException01 extends Throwable {

    private static final long serialVersionUID = 1L;

    private Date date;

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
