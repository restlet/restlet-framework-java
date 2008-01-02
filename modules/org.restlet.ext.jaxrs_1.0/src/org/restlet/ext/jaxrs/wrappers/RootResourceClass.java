/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.wrappers;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.MatchingResult;

/**
 * Instances represents a root resource class.
 * 
 * A resource class annotated with @Path. Root resource classes provide the roots of the
 * resource class tree and provide access to sub-resources, see chapter 2 of JSR-311-Spec.
 * @author Stephan Koops
 *
 */
public class RootResourceClass extends ResourceClass {
	
    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * @param jaxRsClass the root resource class to wrap
     */
	public RootResourceClass(Class jaxRsClass) {
		super(jaxRsClass);
	}

	@Override
	public ResourceObject createInstance(MatchingResult matchingResult, Request restletRequ) throws Exception {
		Object[] args;
		Constructor constr = findConstructor(getJaxRsClass());
		if (constr.getParameterTypes().length == 0)
			args = new Object[0];
		else
			args = getParameterValues(constr.getParameterAnnotations(), constr.getParameterTypes(), matchingResult, restletRequ);
		return new ResourceObject(constr.newInstance(args), this);
	}

	/**
	 * @param jaxRsClass
	 *            a JAX-RS root resource class
	 * @return Returns the constructor to use for the given root resource class (See JSR-311-Spec, section 2.3)
	 */
	private Constructor findConstructor(Class resourceClass) {
		/*
		 * (aus JSR-311-Spec, section 2.3) Root resource classes are instantiated by the JAX-RS runtime and MUST have a constructor with one of the following
		 * annotations on every parameter: @HttpContext, @HeaderParam, @MatrixParam, @Query-Param or @UriParam. Note that a zero argument constructor is
		 * permissible(=erlaubt) under this rule. Section 2.4.1 defines the parameter types permitted for each annotation. If more than one constructor that
		 * matches the above pattern is available then an implementation MUST use the one with the most parameters. Choosing amongst constructors with the same
		 * number of parameters is implementation specific.
		 */

		Collection<Constructor> constructors = Arrays.asList(resourceClass.getConstructors()); // nur die public Constructors
		for (Constructor constr : constructors) { // TODO findConstructor gibt z.Zt. immer den Construktor ohne Argumenten zurück.
			if (constr.getParameterTypes().length == 0)
				return constr;
		}
		/*
		 * Iterator<Constructor> constructorIter = constructors.iterator(); while (constructorIter.hasNext()) { Constructor constructor =
		 * constructorIter.next(); Annotation[][] paramAnnotations = constructor.getParameterAnnotations(); } //
		 */
		return constructors.iterator().next(); // Dummy-Implementation
	}

    @Override
    public boolean equals(Object anotherObject)
    {
        if(this == anotherObject)
            return true;
        if(!(anotherObject instanceof RootResourceClass))
            return false;
        RootResourceClass otherRootResourceClass = (RootResourceClass)anotherObject;
        return this.jaxRsClass.equals(otherRootResourceClass.jaxRsClass);
    }
}