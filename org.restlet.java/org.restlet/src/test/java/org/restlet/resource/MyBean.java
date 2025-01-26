/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import java.io.Serializable;

import org.restlet.engine.util.SystemUtils;

/**
 * Test bean to be serialized.
 * 
 * @author Jerome Louvel
 */
public class MyBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;

    private String name;

    public MyBean() {
    }

    public MyBean(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyBean other = (MyBean) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(name, description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

}
