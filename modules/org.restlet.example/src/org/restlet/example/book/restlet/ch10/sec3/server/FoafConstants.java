package org.restlet.example.book.restlet.ch10.sec3.server;

import org.restlet.data.Reference;

public class FoafConstants {

    public static final String BASE = "http://xmlns.com/foaf/0.1/";

    public static final Reference KNOWS = new Reference(BASE + "knows");

    public static final Reference NAME = new Reference(BASE + "name");

}
