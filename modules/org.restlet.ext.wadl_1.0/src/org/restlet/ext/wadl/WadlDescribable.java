package org.restlet.ext.wadl;

/**
 * Interface that any Restlet can implement in order to provide their own WADL
 * documentation. This is especially useful for subclasses of Directory or other
 * resource finders when the WADL introspection can reach Resource or better
 * WadlResource instances.
 * 
 * @author Thierry Boileau
 */
public interface WadlDescribable {

    /**
     * Returns a full documented resourceInfo instance.
     * 
     * @return A full documented resourceInfo instance.
     */
    public ResourceInfo getResourceInfo();

}
