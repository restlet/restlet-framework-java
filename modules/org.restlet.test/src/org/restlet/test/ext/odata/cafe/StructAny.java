/**
 * Copyright 2005-2013 Restlet S.A.S.
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

package org.restlet.test.ext.odata.cafe;



/**
* Generated for the OData extension for the Restlet framework.<br>
*
* @see <a href="http://localhost:8111/Cafe.svc/$metadata">Metadata of the target WCF Data Services</a>
*
*/
public class StructAny {

    private String name;
    private String type;
    private String unit;
    private String unitType;
    private String values;

    /**
     * Constructor without parameter.
     * 
     */
    public StructAny() {
        super();
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
    * Returns the value of the "type" attribute.
    *
    * @return The value of the "type" attribute.
    */
   public String getType() {
      return type;
   }

   /**
    * Returns the value of the "unit" attribute.
    *
    * @return The value of the "unit" attribute.
    */
   public String getUnit() {
      return unit;
   }

   /**
    * Returns the value of the "unitType" attribute.
    *
    * @return The value of the "unitType" attribute.
    */
   public String getUnitType() {
      return unitType;
   }

   /**
    * Returns the value of the "values" attribute.
    *
    * @return The value of the "values" attribute.
    */
   public String getValues() {
      return values;
   }


   /**
    * Sets the value of the "name" attribute.
    *
    * @param name
    *     The value of the "name" attribute.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Sets the value of the "type" attribute.
    *
    * @param type
    *     The value of the "type" attribute.
    */
   public void setType(String type) {
      this.type = type;
   }

   /**
    * Sets the value of the "unit" attribute.
    *
    * @param unit
    *     The value of the "unit" attribute.
    */
   public void setUnit(String unit) {
      this.unit = unit;
   }

   /**
    * Sets the value of the "unitType" attribute.
    *
    * @param unitType
    *     The value of the "unitType" attribute.
    */
   public void setUnitType(String unitType) {
      this.unitType = unitType;
   }

   /**
    * Sets the value of the "values" attribute.
    *
    * @param values
    *     The value of the "values" attribute.
    */
   public void setValues(String values) {
      this.values = values;
   }

}