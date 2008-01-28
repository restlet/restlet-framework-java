package org.restlet.ext.jaxrs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.util.Series;

class FormMulvaltivaluedMap implements MultivaluedMap<String, String> {
    // TODO TESTEN: soll Case-insensitiv sein
    // LATER FormMultivaluedMap kann nocht nicht alles aus der Map
    FormMulvaltivaluedMap(Form form) {
        this.form = form;
    }

    private Form form;

    public String getFirst(String key) {
        return form.getFirstValue(key, true);
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof String))
            return false;
        return form.getFirst((String) key, true) != null;
    }

    public boolean containsValue(Object value) {
        for (Parameter p : form)
            if (Util.equals(p.getValue(), value))
                return true;
        return false;
    }

    public Set<java.util.Map.Entry<String, List<String>>> entrySet() {
        // TODO 
        throw new NotYetImplementedException();
    }

    public List<String> get(Object key) {
        if (!(key instanceof String))
            return null;
        Series<Parameter> subForm = form.subList((String)key, true);
        List<String> list = new ArrayList<String>();
        for(Parameter parameter : subForm)
            list.add(parameter.getName());
        return list;
    }

    public boolean isEmpty() {
        return form.isEmpty();
    }

    public Set<String> keySet() {
        // TODO
        throw new NotYetImplementedException();
    }

    public int size() {
        return form.size(); // LATER stimmt vielleicht nicht, wenn
        // es Elemente mehrfach gibt
    }

    public Collection<List<String>> values() {
        // TODO
        throw new NotYetImplementedException();
    }

    // gefragt: JSR311: Should the MultivaluedMap unmodifiable for the Query and
    // Template Paramaters?

    @Deprecated
    @SuppressWarnings("unused")
    public void add(String key, String value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void putSingle(String key, String value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void clear() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public List<String> put(String key, List<String> value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void putAll(Map<? extends String, ? extends List<String>> t)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public List<String> remove(Object key) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }
}