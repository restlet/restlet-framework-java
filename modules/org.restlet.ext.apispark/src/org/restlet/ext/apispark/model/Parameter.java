/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Cyprien Quilici
 */
public class Parameter {

	/**
	 * Indicates whether you can provide multiple values for this parameter or
	 * not.
	 */
	private boolean allowMultiple;

	/** Default value of the parameter. */
	private String defaultValue;

	/** Textual description of this parameter. */
	private String description;

	/** Name of the parameter. */
	private String name;

	/**
	 * List of accepted values of the parameter if there is a limited number of
	 * them.
	 */
	private List<String> enumeration;

	/** The expected type of the parameter. By default, string */
	private String type = "string";

	/** Indicates whether the parameter is mandatory or not. */
	private boolean required;

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public List<String> getEnumeration() {
		if (enumeration == null) {
			enumeration = new ArrayList<String>();
		}
		return enumeration;
	}

	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	public boolean isRequired() {
		return required;
	}

	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnumeration(List<String> enumeration) {
		this.enumeration = enumeration;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
