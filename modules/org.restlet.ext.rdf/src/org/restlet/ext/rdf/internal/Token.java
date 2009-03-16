/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.rdf.internal;

import java.io.IOException;

import org.restlet.data.Reference;

class Token extends LexicalUnit {

    public Token(RdfN3ContentHandler contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        this.parse();
    }

    public Token(String value) {
        super(value);
    }

    @Override
    public void parse() throws IOException {
        int c;
        do {
            c = getContentHandler().step();
        } while (c != RdfN3ContentHandler.EOF
                && !RdfN3ContentHandler.isDelimiter(c));
        setValue(getContentHandler().getCurrentToken());
    }

    @Override
    public Object resolve() {
        Object result = null;
        setResolved(true);
        int index = getValue().indexOf(":");
        if (index != -1) {
            String prefix = getValue().substring(0, index+1);

            String base = null;
            if (getContext() != null) {
                base = getContext().getPrefixes().get(prefix);
            }

            if (base != null) {
                result = new Reference(base + getValue().substring(index));
            } else {
                // TODO Error, this prefix has not been declared!
                result = null;
            }
        } else {
            result = new Reference(getValue());
        }

        setResolved(!(result == null));
        return result;
    }
}