package org.restlet.test.resource;

public class GenericAnnotatedServerResource<E> extends
        AbstractGenericAnnotatedServerResource<E> {

    public E addResponse(E representation) {
        return representation;
    }
}
