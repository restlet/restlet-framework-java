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

package org.restlet.ext.openid;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Metadata;
import org.restlet.engine.util.SystemUtils;

/**
 * Describes an attributes exchange, also known as an AX.
 * 
 * @author Martin Svensson
 */
public class AttributeExchange extends Metadata implements
        Comparable<AttributeExchange> {

    private static volatile Map<String, AttributeExchange> _attributes;

    public static final AttributeExchange COUNTRY = register("country",
            "http://axschema.org/contact/country/home", "Country of residence");

    public static final AttributeExchange DOB = register("dob",
            "http://axschema.org/birthDate", "Date of birth");

    public static final AttributeExchange EMAIL = register("email",
            "http://axschema.org/contact/email", "email");

    public static final AttributeExchange FIRST_NAME = register("first",
            "http://axschema.org/namePerson/first", "Given name");

    public static final AttributeExchange FRIENDLY_NAME = register("friendly",
            "http://axschema.org/namePerson/friendly", "Friendly name");

    public static final AttributeExchange FULL_NAME = register("fullname",
            "http://axschema.org/namePerson", "Fullname");

    public static final AttributeExchange GENDER = register("gender",
            "http://axschema.org/person/gender", "Gender");

    public static final AttributeExchange LANGUAGE = register("language",
            "http://axschema.org/pref/language", "Preferred language");

    public static final AttributeExchange LAST_NAME = register("last",
            "http://axschema.org/namePerson/last", "Surname");

    public static final AttributeExchange TIMEZONE = register("timezone",
            "http://axschema.org/pref/timezone", "Preferred timezone");

    public static final AttributeExchange ZIP = register("postcode",
            "http://axschema.org/contact/postalCode/home", "Home zip code");

    private static Map<String, AttributeExchange> getAttributes() {
        if (_attributes == null) {
            _attributes = new HashMap<String, AttributeExchange>();
        }
        return _attributes;
    }

    public static synchronized AttributeExchange register(String name,
            String schema, String description) {

        if (!getAttributes().containsKey(name)) {
            final AttributeExchange ax = new AttributeExchange(name, schema,
                    description);
            getAttributes().put(name, ax);
        }

        return getAttributes().get(name);
    }

    public static AttributeExchange valueOf(String name) {
        AttributeExchange result = null;

        if ((name != null) && !name.equals("")) {
            result = getAttributes().get(name);
        }
        return result;
    }

    public static AttributeExchange valueOfType(String schema) {
        if (schema != null && !schema.equals("")) {
            for (AttributeExchange ax : getAttributes().values()) {
                if (ax.getSchema().equals(schema))
                    return ax;
            }
        }
        return null;
    }

    private volatile String schema;

    public AttributeExchange(String name, String schema, String description) {
        super(name, description);
        this.schema = schema;
    }

    public int compareTo(AttributeExchange o) {
        return this.schema.compareTo(o.getSchema());
        // return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof AttributeExchange) {
            return this.getName()
                    .equals(((AttributeExchange) object).getName());
        }
        return false;
    }

    @Override
    public Metadata getParent() {
        return null;
    }

    public String getSchema() {
        return this.schema;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getSchema());
    }

    @Override
    public boolean includes(Metadata included) {
        return this.equals(included);
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
