/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.osgi;

import org.restlet.Context;
import org.restlet.Restlet;

/**
 * This is the base class for the other providers. It handles the filtering if a
 * filter provider has been bound. Users should typically not extend this class,
 * but instead extend an appropriate base provider.
 * 
 * @author Bryan Hunt
 * 
 */
public abstract class BaseRestletProvider implements RestletProvider {
    private FilterProvider filterProvider;

    /**
     * Called by OSGi DS to inject the filter provider service
     * 
     * @param filterProvider
     *            the filter provider service
     */
    public void bindFilterProvider(FilterProvider filterProvider) {
        this.filterProvider = filterProvider;
    }

    /**
     * Called by getInboundRoot() to determine the filtered restlet that is next
     * in the chain.
     * 
     * @return the restlet to be filtered
     */
    protected abstract Restlet getFilteredRestlet();

    @Override
    public Restlet getInboundRoot(Context context) {
        Restlet inboundRoot = null;

        if (filterProvider != null) {
            inboundRoot = filterProvider.getInboundRoot(context);
            filterProvider.getFilter().setNext(getFilteredRestlet());
        }

        return inboundRoot;
    }

    /**
     * Called by OSGi DS to un-inject the filter provider service
     * 
     * @param filterProvider
     *            the filter provider service
     */
    public void unbindFilterProvider(FilterProvider filterProvider) {
        if (this.filterProvider == filterProvider)
            this.filterProvider = null;
    }
}
