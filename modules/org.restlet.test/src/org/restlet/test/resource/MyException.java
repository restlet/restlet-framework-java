package org.restlet.test.resource;

import java.util.Date;

import org.restlet.resource.Status;

@Status("500")
public class MyException extends Throwable {

    private static final long serialVersionUID = 1L;

    private Date date;

    public MyException(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
