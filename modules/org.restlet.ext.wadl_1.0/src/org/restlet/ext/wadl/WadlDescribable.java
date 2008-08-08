package org.restlet.ext.wadl;

/**
 * Intended to classes that are able to furnish their own Wadl documentation.
 */
public interface WadlDescribable {

    /**
     * Returns a full documented resourceInfo instance.
     * 
     * @return A full documented resourceInfo instance.
     */
    public ResourceInfo getResourceInfo();

}
