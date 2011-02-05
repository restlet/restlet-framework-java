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

package org.restlet.ext.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.restlet.representation.Representation;

/**
 * Velocity resource loader based on a static map of representations or on a
 * default representation.
 * 
 * @author Jerome Louvel
 */
public class RepresentationResourceLoader extends ResourceLoader {

    /** The cache of template representations. */
    private static final Map<String, Representation> store = new ConcurrentHashMap<String, Representation>();

    /**
     * Returns the cache of template representations.
     * 
     * @return The cache of template representations.
     */
    public static Map<String, Representation> getStore() {
        return store;
    }

    /** The default representation to load. */
    private final Representation defaultRepresentation;

    /**
     * Constructor.
     * 
     * @param defaultRepresentation
     *            The default representation to use.
     */
    public RepresentationResourceLoader(Representation defaultRepresentation) {
        this.defaultRepresentation = defaultRepresentation;
    }

    @Override
    public long getLastModified(Resource resource) {
        final Representation original = getStore().get(resource.getName());
        return (original != null) ? original.getModificationDate().getTime()
                : 0;
    }

    @Override
    public InputStream getResourceStream(String name)
            throws ResourceNotFoundException {
        InputStream result = null;

        try {
            Representation resultRepresentation = getStore().get(name);

            if (resultRepresentation == null) {
                resultRepresentation = this.defaultRepresentation;

                if (resultRepresentation == null) {
                    throw new ResourceNotFoundException(
                            "Could not locate resource '" + name + "'");
                }

                result = resultRepresentation.getStream();
            } else {
                result = resultRepresentation.getStream();
            }
        } catch (IOException ioe) {
            throw new ResourceNotFoundException(ioe);
        }

        return result;
    }

    @Override
    public void init(ExtendedProperties configuration) {

    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return getLastModified(resource) != resource.getLastModified();
    }

}
