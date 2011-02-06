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

package org.restlet.ext.xml.internal;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;

/**
 * URI resolver based on a Restlet Context instance.
 * 
 * @author Jerome Louvel
 */
public class ContextResolver implements URIResolver {
    /** The Restlet context. */
    private final Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The Restlet context.
     */
    public ContextResolver(Context context) {
        this.context = context;
    }

    /**
     * Resolves a target reference into a Source document.
     * 
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
     *      java.lang.String)
     */
    public Source resolve(String href, String base) throws TransformerException {
        Source result = null;

        if (this.context != null) {
            Reference targetRef = null;

            if ((base != null) && !base.equals("")) {
                // Potentially a relative reference
                Reference baseRef = new Reference(base);
                targetRef = new Reference(baseRef, href);
            } else {
                // No base, assume "href" is an absolute URI
                targetRef = new Reference(href);
            }

            String targetUri = targetRef.getTargetRef().toString();
            Response response = this.context.getClientDispatcher().handle(
                    new Request(Method.GET, targetUri));

            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                try {
                    result = new StreamSource(response.getEntity().getStream());
                    result.setSystemId(targetUri);

                } catch (IOException e) {
                    this.context.getLogger().log(Level.WARNING,
                            "I/O error while getting the response stream", e);
                }
            }
        }

        return result;
    }
}