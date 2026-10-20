/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jackson;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class Invoice implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

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
