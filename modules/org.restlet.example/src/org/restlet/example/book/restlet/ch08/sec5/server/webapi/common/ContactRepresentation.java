package org.restlet.example.book.restlet.ch08.sec5.server.webapi.common;

public class ContactRepresentation {

    private String firstName;

    private String lastName;

    private String login;

    private String nickName;

    private String senderName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLogin() {
        return login;
    }

    public String getNickName() {
        return nickName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
