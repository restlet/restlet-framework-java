package org.restlet.example.book.restlet.ch9.objects;

/**
 * Contact of a mail box owner.
 * 
 */
public class Contact {
    /** Mail address of the contact. */
    private String mailAddress;

    /** Name of the contact. */
    private String name;

    /** Nickname of the contact. */
    private String nickname;

    public String getMailAddress() {
        return mailAddress;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
