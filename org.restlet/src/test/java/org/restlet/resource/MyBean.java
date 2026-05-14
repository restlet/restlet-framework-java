/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import java.io.Serializable;
import java.util.Objects;
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

    public MyBean() {}

    public MyBean(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MyBean that)) {
            return false;
        }
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getDescription(), that.getDescription());
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
