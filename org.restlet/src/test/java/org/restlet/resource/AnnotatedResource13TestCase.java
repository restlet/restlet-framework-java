/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.Serializable;
import java.util.Date;
import org.junit.jupiter.api.Test;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource13TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    private MyResource13 myResource;

    @Override
    protected void configureClientResource(ClientResource clientResource) {
        super.configureClientResource(clientResource);
        this.myResource = clientResource.wrap(MyResource13.class);
    }

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource13.class);
    }

    @Override
    protected void tearDownEach() {
        myResource = null;
    }

    @Test
    void testQuery() {
        Contact contact = myResource.retrieve();
        assertNotNull(contact);

        LightContact lightContact = myResource.retrieveLight();
        assertNotEquals(Contact.class, lightContact.getClass());
        assertNotNull(lightContact);

        FullContact fullContact = myResource.retrieveFull();
        assertNotNull(fullContact);
    }

    /**
     * Sample server resource for modifier testing.
     *
     * @author Jerome Louvel
     */
    public static class MyServerResource13 extends ServerResource implements MyResource13 {

        public LightContact retrieveLight() {
            return new LightContact("test@domain.com", "Scott", "Tiger");
        }

        public Contact retrieve() {
            return new Contact("test@domain.com", "Scott", "Tiger", new Date(), "test@perso.fr");
        }

        public FullContact retrieveFull() {
            return new FullContact(
                    "test@domain.com",
                    "Scott",
                    "Tiger",
                    new Date(),
                    "test@perso.fr",
                    "1 Main Street",
                    "Restlet city, 0102",
                    "RESTland",
                    "+123.456",
                    "+789012");
        }
    }

    public static class Contact extends LightContact {

        private Date birthDate;

        private String email2;

        public Contact(
                String email, String firstName, String lastName, Date birthDate, String email2) {
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

    public static class FullContact extends Contact {

        private String address1;

        private String address2;

        private String address3;

        public FullContact(
                String email,
                String firstName,
                String lastName,
                Date birthDate,
                String email2,
                String address1,
                String address2,
                String address3,
                String fax,
                String phone) {
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

    public static class LightContact implements Serializable {

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

    public interface MyResource13 {

        @Get("?shallow")
        LightContact retrieveLight();

        @Get
        Contact retrieve();

        @Get("?deep")
        FullContact retrieveFull();
    }
}
