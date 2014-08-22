package org.restlet.test.ext.jackson;

import java.util.Date;

public class Invoice {

    private Date date;

    private Integer amount;

    private boolean paid;

    public Integer getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

}
