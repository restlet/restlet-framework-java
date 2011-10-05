package org.restlet.test.resource;

import java.util.Date;

public class Contact extends LightContact {

    private Date birthDate;

    private String email2;

    public Contact(String email, String firstName, String lastName,
            Date birthDate, String email2) {
        super(email, firstName, lastName);
        this.birthDate = birthDate;
        email = email2;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getEmail2() {
        return email2;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail2(String email) {
        this.email2 = email;
    }

}
