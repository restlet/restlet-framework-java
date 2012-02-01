/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.resource;

import org.restlet.data.Form;
import org.restlet.resource.ServerResource;

public class MyResource9 extends ServerResource {

    @SIMethod("form:html")
    public String si1(Form form) {
        return "si-html+form";
    }

    @SIMethod("txt:html")
    public String si2(String txt) {
        return "si-html+txt";
    }

    @SIMethod("txt")
    public String si3(String txt) {
        return "si-string+" + txt;
    }

    @SIMethod("form:txt")
    public String si4(Form form) {
        return "si-string+form";
    }

    @SNIMethod("form:html")
    public String sni1(Form form) {
        return "sni-html+form";
    }

    @SNIMethod("txt:html")
    public String sni2(String txt) {
        return "sni-html+txt";
    }

    @SNIMethod("txt")
    public String sni3(String txt) {
        return "sni-string+" + txt;
    }

    @USIMethod("txt")
    public String usi1() {
        return "usi-string";
    }

    @USIMethod("html")
    public String usi2() {
        return "usi-html";
    }

    @USIMethod("form:txt")
    public String usi3(Form form) {
        return "usi-string+form";
    }

    @USIMethod("txt")
    public String usi3(String txt) {
        return "usi-string+" + txt;
    }

    @USNIMethod("form:html")
    public String usni1(Form form) {
        return "usni-html+form";
    }

    @USNIMethod("txt:html")
    public String usni2(String txt) {
        return "usni-html+txt";
    }

    @USNIMethod("txt")
    public String usni3(String txt) {
        return "usni-string+" + txt;
    }

}
