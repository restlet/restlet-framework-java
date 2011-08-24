/**
 * 
 */
package org.restlet.example.ext.oauth;

import org.json.JSONObject;

/**
 * @author esvmart
 *
 */
public class FBUser {

  public String id;
  public String name;
  public String firstName;
  public String lastName;
  public String link;
  public String hometown;
  public String updated;


  public FBUser(JSONObject user){
    try{
      setUser(user);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  public String getFirstName() {
    return firstName;
  }

  public String getHometown() {
    return hometown;
  }

  public String getId() {
    return id;
  }

  public String getLastName() {
    return lastName;
  }


  public String getLink() {
    return link;
  }


  public String getName() {
    return name;
  }


  public String getUpdated() {
    return updated;
  }


  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }


  public void setHometown(String hometown) {
    this.hometown = hometown;
  }


  public void setId(String id) {
    this.id = id;
  }


  public void setLastName(String lastName) {
    this.lastName = lastName;
  }


  public void setLink(String link) {
    this.link = link;
  }


  public void setName(String name) {
    this.name = name;
  }


  public void setUpdated(String updated) {
    this.updated = updated;
  }


  public void setUser(JSONObject user) throws Exception{
      setId(user.getString("id"));
    try{
      setName(user.getString("name"));
    }
    catch(Exception e){
      System.out.println("could not set name");
    }
    try{
      setFirstName(user.getString("first_name"));
    }
    catch(Exception e){
      System.out.println("could not set first name");
    }
    try{
      setLastName(user.getString("last_name"));
    }
    catch(Exception e){
      System.out.println("could not set last name");
    } 
    try{
      setLink(user.getString("link"));
    }
    catch(Exception e){
      System.out.println("could not set link");
    } 
    try{
      JSONObject ht = user.getJSONObject("hometown");
      if(ht != null){
        try{
          setHometown(ht.getString("name"));
        }
        catch(Exception e){
          System.out.println("could not set name");
        } 
      }
    }
    catch(Exception e) {System.out.println("could not get hometown");}
    
    try{
        setUpdated(user.getString("updated_time"));
      }
      catch(Exception e){
        System.out.println("could not set update time");
      }
    }
  }
