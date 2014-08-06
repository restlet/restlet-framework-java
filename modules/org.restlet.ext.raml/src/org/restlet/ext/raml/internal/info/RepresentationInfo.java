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

package org.restlet.ext.raml.internal.info;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.ext.raml.internal.reflect.ReflectUtils;
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
    public static RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Type representationType,
            Variant variant) {
        RepresentationInfo result = null;

        if (representationClass != null) {
            result = introspect(representationClass, representationType,
                    variant.getMediaType());
        }

        return result;
    }

    /**
     * Returns the description of the given class as a
     * {@link RepresentationInfo}.
     * 
     * @param clazz
     *            The class to document.
     * @param type
     *            The class to document.
     * @param variant
     *            The current variant.
     * @return The description of the given class as a
     *         {@link RepresentationInfo}.
     */
    public static RepresentationInfo introspect(Class<?> clazz, Type type,
            MediaType mediaType) {
        // Introspect the java class
        RepresentationInfo result = new RepresentationInfo(mediaType);
        result.setCollection(ReflectUtils.isListType(clazz));
        Class<?> c = ReflectUtils.getSimpleClass(type);
        result.setType((c == null) ? clazz : c);
        boolean generic = c != null
                && !c.getCanonicalName().equals(clazz.getCanonicalName());
        if (generic) {
            result.setIdentifier(clazz.getName() + c.getName());
            result.setName(clazz.getSimpleName() + c.getSimpleName());
        } else {
            result.setIdentifier(result.getType().getName());
            result.setName(result.getType().getSimpleName());
        }

        if (org.restlet.representation.Representation.class
                .isAssignableFrom(clazz) || ReflectUtils.isJdkClass(c)) {
            result.setRaw(true);
        } else {
            for (Field field : ReflectUtils.getAllDeclaredFields(clazz)) {
                if (!"serialVersionUID".equals(field.getName())) {
                    PropertyInfo property = new PropertyInfo();
                    property.setName(field.getName());
                    property.setType(ReflectUtils.getSimpleClass(field));
                    property.setMinOccurs(0);
                    if (ReflectUtils.isListType(field.getType())) {
                        property.setMaxOccurs(-1);
                    } else {
                        property.setMaxOccurs(1);
                    }
                    result.getProperties().add(property);
                }
            }
        }
        return result;
    }

    /** Indicates if this representation is a collection. */
    private boolean collection;

    /** Identifier for that element. */
    private String identifier;

    /** Media type of that element. */
    private MediaType mediaType;

    /** Name of that element. */
    private String name;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** List of this representation's properties. */
    private List<PropertyInfo> properties;

    /** Indicates if the representation is structured or not. */
    private boolean raw;

    /** Reference to a representation identifier. */
    private String reference;

    /** Reference to its type if any. */
    private Class<?> type;

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

    public String getName() {
        return name;
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

    public List<PropertyInfo> getProperties() {
        // Lazy initialization with double-check.
        List<PropertyInfo> p = this.properties;
        if (p == null) {
            synchronized (this) {
                p = this.properties;
                if (p == null) {
                    this.properties = p = new ArrayList<PropertyInfo>();
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

    public Class<?> getType() {
        return type;
    }

    public boolean isCollection() {
        return collection;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
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

    public void setName(String name) {
        this.name = name;
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

    public void setProperties(List<PropertyInfo> properties) {
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

    public void setType(Class<?> type) {
        this.type = type;
    }

}
