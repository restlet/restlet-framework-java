package org.restlet.test.jaxrs.services.point;

import java.awt.Point;


public class EchoResourceImpl implements EchoResource{

	@Override
	public Point echoPoint(Point point) {
		return point;
	}

	@Override
	public String echo(String input) {
		return input;
	}
	 
}
