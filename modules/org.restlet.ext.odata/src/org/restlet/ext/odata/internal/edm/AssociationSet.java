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
 * Represents an association inside an EntityContainer.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb386894.aspx">AssociationSet
 *      Element (EntityContainer CSDL)</a>
 */
public class AssociationSet extends NamedObject {

    /** The referenced schema's association. */
    private Association association;

    /** The list of entities implied in this association. */
    private List<AssociationSetEnd> ends;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the association set.
     */
    public AssociationSet(String name) {
        super(name);
    }

    /**
     * Returns the referenced schema's association.
     * 
     * @return The referenced schema's association.
     */
    public Association getAssociation() {
        return association;
    }

    /**
     * Returns the list of entities implied in this association.
     * 
     * @return The list of entities implied in this association.
     */
    public List<AssociationSetEnd> getEnds() {
        if (ends == null) {
            ends = new ArrayList<AssociationSetEnd>();
        }
        return ends;
    }

    /**
     * Sets the referenced schema's association.
     * 
     * @param association
     *            The referenced schema's association.
     */
    public void setAssociation(Association association) {
        this.association = association;
    }

    /**
     * Sets the list of entities implied in this association.
     * 
     * @param ends
     *            The list of entities implied in this association.
     */
    public void setEnds(List<AssociationSetEnd> ends) {
        this.ends = ends;
    }

}
