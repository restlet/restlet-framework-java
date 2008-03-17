package org.restlet.example.book.restlet.ch9.objects;

/**
 * USer account.
 */
public class User {
    /** Is the user an administrator? */
    private boolean administrator;

    /** Name of the user. */
    private String name;

    public String getName() {
        return name;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public void setName(String name) {
        this.name = name;
    }

}
