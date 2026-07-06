/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import java.util.Collection;
import java.util.Objects;

/**
 * A list that prevents adding null elements.
 *
 * @param <T>
 */
public class NonNullItemsList<T> extends WrapperList<T> {

    private final String exceptionMessage;

    public NonNullItemsList(final String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public boolean add(final T element) {
        validate(element);
        return super.add(element);
    }

    @Override
    public void add(final int index, final T element) {
        validate(element);
        super.add(index, element);
    }

    @Override
    public void addFirst(final T element) {
        validate(element);
        super.addFirst(element);
    }

    @Override
    public void addLast(final T element) {
        validate(element);
        super.addLast(element);
    }

    @Override
    public boolean addAll(final Collection<? extends T> elements) {
        validate(elements);
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> elements) {
        validate(elements);
        return super.addAll(index, elements);
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof NonNullItemsList<?> that)
                && super.equals(obj)
                && Objects.equals(exceptionMessage, that.exceptionMessage);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void validate(final T element) {
        if (element == null) {
            throw new IllegalArgumentException(this.exceptionMessage);
        }
    }

    private void validate(final Collection<? extends T> elements) {
        boolean addNull = (elements == null);
        if (!addNull) {
            for (T element : elements) {
                if (element == null) {
                    addNull = true;
                    break;
                }
            }
        }
        if (addNull) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }
}
