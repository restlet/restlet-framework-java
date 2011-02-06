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

package org.restlet.ext.rdf.internal.turtle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.Reference;

/**
 * Contains essentials data updated during the parsing of a N3 document such as
 * the list of known namespaces, keywords.
 * 
 * @author Thierry Boileau
 */
public class Context {

    /** The value of the "base" keyword. */
    private Reference base;

    /** The list of known keywords. */
    private List<String> keywords;

    /** The list of known prefixes. */
    private Map<String, String> prefixes;

    /**
     * Default constructor.
     */
    public Context() {
        super();
        this.prefixes = new HashMap<String, String>();
        // "#" is the default URI of the "null" prefix
        this.prefixes.put(":", "#");
        this.keywords = new ArrayList<String>();
    }

    /**
     * Returns the base reference.
     * 
     * @return The base reference.
     */
    public Reference getBase() {
        if (base == null) {
            base = new Reference();
        }
        return base;
    }

    /**
     * Returns the list of known keywords.
     * 
     * @return The list of known keywords.
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Returns the list of known prefixes.
     * 
     * @return The list of known prefixes.
     */
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    /**
     * Returns true if the given value is a qualified name.
     * 
     * @param value
     *            The value to test.
     * @return True if the given value is a qualified name.
     */
    public boolean isQName(String value) {
        boolean result = (value.indexOf(":") != -1)
                || getKeywords().contains(value);

        return result;
    }

    /**
     * Resolves a qualified name according to the current context.
     * 
     * @param qname
     *            The qualified name to resolve.
     * @return The RDF URI reference.
     */
    public Reference resolve(String qname) {
        Reference result = null;
        int index = qname.indexOf(":");
        if (index != -1) {
            String prefix = qname.substring(0, index + 1);
            String base = getPrefixes().get(prefix);
            if (base != null) {
                result = new Reference(base + qname.substring(index + 1));
            } else {
                org.restlet.Context.getCurrentLogger().warning(
                        "Error, this prefix " + prefix
                                + " has not been declared!");
            }
        } else {
            if (getKeywords().contains(qname)) {
                String base = getPrefixes().get(":");
                if (base != null) {
                    result = new Reference(base + qname);
                } else {
                    org.restlet.Context.getCurrentLogger().warning(
                            "Error, the empty prefix has not been declared!");
                }
            } else {
                result = new Reference(getBase().toString() + qname);
            }
        }

        return result;
    }

    /**
     * Sets the base reference.
     * 
     * @param base
     *            The base reference.
     */
    public void setBase(Reference base) {
        this.base = base;
    }
}