/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.odata.internal.edm;

/**
 * Represents a schema's namespace in the metadata descriptor of a OData
 * service.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class Namespace extends NamedObject {
    /** The short alias for this namespace. */
    private String alias;

    /**
     * Constructor.
     * 
     * @param name
     *            The alias for this namespace.
     */
    public Namespace(String name) {
        super(name);
    }

    /**
     * Returns the short alias for this namespace.
     * 
     * @return The short alias for this namespace.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the short alias for this namespace.
     * 
     * @param alias
     *            The short alias for this namespace.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

}
