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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;

/**
 * Represents an exposed stored procedure.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/cc716710.aspx">FunctionImport
 *      Element (CSDL)</a>
 */
public class FunctionImport extends NamedObject {

    /** The entity set returned by this function, if applicable. */
    private EntitySet entitySet;

    /** The metadata. */
    private Metadata metadata;

    /** The method used to invoke this function. */
    private Method method;

    /**
     * The method access of this function (defined in the CSDL, but not
     * described).
     */
    private String methodAccess;

    /** The list of parameters. */
    private List<Parameter> parameters;

    /** The return type of this function. */
    private String returnType;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the schema.
     */
    public FunctionImport(String name) {
        super(name);
    }

    /**
     * Returns the entity set returned by this function, if applicable.
     * 
     * @return The entity set returned by this function, if applicable.
     */
    public EntitySet getEntitySet() {
        return entitySet;
    }

    /**
     * Returns the metadata.
     * 
     * @return The metadata.
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the method used to invoke this function.
     * 
     * @return The method used to invoke this function.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the method access of this function (defined in the CSDL, but not
     * described).
     * 
     * @return The method access of this function (defined in the CSDL, but not
     *         described).
     */
    public String getMethodAccess() {
        return methodAccess;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        return parameters;
    }

    /**
     * Returns the return type of this function.
     * 
     * @return The return type of this function.
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Returns the return type as complex type, or null if it is not.
     * 
     * @return The return type as complex type, or null if it is not.
     */
    public ComplexType getReturnTypeAsComplexType() {
        ComplexType result = null;
        String rt = getSimpleReturnType();
        if (getReturnType() != null && metadata != null) {
            for (Schema schema : metadata.getSchemas()) {
                for (ComplexType complexType : schema.getComplexTypes()) {
                    if (rt.equalsIgnoreCase(complexType.getName())) {
                        result = complexType;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the return type as complex type, or null if it is not.
     * 
     * @return The return type as complex type, or null if it is not.
     */
    public EntityType getReturnTypeAsEntityType() {
        EntityType result = null;
        String rt = getSimpleReturnType();
        if (getReturnType() != null && metadata != null) {
            for (Schema schema : metadata.getSchemas()) {
                for (EntityType entityType : schema.getEntityTypes()) {
                    if (rt.equalsIgnoreCase(entityType.getName())) {
                        result = entityType;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the name of the return type, or if it's a collection, returns its
     * element's type.
     * 
     * @return The name of the return type, or if it's a collection, returns its
     *         element's type.
     */
    public String getSimpleReturnType() {
        return returnType;
    }

    /**
     * Returns true if the result of the invocation of the service is a
     * collection.
     * 
     * @return True if the result of the invocation of the service is a
     *         collection.
     */
    public boolean isReturningCollection() {
        return getReturnType() != null
                && getReturnType().toLowerCase().startsWith("collection(");
    }

    /**
     * Returns true if the result of the invocation of the service is a complex
     * type.
     * 
     * @return True if the result of the invocation of the service is a complex
     *         type.
     */
    public boolean isReturningComplexType() {
        return getReturnTypeAsComplexType() != null;
    }

    /**
     * Returns true if the result of the invocation of the service is an EDM
     * simple type.
     * 
     * @return True if the result of the invocation of the service is an EDM
     *         simple type.
     */
    public boolean isReturningEdmSimpleType() {
        return getReturnType() != null
                && getReturnType().toLowerCase().startsWith("edm.");
    }

    /**
     * Returns true if the result of the invocation of the service is an entity
     * type.
     * 
     * @return True if the result of the invocation of the service is an entity
     *         type.
     */
    public boolean isReturningEntityType() {
        return getReturnType() != null
                && getReturnType().toLowerCase().startsWith("edm.");
    }

    /**
     * Sets the entity set returned by this function, if applicable.
     * 
     * @param entitySet
     *            The entity set returned by this function, if applicable.
     */
    public void setEntitySet(EntitySet entitySet) {
        this.entitySet = entitySet;
    }

    /**
     * Sets the metadata.
     * 
     * @param metadata
     *            The metadata.
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the method used to invoke this function.
     * 
     * @param method
     *            The method used to invoke this function.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Sets the method access of this function (defined in the CSDL, but not
     * described).
     * 
     * @param methodAccess
     *            The method access of this function (defined in the CSDL, but
     *            not described).
     */
    public void setMethodAccess(String methodAccess) {
        this.methodAccess = methodAccess;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the return type of this function.
     * 
     * @param returnType
     *            The return type of this function.
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

}
