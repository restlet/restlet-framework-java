/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.component;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

/**
 * Provides the behavior of the internal router of a Component. It overrides the
 * default behavior of a classic Router.
 * 
 * @author Thierry Boileau
 */
public class InternalRouter extends Router {

	/**
	 * Constructor.
	 * 
	 * @param context The current context.
	 */
	public InternalRouter(Context context) {
		super(context);
		// Override Router's default modes
		setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		setRoutingMode(Router.MODE_BEST_MATCH);
	}

	@Override
	protected TemplateRoute createRoute(String uriPattern, Restlet target, int matchingMode) {
		TemplateRoute result = new TemplateRoute(this, uriPattern, target) {
			@Override
			protected int beforeHandle(Request request, Response response) {
				final int result = super.beforeHandle(request, response);

				// Set the request's root reference in order to help the
				// retrieval of the relative reference.
				request.setRootRef(request.getResourceRef().getBaseRef());

				return result;
			}
		};

		result.getTemplate().setMatchingMode(matchingMode);
		result.setMatchingQuery(getDefaultMatchingQuery());
		return result;
	}

	@Override
	public TemplateRoute attach(Restlet target) {
		if (target.getContext() == null) {
			target.setContext(getContext().createChildContext());
		}

		return super.attach(target);
	}

	@Override
	public TemplateRoute attach(String uriPattern, Restlet target) {
		if (target.getContext() == null) {
			target.setContext(getContext().createChildContext());
		}

		return super.attach(uriPattern, target);
	}

	@Override
	public TemplateRoute attachDefault(Restlet defaultTarget) {
		if (defaultTarget.getContext() == null) {
			defaultTarget.setContext(getContext().createChildContext());
		}

		return super.attachDefault(defaultTarget);
	}

	@Override
	public Finder createFinder(Class<? extends ServerResource> targetClass) {
		Finder result = super.createFinder(targetClass);
		result.setContext(getContext().createChildContext());
		return result;
	}

}
