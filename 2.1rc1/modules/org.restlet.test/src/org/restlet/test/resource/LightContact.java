package org.restlet.test.resource;

public class LightContact {

    private String email1;

    private String firstName;

    private String lastName;

    public LightContact(String email, String firstName, String lastName) {
        super();
        this.email1 = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail1() {
        return email1;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setEmail1(String email) {
        this.email1 = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
