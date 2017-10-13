package org.restlet;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

public final class Metrics {

	public static final MetricRegistry REGISTRY = new MetricRegistry();
	
	static {
		JmxReporter.forRegistry(REGISTRY).build().start();
	}
	
}
