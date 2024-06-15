package org.restlet.client.resource;

public interface ExceptionHandler<E extends Throwable> {
    void handle(E throwable);
}
