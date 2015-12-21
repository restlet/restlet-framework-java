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

package com.google.gwt.emul.java.io;

import java.io.IOException;

/**
 * Emulation of the {@link java.io.StringWriter} class for the GWT edition.
 * 
 * @author Jerome Louvel
 */
public abstract class StringWriter implements Appendable {

    private StringBuilder sb;

    public StringWriter() {
        super();
        this.sb = new StringBuilder();
    }

    public Appendable append(char c) {
        return sb.append(c);
    }

    public Appendable append(CharSequence csq, int start, int end)
            throws IOException {
        return sb.append(csq, start, end);
    }

    public Appendable append(CharSequence csq) {
        return sb.append(csq);
    }

    public void close() throws IOException {
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
