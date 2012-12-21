/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.odata.deepexpand.model;


import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.Multilingual;
import org.restlet.test.ext.odata.deepexpand.model.Report;
import org.restlet.test.ext.odata.deepexpand.model.Role;

/**
* Generated by the generator tool for the OData extension for the Restlet framework.<br>
*
* @see <a href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata of the target OData service</a>
*
*/
public class ReportType {

    private boolean _final;
    private String codeName;
    private String factoryClassName;
    private int id;
    private boolean isAggregate;
    private String scope;
    private Tracking tracking;
    private Multilingual comments;
    private Multilingual name;
    private List<Report> reports;
    private List<Role> roles;

    /**
     * Constructor without parameter.
     * 
     */
    public ReportType() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public ReportType(int id) {
        this();
        this.id = id;
    }

   /**
    * Returns the value of the "_final" attribute.
    *
    * @return The value of the "_final" attribute.
    */
   public boolean get_final() {
      return _final;
   }
   /**
    * Returns the value of the "codeName" attribute.
    *
    * @return The value of the "codeName" attribute.
    */
   public String getCodeName() {
      return codeName;
   }
   /**
    * Returns the value of the "factoryClassName" attribute.
    *
    * @return The value of the "factoryClassName" attribute.
    */
   public String getFactoryClassName() {
      return factoryClassName;
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
    * Returns the value of the "isAggregate" attribute.
    *
    * @return The value of the "isAggregate" attribute.
    */
   public boolean getIsAggregate() {
      return isAggregate;
   }
   /**
    * Returns the value of the "scope" attribute.
    *
    * @return The value of the "scope" attribute.
    */
   public String getScope() {
      return scope;
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
    * Returns the value of the "comments" attribute.
    *
    * @return The value of the "comments" attribute.
    */
   public Multilingual getComments() {
      return comments;
   }
   
   /**
    * Returns the value of the "name" attribute.
    *
    * @return The value of the "name" attribute.
    */
   public Multilingual getName() {
      return name;
   }
   
   /**
    * Returns the value of the "reports" attribute.
    *
    * @return The value of the "reports" attribute.
    */
   public List<Report> getReports() {
      return reports;
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
    * Sets the value of the "_final" attribute.
    *
    * @param _final
    *     The value of the "_final" attribute.
    */
   public void set_final(boolean _final) {
      this._final = _final;
   }
   /**
    * Sets the value of the "codeName" attribute.
    *
    * @param codeName
    *     The value of the "codeName" attribute.
    */
   public void setCodeName(String codeName) {
      this.codeName = codeName;
   }
   /**
    * Sets the value of the "factoryClassName" attribute.
    *
    * @param factoryClassName
    *     The value of the "factoryClassName" attribute.
    */
   public void setFactoryClassName(String factoryClassName) {
      this.factoryClassName = factoryClassName;
   }
   /**
    * Sets the value of the "id" attribute.
    *
    * @param id
    *     The value of the "id" attribute.
    */
   public void setId(int id) {
      this.id = id;
   }
   /**
    * Sets the value of the "isAggregate" attribute.
    *
    * @param isAggregate
    *     The value of the "isAggregate" attribute.
    */
   public void setIsAggregate(boolean isAggregate) {
      this.isAggregate = isAggregate;
   }
   /**
    * Sets the value of the "scope" attribute.
    *
    * @param scope
    *     The value of the "scope" attribute.
    */
   public void setScope(String scope) {
      this.scope = scope;
   }
   /**
    * Sets the value of the "tracking" attribute.
    *
    * @param tracking
    *     The value of the "tracking" attribute.
    */
   public void setTracking(Tracking tracking) {
      this.tracking = tracking;
   }
   
   /**
    * Sets the value of the "comments" attribute.
    *
    * @param comments"
    *     The value of the "comments" attribute.
    */
   public void setComments(Multilingual comments) {
      this.comments = comments;
   }

   /**
    * Sets the value of the "name" attribute.
    *
    * @param name"
    *     The value of the "name" attribute.
    */
   public void setName(Multilingual name) {
      this.name = name;
   }

   /**
    * Sets the value of the "reports" attribute.
    *
    * @param reports"
    *     The value of the "reports" attribute.
    */
   public void setReports(List<Report> reports) {
      this.reports = reports;
   }

   /**
    * Sets the value of the "roles" attribute.
    *
    * @param roles"
    *     The value of the "roles" attribute.
    */
   public void setRoles(List<Role> roles) {
      this.roles = roles;
   }

}