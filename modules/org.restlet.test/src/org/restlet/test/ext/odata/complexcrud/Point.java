package org.restlet.test.ext.odata.complexcrud;

import java.util.ArrayList;
import java.util.List;

/**
* Generated for the OData extension for the Restlet framework.<br>
*
* @see <a href="http://localhost:8111/Cafe.svc/$metadata">Metadata of the target WCF Data Services</a>
*
*/
public class Point {

    private String geo_type;
    private String geo_name;
    private List<StructAny> properties = new ArrayList<StructAny>();
    private List<java.lang.Double> x = new ArrayList<java.lang.Double>();
    private List<java.lang.Double> y = new ArrayList<java.lang.Double>();

    /**
     * Constructor without parameter.
     * 
     */
    public Point() {
        super();
    }
    
   /**
    * Returns the value of the "geo_type" attribute.
    *
    * @return The value of the "geo_type" attribute.
    */
   public String getGeo_type() {
      return geo_type;
   }

  

   /**
    * Returns the value of the "x" attribute.
    *
    * @return The value of the "x" attribute.
    */
   public List<java.lang.Double> getX() {
      return x;
   }

   /**
    * Returns the value of the "y" attribute.
    *
    * @return The value of the "y" attribute.
    */
   public List<java.lang.Double> getY() {
      return y;
   }



   /**
    * Sets the value of the "geo_type" attribute.
    *
    * @param geo_type
    *     The value of the "geo_type" attribute.
    */
   public void setGeo_type(String geo_type) {
      this.geo_type = geo_type;
   }

   
   
   /**
    * Sets the value of the "x" attribute.
    *
    * @param x
    *     The value of the "x" attribute.
    */
   public void setX(List<java.lang.Double> x) {
      this.x = x;
   }
   
   /**
    * Sets the value of the "y" attribute.
    *
    * @param y
    *     The value of the "y" attribute.
    */
   public void setY(List<java.lang.Double> y) {
      this.y = y;
   }
   
  
/**
 * @return the geo_name
 */
public String getGeo_name() {
	return geo_name;
}

/**
 * @param geo_name the geo_name to set
 */
public void setGeo_name(String geo_name) {
	this.geo_name = geo_name;
}

/**
 * @return the properties
 */
public List<StructAny> getProperties() {
	return properties;
}

/**
 * @param properties the properties to set
 */
public void setProperties(List<StructAny> properties) {
	this.properties = properties;
}
   
}