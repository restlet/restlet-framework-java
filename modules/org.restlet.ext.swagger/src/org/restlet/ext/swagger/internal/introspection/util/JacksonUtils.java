package org.restlet.ext.platform.internal.introspection.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.util.Locale;

@Deprecated
public class JacksonUtils {

    public static JsonIgnoreProperties getJsonIgnoreProperties(Class<?> clazz) {
        return AnnotatedClassResolver.resolve(serializationConfig, TypeFactory.defaultInstance().constructType(clazz), null)
                .getAnnotation(JsonIgnoreProperties.class);
    }

    private static final SerializationConfig serializationConfig = getSerializationConfig();

    private static SerializationConfig getSerializationConfig() {
        SimpleMixInResolver mixins = new SimpleMixInResolver(null);
        BaseSettings DEFAULT_BASE = new BaseSettings(
                null, // cannot share global ClassIntrospector any more (2.5+)
                new JacksonAnnotationIntrospector(),
                null, TypeFactory.defaultInstance(),
                null, StdDateFormat.instance, null,
                Locale.getDefault(),
                null, // to indicate "use Jackson default TimeZone" (UTC since Jackson 2.7)
                Base64Variants.getDefaultVariant(), // 2.1
                null,
        null,
             null
        );
        BaseSettings base = DEFAULT_BASE.withClassIntrospector(new BasicClassIntrospector());
        return new SerializationConfig(base, new StdSubtypeResolver(), mixins, new RootNameLookup(), new ConfigOverrides());
    }

}
