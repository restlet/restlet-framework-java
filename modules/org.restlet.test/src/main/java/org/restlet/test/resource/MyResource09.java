/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.resource;

import org.restlet.data.Form;
import org.restlet.resource.ServerResource;

public class MyResource09 extends ServerResource {

    @SIMethod("form:html")
    public String si1(Form form) {
        return "si-form:html";
    }

    @SIMethod("txt:html")
    public String si2(String txt) {
        return "si-txt:html";
    }

    @SIMethod("txt")
    public String si3(String txt) {
        return "si-txt|" + txt;
    }

    @SIMethod("form:txt")
    public String si4(Form form) {
        return "si-form:txt";
    }

    @SIMethod("html")
    public String si5() {
        return "si-:html";
    }

    @SIMethod("xml")
    public String si6() {
        return "si-xml:xml";
    }

    @SIMethod(":xml")
    public String si7() {
        return "si-:xml";
    }

    @SNIMethod("form:html")
    public String sni1(Form form) {
        return "sni-form:html";
    }

    @SNIMethod("txt:html")
    public String sni2(String txt) {
        return "sni-txt:html";
    }

    @SNIMethod("txt")
    public String sni3(String txt) {
        return "sni-txt|" + txt;
    }

    @SNIMethod("form:txt")
    public String sni4(Form form) {
        return "sni-form:txt";
    }

    @SNIMethod("html")
    public String sni5() {
        return "sni-:html";
    }

    @SNIMethod("xml")
    public String sni6() {
        return "sni-xml:xml";
    }

    @SNIMethod(":xml")
    public String sni7() {
        return "sni-:xml";
    }

    @USIMethod("form:html")
    public String usi1(Form form) {
        return "usi-form:html";
    }

    @USIMethod("txt:html")
    public String usi2(String txt) {
        return "usi-txt:html";
    }

    @USIMethod("txt")
    public String usi3(String txt) {
        return "usi-txt|" + txt;
    }

    @USIMethod("form:txt")
    public String usi4(Form form) {
        return "usi-form:txt";
    }

    @USIMethod("html")
    public String usi5() {
        return "usi-:html";
    }

    @USIMethod("xml")
    public String usi6() {
        return "usi-xml:xml";
    }

    @USIMethod(":xml")
    public String usi7() {
        return "usi-:xml";
    }

    @USNIMethod("form:html")
    public String usni1(Form form) {
        return "usni-form:html";
    }

    @USNIMethod("txt:html")
    public String usni2(String txt) {
        return "usni-txt:html";
    }

    @USNIMethod("txt")
    public String usni3(String txt) {
        return "usni-txt|" + txt;
    }

    @USNIMethod("form:txt")
    public String usni4(Form form) {
        return "usni-form:txt";
    }

    @USNIMethod("html")
    public String usni5() {
        return "usni-:html";
    }

    @USNIMethod("xml")
    public String usni6() {
        return "usni-xml:xml";
    }

    @USNIMethod(":xml")
    public String usni7() {
        return "usni-:xml";
    }
}
