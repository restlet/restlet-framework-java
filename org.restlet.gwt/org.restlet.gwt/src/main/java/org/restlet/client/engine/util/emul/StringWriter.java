/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.util.emul;

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
