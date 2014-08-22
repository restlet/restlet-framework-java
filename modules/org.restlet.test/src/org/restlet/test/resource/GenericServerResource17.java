package org.restlet.test.resource;

import org.restlet.resource.ServerResource;

public class GenericServerResource17<E> extends ServerResource implements
        MyResource17<E> {

    public E add(E rep) {
        return rep;
    };

}
