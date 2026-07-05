/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.junit.jupiter.api.Test;
import org.restlet.util.Resolver;

class ResolverHashModelTestCase {

    @Test
    void get_withUnresolvedKey_returnsNull() throws TemplateModelException {
        ResolverHashModel model =
                new ResolverHashModel(
                        new Resolver<>() {
                            @Override
                            public Object resolve(String name) {
                                return null;
                            }
                        });
        assertNull(model.get("anyKey"));
    }

    @Test
    void get_withScalarValue_returnsScalarModel() throws TemplateModelException {
        ResolverHashModel model =
                new ResolverHashModel(
                        new Resolver<>() {
                            @Override
                            public Object resolve(String name) {
                                return "value-" + name;
                            }
                        });
        TemplateModel result = model.get("myKey");
        assertEquals("value-myKey", ((TemplateScalarModel) result).getAsString());
    }

    @Test
    void get_withTemplateModelValue_returnsSameInstance() throws TemplateModelException {
        TemplateScalarModel scalar = () -> "already-a-template-model";
        ResolverHashModel model =
                new ResolverHashModel(
                        new Resolver<>() {
                            @Override
                            public Object resolve(String name) {
                                return scalar;
                            }
                        });
        assertSame(scalar, model.get("myKey"));
    }

    @Test
    void isEmpty_alwaysReturnsFalse() throws TemplateModelException {
        ResolverHashModel model =
                new ResolverHashModel(
                        new Resolver<>() {
                            @Override
                            public Object resolve(String name) {
                                return null;
                            }
                        });
        assertFalse(model.isEmpty());
    }
}
