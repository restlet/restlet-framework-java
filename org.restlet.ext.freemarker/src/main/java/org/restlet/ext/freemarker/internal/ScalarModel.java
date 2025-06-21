/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.freemarker.internal;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Data model that gives access to a Object value.
 * 
 * @author Jerome Louvel
 */
class ScalarModel implements TemplateScalarModel {
    /** The inner value. */
    private final Object value;

    /**
     * Constructor.
     * 
     * @param value
     *            the provided value of this scalar model.
     */
    public ScalarModel(Object value) {
        super();
        this.value = value;
    }

    public String getAsString() throws TemplateModelException {
        return this.value.toString();
    }
}
