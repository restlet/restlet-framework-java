/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.odata.internal.edm;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an association inside a schema.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399734.aspx">Association
 *      Element (CSDL)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class Association extends NamedObject {
    /** The list of entities linked by this association. */
    private List<AssociationEnd> ends;

    /** The schema. */
    private Schema schema;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the schema.
     */
    public Association(String name) {
        super(name);
    }

    /**
     * Returns the list of entities linked by this association.
     * 
     * @return The list of entities linked by this association.
     */
    public List<AssociationEnd> getEnds() {
        if (ends == null) {
            ends = new ArrayList<AssociationEnd>();
        }
        return ends;
    }

    /**
     * Returns the schema.
     * 
     * @return The schema.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Sets the list of entities linked by this association.
     * 
     * @param ends
     *            The list of entities linked by this association.
     */
    public void setEnds(List<AssociationEnd> ends) {
        this.ends = ends;
    }

    /**
     * Sets the schema.
     * 
     * @param schema
     *            The schema.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
