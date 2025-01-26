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
 * Represents an association inside an EntityContainer.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb386894.aspx">AssociationSet
 *      Element (EntityContainer CSDL)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
