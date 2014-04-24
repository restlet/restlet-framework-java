/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark.info;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.apispark.Property;
import org.restlet.ext.apispark.Representation;
import org.restlet.ext.apispark.reflect.ReflectUtils;
import org.restlet.representation.Variant;

/**
 * Describes a variant representation for a target resource.
 * 
 * @author Jerome Louvel
 */
public class RepresentationInfo extends DocumentedInfo {

    /**
     * Describes a representation class and variant couple as APISpark
     * information for the given method and request. The variant contains the
     * target media type that can be converted to by one of the available
     * Restlet converters.<br>
     * <br>
     * By default, it introspects the given class.
     * 
     * @param methodInfo
     *            The parent method description.
     * @param requestInfo
     *            The parent request description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The APISpark representation information.
     */
    protected static RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Variant variant) {
        RepresentationInfo result = null;

        if (representationClass != null) {
            // Introspect the java class
            result = new RepresentationInfo(variant);

            result.setIdentifier(representationClass.getSimpleName());

            if (Representation.class.isAssignableFrom(representationClass)) {
                result.setRaw(true);
            } else {
                // TODO support parent types
                // result.setParentType(parentType);
                for (Field field : ReflectUtils
                        .getAllDeclaredFields(representationClass)) {
                    if (!"serialVersionUID".equals(field.getName())) {
                        Property property = new Property();
                        property.setName(field.getName());
                        property.setType(field.getType().getSimpleName());
                        result.getProperties().add(property);
                    }

                }
            }
        }

        return result;
    }

    /** Identifier for that element. */
    private String identifier;

    /** Media type of that element. */
    private MediaType mediaType;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** Reference to its parent type if any. */
    private String parentType;

    /** List of locations of one or more meta data profiles. */
    private List<Reference> profiles;

    // TODO review
    /** List of this representation's properties. */
    private List<Property> properties;

    /** Indicates if the representation is structured or not. */
    private boolean raw;

    /** Reference to a representation identifier. */
    private String reference;

    /**
     * Constructor.
     */
    public RepresentationInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public RepresentationInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public RepresentationInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a media type.
     * 
     * @param mediaType
     *            The media type of the representation.
     */
    public RepresentationInfo(MediaType mediaType) {
        setMediaType(mediaType);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public RepresentationInfo(String documentation) {
        super(documentation);
    }

    /**
     * Constructor with a variant.
     * 
     * @param variant
     *            The variant to describe.
     */
    public RepresentationInfo(Variant variant) {
        setMediaType(variant.getMediaType());
    }

    /**
     * Returns the identifier for that element.
     * 
     * @return The identifier for that element.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the media type of that element.
     * 
     * @return The media type of that element.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<ParameterInfo> getParameters() {
        // Lazy initialization with double-check.
        List<ParameterInfo> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    this.parameters = p = new ArrayList<ParameterInfo>();
                }
            }
        }
        return p;
    }

    public String getParentType() {
        return parentType;
    }

    /**
     * Returns the list of locations of one or more meta data profiles.
     * 
     * @return The list of locations of one or more meta data profiles.
     */
    public List<Reference> getProfiles() {
        // Lazy initialization with double-check.
        List<Reference> p = this.profiles;
        if (p == null) {
            synchronized (this) {
                p = this.profiles;
                if (p == null) {
                    this.profiles = p = new ArrayList<Reference>();
                }
            }
        }
        return p;
    }

    public List<Property> getProperties() {
        // Lazy initialization with double-check.
        List<Property> p = this.properties;
        if (p == null) {
            synchronized (this) {
                p = this.properties;
                if (p == null) {
                    this.properties = p = new ArrayList<Property>();
                }
            }
        }
        return p;
    }

    /**
     * Returns the reference to an representation identifier.
     * 
     * @return The reference to an representation identifier.
     */
    public String getReference() {
        return reference;
    }

    public boolean isRaw() {
        return raw;
    }

    /**
     * Sets the identifier for that element.
     * 
     * @param identifier
     *            The identifier for that element.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the media type of that element.
     * 
     * @param mediaType
     *            The media type of that element.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    /**
     * Sets the list of locations of one or more meta data profiles.
     * 
     * @param profiles
     *            The list of locations of one or more meta data profiles.
     */
    public void setProfiles(List<Reference> profiles) {
        this.profiles = profiles;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    /**
     * Sets the reference to an representation identifier.
     * 
     * @param reference
     *            The reference to an representation identifier.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

}
