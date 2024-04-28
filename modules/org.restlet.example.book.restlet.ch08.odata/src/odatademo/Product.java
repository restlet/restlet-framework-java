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

package odatademo;


import java.util.Date;
import odatademo.Category;
import odatademo.Supplier;

/**
* Generated by the generator tool for the OData extension for the Restlet framework.<br>
*
* @see <a href="http://services.odata.org/OData/OData.svc/$metadata">Metadata of the target OData service</a>
*
*/
public class Product {

    private String description;
    private Date discontinuedDate;
    private int id;
    private String name;
    private double price;
    private int rating;
    private Date releaseDate;
    private Category category;
    private Supplier supplier;

    /**
     * Constructor without parameter.
     * 
     */
    public Product() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Product(int id) {
        this();
        this.id = id;
    }

   /**
    * Returns the value of the "description" attribute.
    *
    * @return The value of the "description" attribute.
    */
   public String getDescription() {
      return description;
   }
   /**
    * Returns the value of the "discontinuedDate" attribute.
    *
    * @return The value of the "discontinuedDate" attribute.
    */
   public Date getDiscontinuedDate() {
      return discontinuedDate;
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
    * Returns the value of the "name" attribute.
    *
    * @return The value of the "name" attribute.
    */
   public String getName() {
      return name;
   }
   /**
    * Returns the value of the "price" attribute.
    *
    * @return The value of the "price" attribute.
    */
   public double getPrice() {
      return price;
   }
   /**
    * Returns the value of the "rating" attribute.
    *
    * @return The value of the "rating" attribute.
    */
   public int getRating() {
      return rating;
   }
   /**
    * Returns the value of the "releaseDate" attribute.
    *
    * @return The value of the "releaseDate" attribute.
    */
   public Date getReleaseDate() {
      return releaseDate;
   }
   /**
    * Returns the value of the "category" attribute.
    *
    * @return The value of the "category" attribute.
    */
   public Category getCategory() {
      return category;
   }
   
   /**
    * Returns the value of the "supplier" attribute.
    *
    * @return The value of the "supplier" attribute.
    */
   public Supplier getSupplier() {
      return supplier;
   }
   
   /**
    * Sets the value of the "description" attribute.
    *
    * @param description
    *     The value of the "description" attribute.
    */
   public void setDescription(String description) {
      this.description = description;
   }
   /**
    * Sets the value of the "discontinuedDate" attribute.
    *
    * @param discontinuedDate
    *     The value of the "discontinuedDate" attribute.
    */
   public void setDiscontinuedDate(Date discontinuedDate) {
      this.discontinuedDate = discontinuedDate;
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
    * Sets the value of the "name" attribute.
    *
    * @param name
    *     The value of the "name" attribute.
    */
   public void setName(String name) {
      this.name = name;
   }
   /**
    * Sets the value of the "price" attribute.
    *
    * @param price
    *     The value of the "price" attribute.
    */
   public void setPrice(double price) {
      this.price = price;
   }
   /**
    * Sets the value of the "rating" attribute.
    *
    * @param rating
    *     The value of the "rating" attribute.
    */
   public void setRating(int rating) {
      this.rating = rating;
   }
   /**
    * Sets the value of the "releaseDate" attribute.
    *
    * @param releaseDate
    *     The value of the "releaseDate" attribute.
    */
   public void setReleaseDate(Date releaseDate) {
      this.releaseDate = releaseDate;
   }
   /**
    * Sets the value of the "category" attribute.
    *
    * @param category"
    *     The value of the "category" attribute.
    */
   public void setCategory(Category category) {
      this.category = category;
   }

   /**
    * Sets the value of the "supplier" attribute.
    *
    * @param supplier"
    *     The value of the "supplier" attribute.
    */
   public void setSupplier(Supplier supplier) {
      this.supplier = supplier;
   }

}
