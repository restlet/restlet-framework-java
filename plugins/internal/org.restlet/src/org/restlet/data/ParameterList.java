/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.util.WrapperList;

/**
 * Modifiable list of parameters with many helper methods. Note that this class implements the java.util.List
 * interface using the Parameter class as the template type. This allows you to use an instance of this class
 * as any other java.util.List, in particular all the helper methods in java.util.Collections.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see java.util.Collections
 * @see java.util.List
 */
public class ParameterList extends WrapperList<Parameter>
{
	/**
	 * Constructor.
	 */
	public ParameterList()
	{
		super();
	}

	/**
	 * Constructor.
	 * @param initialCapacity The initial list capacity.
	 */
	public ParameterList(int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Constructor.
	 * @param delegate The delegate list.
	 */
	public ParameterList(List<Parameter> delegate)
	{
		super(delegate);
	}

	/**
	 * Creates then adds a parameter at the end of the list.
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(String name, String value)
	{
		return add(new Parameter(name, value));
	}

	/**
	 * Tests the equality of two string, potentially null, which a case sensitivity flag.  
	 * @param value1 The first value.
	 * @param value2 The second value.
	 * @param ignoreCase Indicates if the test should be case insensitive.
	 * @return True if both values are equal.
	 */
	private boolean equals(String value1, String value2, boolean ignoreCase)
	{
		boolean result = (value1 == value2);

		if (!result)
		{
			if ((value1 != null) && (value2 != null))
			{
				if (ignoreCase)
				{
					result = value1.equalsIgnoreCase(value2);
				}
				else
				{
					result = value1.equals(value2);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the first parameter found with the given name. 
	 * @param name The parameter name (case sensitive).
	 * @return The first parameter found with the given name.
	 */
	public Parameter getFirst(String name)
	{
		return getFirst(name, false);
	}

	/**
	 * Returns the first parameter found with the given name. 
	 * @param name The parameter name.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The first parameter found with the given name.
	 */
	public Parameter getFirst(String name, boolean ignoreCase)
	{
		for (Parameter param : this)
		{
			if (equals(param.getName(), name, ignoreCase))
			{
				return param;
			}
		}

		return null;
	}

	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name (case sensitive).
	 * @return The value of the first parameter found with the given name.
	 */
	public String getFirstValue(String name)
	{
		return getFirstValue(name, false);
	}

	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name (case sensitive).
	 * @param defaultValue The default value to return if no matching parameter found.
	 * @return The value of the first parameter found with the given name or the default value.
	 */
	public String getFirstValue(String name, String defaultValue)
	{
		return getFirstValue(name, false, defaultValue);
	}

	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The value of the first parameter found with the given name.
	 */
	public String getFirstValue(String name, boolean ignoreCase)
	{
		return getFirstValue(name, ignoreCase, null);
	}

	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @param defaultValue The default value to return if no matching parameter found.
	 * @return The value of the first parameter found with the given name or the default value.
	 */
	public String getFirstValue(String name, boolean ignoreCase, String defaultValue)
	{
		String result = defaultValue;
		Parameter param = getFirst(name, ignoreCase);

		if (param != null)
		{
			result = param.getValue();
		}

		return result;
	}

	/**
	 * Returns the values of the parameters with a given name. If multiple parameters with the same name are 
	 * found, all values are concatenated and separated by a comma (like for HTTP message headers). 
	 * @param name The parameter name (case insensitive).
	 * @return The values of the parameters with a given name.
	 */
	public String getValues(String name)
	{
		return getValues(name, ",", true);
	}

	/**
	 * Returns the set of parameter names (case sensitive).
	 * @return The set of parameter names.
	 */
	public Set<String> getNames()
	{
		Set<String> result = new HashSet<String>();

		for (Parameter param : this)
		{
			result.add(param.getName());
		}

		return result;
	}

	/**
	 * Returns the parameter values with a given name. If multiple parameters with the same name are found, 
	 * all values are concatenated and separated by the given separator.
	 * @param name The parameter name.
	 * @param separator The separator character.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The sequence of values.
	 */
	public String getValues(String name, String separator, boolean ignoreCase)
	{
		String result = null;
		StringBuilder sb = null;

		for (Parameter param : this)
		{
			if (param.getName().equalsIgnoreCase(name))
			{
				if (sb == null)
				{
					if (result == null)
					{
						result = param.getValue();
					}
					else
					{
						sb = new StringBuilder();
						sb.append(result).append(separator).append(param.getValue());
					}
				}
				else
				{
					sb.append(separator).append(param.getValue());
				}
			}
		}

		if (sb != null)
		{
			result = sb.toString();
		}

		return result;
	}

	/**
	 * Removes all the parameters with a given name.
	 * @param name The parameter name (case sensitive).
	 * @return True if the list changed.
	 */
	public boolean removeAll(String name)
	{
		return removeAll(name, false);
	}

	/**
	 * Removes all the parameters with a given name.
	 * @param name The parameter name.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return True if the list changed.
	 */
	public boolean removeAll(String name, boolean ignoreCase)
	{
		boolean changed = false;
		Parameter param = null;

		for (Iterator<Parameter> iter = iterator(); iter.hasNext();)
		{
			param = iter.next();
			if (equals(param.getName(), name, ignoreCase))
			{
				iter.remove();
				changed = true;
			}
		}

		return changed;
	}

	/**
	 * Replaces the value of the first parameter with the given name
	 * and removes all other parameters with the same name.
	 * @param name The parameter name.
	 * @param value The value to set.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The parameter set or added.
	 */
	public Parameter set(String name, String value, boolean ignoreCase)
	{
		Parameter result = null;
		Parameter param = null;
		boolean found = false;

		for (Iterator<Parameter> iter = iterator(); iter.hasNext();)
		{
			param = iter.next();

			if (equals(param.getName(), name, ignoreCase))
			{
				if (found)
				{
					// Remove other parameters with the same name
					iter.remove();
				}
				else
				{
					// Change the value of the first matching parameter
					found = true;
					param.setValue(value);
					result = param;
				}
			}
		}

		if (!found)
		{
			add(name, value);
		}

		return result;
	}

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, 
	 * inclusive, and toIndex, exclusive.
	 * @param fromIndex The start position.
	 * @param toIndex The end position (exclusive).
	 * @return The sub-list.
	 */
	public ParameterList subList(int fromIndex, int toIndex)
	{
		return new ParameterList(getDelegate().subList(fromIndex, toIndex));
	}

	/**
	 * Returns a list of all the values associated to the parameter name.
	 * @param name The parameter name (case sensitive).
	 * @return The list of values.
	 */
	public ParameterList subList(String name)
	{
		return subList(name, false);
	}

	/**
	 * Returns a list of all the values associated to the parameter name.
	 * @param name The parameter name.
	 * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The list of values.
	 */
	public ParameterList subList(String name, boolean ignoreCase)
	{
		ParameterList result = new ParameterList();

		for (Parameter param : this)
		{
			if (equals(param.getName(), name, ignoreCase))
			{
				result.add(param);
			}
		}

		return result;
	}

	/**
	 * Copies the parameters whose name is a key in the given map.<br/>
	 * If a matching parameter is found, its value is put in the map.<br/>
	 * If multiple values are found, a list is created and set in the map.
	 * @param params The map controlling the copy.
	 */
	@SuppressWarnings("unchecked")
	public void copyTo(Map<String, Object> params)
	{
		Parameter param;
		Object currentValue = null;
		for (Iterator<Parameter> iter = iterator(); iter.hasNext();)
		{
			param = iter.next();

			if (params.containsKey(param.getName()))
			{
				currentValue = params.get(param.getName());

				if (currentValue != null)
				{
					List<Object> values = null;

					if (currentValue instanceof List)
					{
						// Multiple values already found for this parameter
						values = (List<Object>) currentValue;
					}
					else
					{
						// Second value found for this parameter
						// Create a list of values
						values = new ArrayList<Object>();
						values.add(currentValue);
						params.put(param.getName(), values);
					}

					if (param.getValue() == null)
					{
						values.add(new EmptyValue());
					}
					else
					{
						values.add(param.getValue());
					}
				}
				else
				{
					if (param.getValue() == null)
					{
						params.put(param.getName(), new EmptyValue());
					}
					else
					{
						params.put(param.getName(), param.getValue());
					}
				}
			}
		}
	}

}
