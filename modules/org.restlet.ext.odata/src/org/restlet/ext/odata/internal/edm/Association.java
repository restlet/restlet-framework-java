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
 */
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
