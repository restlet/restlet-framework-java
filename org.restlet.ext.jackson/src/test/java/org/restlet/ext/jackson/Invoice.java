/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jackson;

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
