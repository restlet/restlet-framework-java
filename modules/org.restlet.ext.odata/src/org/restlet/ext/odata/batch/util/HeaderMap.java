package org.restlet.ext.odata.batch.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A {@link MultivaluedMap} with String keys and values backed by a HashMap.
 * 
 * Although keys are stored case-sensitive, all (internal) comparisons are done
 * case-insensitive. I.e. {@code get("key")} and {@code get("KEY")} return the
 * same values.
 */
public class HeaderMap extends HashMap<String, List<String>> implements
		MultivaluedMap<String, String> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -913599179728662269L;

	@Override
	public void putSingle(String key, String value) {
		List<String> values = new ArrayList<String>();
		values.add(value);
		this.put(key, values);
	}

	@Override
	public void add(String key, String value) {
		List<String> values = this.get(key);
		if (values == null)
			values = new ArrayList<String>();
		values.add(value);
		this.put(key, values);
	}

	@Override
	public String getFirst(String key) {
		List<String> values = this.get(key);
		if (values == null || values.size() == 0)
			return null;
		return values.get(0);
	}

	@Override
	public boolean containsKey(Object key) {
		for (String k : this.keySet())
			if (k.equalsIgnoreCase((String) key))
				return true;
		return false;
	}

	@Override
	public List<String> get(Object key) {
		for (String k : this.keySet())
			if (k.equalsIgnoreCase((String) key))
				return super.get(k);
		return null;
	}

	@Override
	public List<String> put(String key, List<String> value) {
		List<String> previous = this.remove(key);
		super.put(key, value);
		return previous;
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> map) {
		for (Map.Entry<? extends String, ? extends List<String>> e : map
				.entrySet())
			this.put(e.getKey(), e.getValue());
	}

	@Override
	public List<String> remove(Object key) {
		for (String k : this.keySet())
			if (k.equalsIgnoreCase((String) key))
				return super.remove(k);
		return null;
	}
}
