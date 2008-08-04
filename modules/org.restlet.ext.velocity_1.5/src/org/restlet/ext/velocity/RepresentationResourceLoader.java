/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
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
import org.restlet.resource.Representation;

/**
 * Velocity resource loader based on a static map of representations or on a
 * default representation.
 * 
 * @author Jerome Louvel
 */
public class RepresentationResourceLoader extends ResourceLoader {

    private static final Map<String, Representation> store = new ConcurrentHashMap<String, Representation>();

    public static Map<String, Representation> getStore() {
        return store;
    }

    /** The default representation to load. */
    private final Representation defaultRepresentation;

    /**
     * Constructeur.
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
                } else {
                    result = resultRepresentation.getStream();
                }
            } else {
                result = resultRepresentation.getStream();
            }
        } catch (final IOException ioe) {
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
