/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Filter output stream that sends a copy of all data on the standard console.
 * 
 * @author Jerome Louvel
 */
public class TraceOutputStream extends FilterOutputStream {

    /**
     * Constructor.
     * 
     * @param out
     *            The output stream to wrap.
     */
    public TraceOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void flush() throws IOException {
        super.flush();

        synchronized (System.out) {
            System.out.flush();
        }
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);

        synchronized (System.out) {
            System.out.write(b);
        }
    }

}
