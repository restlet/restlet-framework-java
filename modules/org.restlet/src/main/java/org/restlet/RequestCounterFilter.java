package org.restlet;

import org.restlet.routing.Filter;

import com.codahale.metrics.Counter;

public class RequestCounterFilter extends Filter{

	private final Counter counter;
	
	private final String name;
	
	public RequestCounterFilter(String name) {
		
		this.name = name;
		this.counter = new Counter();
		
		Metrics.REGISTRY.remove(name);
		Metrics.REGISTRY.register(name, counter);
	}
	
	@Override
	protected int beforeHandle(Request request, Response response) {
		counter.inc();
		return super.beforeHandle(request, response);
	}
	
}
