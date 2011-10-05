package org.restlet.example.book.restlet.ch10.sec3;

import org.restlet.data.Reference;

public class FoafConstants {

    public static final String BASE = "http://xmlns.com/foaf/0.1/";

    public static final Reference KNOWS = new Reference(BASE + "knows");

    public static final Reference NAME = new Reference(BASE + "name");

    public static final Reference FIRST_NAME = new Reference(BASE + "firstName");

    public static final Reference LAST_NAME = new Reference(BASE + "lastName");

    public static final Reference MBOX = new Reference(BASE + "mbox");

    public static final Reference NICK = new Reference(BASE + "nick");

}
