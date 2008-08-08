package org.restlet.ext.wadl;

import org.restlet.Restlet;
import org.restlet.util.WrapperRestlet;

/**
 * Restlet wrapper. Useful for application developer who need to provide the
 * WADL documentation for a Restlet instance.
 * 
 * @author Thierry Boileau
 */
public abstract class WadlWrapper extends WrapperRestlet implements
        WadlDescribable {

    public WadlWrapper(Restlet wrappedRestlet) {
        super(wrappedRestlet);
    }

    /**
     * Provides the data available about the wrapped Restlet.
     * 
     * @return The ResourceInfo object of the wrapped Restlet.
     */
    public abstract ResourceInfo getResourceInfo();

}
