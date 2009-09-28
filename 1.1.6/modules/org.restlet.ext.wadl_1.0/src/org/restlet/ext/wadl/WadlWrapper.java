package org.restlet.ext.wadl;

import org.restlet.Restlet;
import org.restlet.util.WrapperRestlet;

/**
 * Restlet WADL wrapper. Useful for application developer who need to provide
 * the WADL documentation for a Restlet instance like a Directory.
 * 
 * @author Thierry Boileau
 */
public abstract class WadlWrapper extends WrapperRestlet implements
        WadlDescribable {

    /** The description of the wrapped Restlet. */
    private ResourceInfo resourceInfo;

    /**
     * Constructor.
     * 
     * @param wrappedRestlet
     *            The Restlet to wrap.
     */
    public WadlWrapper(Restlet wrappedRestlet) {
        super(wrappedRestlet);
    }

    /**
     * Returns the description of the wrapped Restlet.
     * 
     * @return The ResourceInfo object of the wrapped Restlet.
     */
    public ResourceInfo getResourceInfo() {
        return this.resourceInfo;
    }

    /**
     * Sets the description of the wrapped Restlet.
     * 
     * @param resourceInfo
     *            The ResourceInfo object of the wrapped Restlet.
     */
    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

}
