package org.restlet.test.jaxrs.services.others;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@ProduceMime("text/*")
public class Jsr250TestProvider implements MessageBodyWriter<Boolean> {
    /**
     * This field is set after {@link #init()} was called.
     */
    public static boolean initiated = false;

    /**
     * This static field contains is set to true, if an provider instance is
     * destroyed, see {@link #preDeytroy()}.
     */
    public static boolean destroyed = false;

    @PostConstruct
    private void init() {
        initiated = true;
    }

    @GET
    public String get() {
        return String.valueOf(initiated);
    }

    @PreDestroy
    private void preDeytroy() {
        destroyed = true;
    }

    public long getSize(Boolean t) {
        if(t == null || t.booleanValue())
            return 4;
        return 5;
    }

    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type);
    }

    public void writeTo(Boolean t, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        entityStream.write("value: ".getBytes());
        entityStream.write(String.valueOf(t).getBytes());
        entityStream.write("\nMessageBodyWriter initiaied: ".getBytes());
        entityStream.write(String.valueOf(initiated).getBytes());
    }
}