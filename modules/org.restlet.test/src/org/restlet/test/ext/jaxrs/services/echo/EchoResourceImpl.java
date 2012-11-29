package org.restlet.test.ext.jaxrs.services.echo;

import java.awt.Point;


public class EchoResourceImpl implements EchoResource{

	@Override
	public Point echoPointHeaderParam(Point point) {
		return point;
	}

	@Override
	public String echo(String input) {
		return input;
	}

	@Override
	public Point echoPointQueryParam(Point point) {
		return point;
	}

	@Override
	public Point echoPointPathParam(Point point) {
		return point;
	}

	@Override
	public String echoStringRegexPathParam(String input) {
		return input;
	}

	@Override
	public Point echoPointCookieParam(Point point) {
		return point;
	}
	 
}
