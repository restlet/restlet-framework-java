package org.restlet.ext.odata.internal;
import java.util.List;

import org.restlet.representation.Representation;


/**
 * The interface for parsing the representation result provided by RESLET as a response in the 
 * respective object/ return type for the functions/actions.
 * 
 * @author Shantanu
 *
 */
public interface FunctionContentHandler {
	
	/**
	 * Parses the result.
	 *
	 * @param classType - Class<?>
	 * @param representation - Representation
	 * @param functionName - Name of the function
	 * @param returnType - The return type
	 * @return expected result object for a function or action
	 */
	Object parseResult(Class<?> classType, Representation representation, String functionName, List<?> returnType);
}
