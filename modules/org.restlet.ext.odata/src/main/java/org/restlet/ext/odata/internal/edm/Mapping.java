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

package org.restlet.ext.odata.internal.edm;

/**
 * Defines a property mapping used in customized feeds. It maps an XML element
 * of the feed to parse and a property of entity.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/ee373839.aspx">Atom
 *      Feed Customization (ADO.NET Data Services)</a>
 */
public class Mapping {

    /** Is the value taken from an attribute or not. */
    private boolean attributeValue;

    /** The content type of the value of the mapped property. */
    private String contentKind;

    /** The namespace prefix of the feed's XML element that stores the value. */
    private String nsPrefix;

    /** The namespace URI of the feed's XML element that stores the value. */
    private String nsUri;

    /** The path of the property to update. */
    private String propertyPath;

    /** The type of the entity to update. */
    private EntityType type;

    /** The path of the XML element of the feed that stores the value to set. */
    private String valuePath;

    /**
     * Constructor.
     * 
     * @param type
     *            The type of the entity to update.
     * @param nsPrefix
     *            The namespace prefix of the feed's XML element that stores the
     *            value.
     * @param nsUri
     *            The namespace URI of the feed's XML element that stores the
     *            value.
     * @param propertyPath
     *            The type of the entity to update.
     * @param valuePath
     *            The path of the XML element of the feed that stores the value
     *            to set.
     * @param contentKind
     *            The content type of the value of the mapped property..
     */
    public Mapping(EntityType type, String nsPrefix, String nsUri,
            String propertyPath, String valuePath, String contentKind) {
        super();
        this.type = type;
        this.nsPrefix = nsPrefix;
        this.nsUri = nsUri;
        this.propertyPath = propertyPath;
        this.valuePath = valuePath;
        this.contentKind = contentKind;

        this.attributeValue = false;
        if (this.valuePath != null) {
            int index = valuePath.lastIndexOf("/");
            if (index != -1 && valuePath.length() > index) {
                attributeValue = ('@' == valuePath.charAt(index + 1));
            }
        }
    }

    /**
     * Returns the content type of the value of the mapped property.
     * 
     * @return The content type of the value of the mapped property.
     */
    public String getContentKind() {
        return contentKind;
    }

    /**
     * Returns the namespace prefix of the feed's XML element that stores the
     * value.
     * 
     * @return The namespace prefix of the feed's XML element that stores the
     *         value.
     */
    public String getNsPrefix() {
        return nsPrefix;
    }

    /**
     * Returns the namespace URI of the feed's XML element that stores the
     * value.
     * 
     * @return The namespace URI of the feed's XML element that stores the
     *         value.
     */
    public String getNsUri() {
        return nsUri;
    }

    /**
     * Returns the path of the property to update.
     * 
     * @return The path of the property to update.
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * Returns the type of the entity to update.
     * 
     * @return The type of the entity to update.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Returns the name of the attribute that stores the value to set, if
     * pertinent, and null otherwise.
     * 
     * @return The he name of the attribute that stores the value to set.
     */
    public String getValueAttributeName() {
        String result = null;
        if (isAttributeValue()) {
            int index = valuePath.lastIndexOf("/");
            result = valuePath.substring(index + 2, valuePath.length());
        }
        return result;
    }

    /**
     * Returns the name of the attribute that stores the value to set, if
     * pertinent, and null otherwise.
     * 
     * @return The he name of the attribute that stores the value to set.
     */
    public String getValueNodePath() {
        return (isAttributeValue()) ? valuePath.substring(0,
                valuePath.lastIndexOf("/")) : valuePath;
    }

    /**
     * Returns the path of the XML element of the feed that stores the value to
     * set.
     * 
     * @return The path of the XML element of the feed that stores the value to
     *         set.
     */
    public String getValuePath() {
        return valuePath;
    }

    /**
     * Returns true if the value is taken from an attribute.
     * 
     * @return True if the value is taken from an attribute.
     */
    public boolean isAttributeValue() {
        return attributeValue;
    }
}
