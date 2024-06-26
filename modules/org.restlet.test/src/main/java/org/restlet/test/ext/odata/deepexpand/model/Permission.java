/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.odata.deepexpand.model;

import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.EntityAccess;
import org.restlet.test.ext.odata.deepexpand.model.Role;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Permission {

    private String comment;

    private int id;

    private String managerName;

    private String name;

    private Tracking tracking;

    private List<EntityAccess> entityAccesses;

    private List<Role> roles;

    /**
     * Constructor without parameter.
     * 
     */
    public Permission() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Permission(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "comment" attribute.
     * 
     * @return The value of the "comment" attribute.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the value of the "id" attribute.
     * 
     * @return The value of the "id" attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the value of the "managerName" attribute.
     * 
     * @return The value of the "managerName" attribute.
     */
    public String getManagerName() {
        return managerName;
    }

    /**
     * Returns the value of the "name" attribute.
     * 
     * @return The value of the "name" attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the "tracking" attribute.
     * 
     * @return The value of the "tracking" attribute.
     */
    public Tracking getTracking() {
        return tracking;
    }

    /**
     * Returns the value of the "entityAccesses" attribute.
     * 
     * @return The value of the "entityAccesses" attribute.
     */
    public List<EntityAccess> getEntityAccesses() {
        return entityAccesses;
    }

    /**
     * Returns the value of the "roles" attribute.
     * 
     * @return The value of the "roles" attribute.
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the value of the "comment" attribute.
     * 
     * @param comment
     *            The value of the "comment" attribute.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the value of the "id" attribute.
     * 
     * @param id
     *            The value of the "id" attribute.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the value of the "managerName" attribute.
     * 
     * @param managerName
     *            The value of the "managerName" attribute.
     */
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    /**
     * Sets the value of the "name" attribute.
     * 
     * @param name
     *            The value of the "name" attribute.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value of the "tracking" attribute.
     * 
     * @param tracking
     *            The value of the "tracking" attribute.
     */
    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    /**
     * Sets the value of the "entityAccesses" attribute.
     * 
     * @param entityAccesses
     *            " The value of the "entityAccesses" attribute.
     */
    public void setEntityAccesses(List<EntityAccess> entityAccesses) {
        this.entityAccesses = entityAccesses;
    }

    /**
     * Sets the value of the "roles" attribute.
     * 
     * @param roles
     *            " The value of the "roles" attribute.
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

}