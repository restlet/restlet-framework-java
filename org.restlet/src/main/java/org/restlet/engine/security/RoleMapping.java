/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.security;

import org.restlet.security.Group;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Mapping from an organization or a user or a group to a role.
 * 
 * @author Jerome Louvel
 */
public class RoleMapping {

	/**
	 * The source of the mapping. It must be an instance of one of these classes:
	 * {@link User} or {@link Group}.
	 */
	private volatile Object source;

	/** The target role of the mapping. */
	private volatile Role target;

	/**
	 * Default constructor.
	 */
	public RoleMapping() {
		this(null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param target
	 */
	public RoleMapping(Object source, Role target) {
		super();
		this.source = source;
		this.target = target;
	}

	public Object getSource() {
		return source;
	}

	public Role getTarget() {
		return target;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setTarget(Role target) {
		this.target = target;
	}

}
