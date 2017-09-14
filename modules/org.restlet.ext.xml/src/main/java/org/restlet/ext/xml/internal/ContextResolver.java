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
