package org.restlet.test.ext.jackson;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Customer {

    private String firstName;
    
    private String lastName;
    
    private List<Invoice> invoices = new CopyOnWriteArrayList<Invoice>();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }
    
}
