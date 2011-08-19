/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.openid;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Metadata;
import org.restlet.engine.util.SystemUtils;

/**
 * @author esvmart
 *
 */
public class AX extends Metadata {
    
    private volatile String schema;
    private static volatile Map <String, AX> _attributes;
    
    public static final AX FRIENDLY_NAME = register(
            "friendly", "http://axschema.org/namePerson/friendly", "Friendly name");
    public static final AX EMAIL = register(
            "email", "http://axschema.org/contact/email", "email");
    public static final AX FIRST_NAME = register(
            "first", "http://axschema.org/namePerson/first", "Given name");
    public static final AX LAST_NAME = register(
            "last", "http://axschema.org/namePerson/last", "Surname");
    public static final AX FULL_NAME = register(
            "fullname", "http://axschema.org/namePerson", "Fullname");
    public static final AX DOB = register(
            "dob", "http://axschema.org/birthDate", "Date of birth");
    public static final AX GENDER = register(
            "gender", "http://axschema.org/person/gender", "Gender");
    public static final AX ZIP = register(
            "postcode", "http://axschema.org/contact/postalCode/home", "Home zip code");
    public static final AX COUNTRY = register(
            "country", "http://axschema.org/contact/country/home", "Country of residence");
    public static final AX LANGUAGE = register(
            "language", "http://axschema.org/pref/language", "Preferred language");
    public static final AX TIMEZONE = register(
            "timezone", "http://axschema.org/pref/timezone", "Preferred timezone");
    
    
    public static synchronized AX register(String name, String schema,
            String description) {

        if (!getAttributes().containsKey(name)) {
            final AX ax = new AX(name, schema, description);
            getAttributes().put(name, ax);
        }

        return getAttributes().get(name);
    }
    
    public static AX valueOf(String name) {
        AX result = null;

        if ((name != null) && !name.equals("")) {
            result = getAttributes().get(name);
        }
        return result;
    }
    
    private static Map<String, AX> getAttributes() {
        if (_attributes == null) {
            _attributes = new HashMap<String, AX>();
        }
        return _attributes;
    }
    
    public AX(String name, String schema, String description){
        super(name, description);
        this.schema = schema;
    }
    
    public String getSchema(){
        return this.schema;
    }
    
    public void setSchema(String schema){
        this.schema = schema;
    }

    @Override
    public Metadata getParent() {
        return null;
    }
    
    
    @Override
    public int hashCode(){
        return SystemUtils.hashCode(getName(), getSchema());
    }
    
    @Override
    public boolean equals(Object object) {
        if(object instanceof AX){
           return this.getName().equals(((AX) object).getName());
        }
        return false;
    }




    @Override
    public boolean includes(Metadata included) {
        return this.equals(included);
    }

}
