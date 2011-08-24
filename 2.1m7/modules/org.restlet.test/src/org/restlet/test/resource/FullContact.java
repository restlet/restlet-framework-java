package org.restlet.test.resource;

import java.util.Date;

public class FullContact extends Contact {

    private String address1;

    private String address2;

    private String address3;

    public FullContact(String email, String firstName, String lastName,
            Date birthDate, String email2, String address1, String address2,
            String address3, String fax, String phone) {
        super(email, firstName, lastName, birthDate, email2);
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.fax = fax;
        this.phone = phone;
    }

    private String fax;

    private String phone;

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    public String getFax() {
        return fax;
    }

    public String getPhone() {
        return phone;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
