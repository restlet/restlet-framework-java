/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.BeanInfo;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link BeanInfoUtils}. */
class BeanInfoUtilsTestCase {

    public static class SampleBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class SampleException extends Exception {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Test
    void getBeanInfo_forPlainClass_returnsBeanInfo() {
        BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(SampleBean.class);
        assertNotNull(beanInfo);
    }

    @Test
    void getBeanInfo_forThrowableSubclass_returnsBeanInfo() {
        BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(SampleException.class);
        assertNotNull(beanInfo);
    }

    @Test
    void getBeanInfo_secondCall_returnsCachedInstance() {
        BeanInfo first = BeanInfoUtils.getBeanInfo(SampleBean.class);
        BeanInfo second = BeanInfoUtils.getBeanInfo(SampleBean.class);
        assertSame(first, second);
    }
}
