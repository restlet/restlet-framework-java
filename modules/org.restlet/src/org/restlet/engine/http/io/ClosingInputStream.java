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

package org.restlet.engine.http.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream based on a source stream that must only be totally read before
 * closing.
 * 
 * @author Jerome Louvel
 */
public class ClosingInputStream extends InputEntityStream {

    /**
     * Constructor.
     * 
     * @param notifiable
     *            The notifiable connection.
     * @param inboundStream
     *            The inbound stream.
     */
    public ClosingInputStream(Notifiable notifiable, InputStream inboundStream) {
        super(notifiable, inboundStream);
    }

    @Override
    public int read() throws IOException {
        int r = -1;
        try {
            r = getInboundStream().read();
            if (r == -1) {
                onEndReached();
            }
        } catch (Exception e) {
            onError();
        }

        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = getInboundStream().read(b, off, len);
        if (result == -1) {
            onEndReached();
        }
        return result;
    }
}
