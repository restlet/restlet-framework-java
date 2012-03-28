package org.restlet.test.resource;

public class GenericServerResource16<E> extends
        AbstractGenericAnnotatedServerResource<E> {

    @Override
    public E addResponse(E representation) {
        return representation;
    };

}
