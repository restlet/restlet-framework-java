/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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
