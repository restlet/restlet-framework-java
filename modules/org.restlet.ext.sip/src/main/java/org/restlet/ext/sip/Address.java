/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sip;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.sip.internal.AddressWriter;
import org.restlet.util.Series;

/**
 * Address of a SIP agent. Used by the SIP "Alert-Info", "Contact",
 * "Error-info", "From", "Record-Route", "Reply-To", "Route" and "To" headers.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class Address implements Cloneable {

    /** The optional name displayed. */
    private String displayName;

    /** The list of generic parameters. */
    private Series<Parameter> parameters;

    /** The address reference. */
    private Reference reference;

    /**
     * Default constructor.
     */
    public Address() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     */
    public Address(Reference reference) {
        super();
        this.reference = reference;
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     * @param displayName
     *            The name displayed.
     */
    public Address(Reference reference, String displayName) {
        this.reference = reference;
        this.displayName = displayName;
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The address reference.
     * @param displayName
     *            The name displayed.
     */
    public Address(String reference, String displayName) {
        this(new Reference(reference), displayName);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Address result = (Address) super.clone();
        result.reference = reference.clone();

        if (parameters != null) {
            result.parameters = new Form();

            for (Parameter param : parameters) {
                result.parameters.add(param.getName(), param.getValue());
            }
        }

        return result;
    }

    /**
     * Returns the optional name displayed.
     * 
     * @return The optional name displayed.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the list of generic parameters.
     * 
     * @return The list of generic parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Form();
        }
        return parameters;
    }

    /**
     * Returns the address reference.
     * 
     * @return The address reference.
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Sets the optional name displayed.
     * 
     * @param displayName
     *            The optional name displayed.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the list of generic parameters.
     * 
     * @param parameters
     *            The list of generic parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the address reference.
     * 
     * @param reference
     *            The address reference.
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return AddressWriter.write(this);
    }

}
