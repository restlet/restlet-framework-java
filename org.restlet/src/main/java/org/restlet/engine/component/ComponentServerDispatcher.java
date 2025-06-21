/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.component;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.util.TemplateDispatcher;

/**
 * Component server dispatcher.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class ComponentServerDispatcher extends TemplateDispatcher {

	/** The component context. */
	private ComponentContext componentContext;

	/**
	 * Constructor.
	 * 
	 * @param componentContext The component context.
	 */
	public ComponentServerDispatcher(ComponentContext componentContext) {
		this.componentContext = componentContext;
	}

	@Override
	public int beforeHandle(Request request, Response response) {
		int result = super.beforeHandle(request, response);

		// This causes the baseRef of the resource reference to be set
		// as if it had actually arrived from a server connector.
		request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());

		return result;
	}

	@Override
	protected int doHandle(Request request, Response response) {
		int result = CONTINUE;
		// Ask the server router to actually handle the call
		getComponentContext().getComponentHelper().getServerRouter().handle(request, response);

		return result;
	}

	/**
	 * Returns the component context.
	 * 
	 * @return The component context.
	 */
	private ComponentContext getComponentContext() {
		return componentContext;
	}

}
