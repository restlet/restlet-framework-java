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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Multi-usage parameter.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ParameterList implements List<Parameter>, Data
{
	protected List<Parameter> delegate;

	/**
	 * Constructor.
	 */
	public ParameterList()
	{
		this.delegate = null;
	}

	/**
	 * Constructor.
	 * @param initialCapacity The initial list capacity.
	 */
	public ParameterList(int initialCapacity)
	{
		this(new ArrayList<Parameter>(initialCapacity));
	}
	
	/**
	 * Constructor.
	 * @param delegate The delegate list.
	 */
	protected ParameterList(List<Parameter> delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * Returns the delegate list.
	 * @return The delegate list.
	 */
	protected List<Parameter> getDelegate()
	{
		if(this.delegate == null)
		{
			this.delegate = new ArrayList<Parameter>();
		}
		
		return this.delegate;
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
	 * Adds a parameter at the end of the list.
	 */
	public boolean add(Parameter parameter)
	{
		return getDelegate().add(parameter);
	}

	public void add(int index, Parameter element)
	{
		getDelegate().add(index, element);
	}

	public boolean addAll(Collection<? extends Parameter> c)
	{
		return getDelegate().addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Parameter> c)
	{
		return getDelegate().addAll(index, c);
	}

	public void clear()
	{
		if(this.delegate != null)
		{
			getDelegate().clear();
			this.delegate = null;
		}
	}

	public boolean contains(Object o)
	{
		if(this.delegate != null)
		{
			return this.delegate.contains(o);
		}
		else
		{
			return false;
		}
	}

	public boolean containsAll(Collection<?> c)
	{
		if(this.delegate != null)
		{
			return this.delegate.containsAll(c);
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 
	 * @param value1
	 * @param value2
	 * @param ignoreCase
	 * @return
	 */
	private boolean equals(String value1, String value2, boolean ignoreCase)
	{
		boolean result = (value1 == value2);
		
		if(!result)
		{
			if((value1 != null) && (value2 != null))
			{
				if(ignoreCase)
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

	public Parameter get(int index)
	{
		if(this.delegate != null)
		{
			return this.delegate.get(index);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name (case sensitive).
	 * @return The first parameter found with the given name.
	 */
	public Parameter getFirst(String name)
	{
		return getFirst(name, false);
	}
	
	/**
	 * Returns the value of the first parameter found with the given name. 
	 * @param name The parameter name (case sensitive).
    * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The value of the first parameter found with the given name.
	 */
	public Parameter getFirst(String name, boolean ignoreCase)
	{
		for(Parameter param : this)
		{
			if(equals(param.getName(), name, ignoreCase))
			{
				return param;
			}
		}
		
		return null;
	}
	
	/**
    * Returns the header values with a given name. If multiple headers with the same name are found, 
    * all values are concatenated and separated by a comma. 
    * @param name The header name (case insensitive).
	 * @return The header values with a given name.
	 */
	public String getHeader(String name)
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
		
		for(Parameter param : this)
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
   	
   	for(Parameter param : this)
   	{
   		if(param.getName().equalsIgnoreCase(name))
   		{
   			if(sb == null)
   			{
   				if(result == null)
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
   	
   	if(sb != null)
   	{
   		result = sb.toString();
   	}
   	
   	return result;
   }

	public int indexOf(Object o)
	{
		if(this.delegate != null)
		{
			return this.delegate.indexOf(o);
		}
		else
		{
			return -1;
		}
	}

	public boolean isEmpty()
	{
		if(this.delegate != null)
		{
			return this.delegate.isEmpty();
		}
		else
		{
			return true;
		}
	}

	public Iterator<Parameter> iterator()
	{
		return getDelegate().iterator();
	}

	public int lastIndexOf(Object o)
	{
		if(this.delegate != null)
		{
			return this.delegate.lastIndexOf(o);
		}
		else
		{
			return -1;
		}
	}

	public ListIterator<Parameter> listIterator()
	{
		return getDelegate().listIterator();
	}

	public ListIterator<Parameter> listIterator(int index)
	{
		return getDelegate().listIterator(index);
	}

	public boolean remove(Object o)
	{
		if(this.delegate != null)
		{
			return this.delegate.remove(o);
		}
		else
		{
			return false;
		}
	}

	public Parameter remove(int index)
	{
		if(this.delegate != null)
		{
			return this.delegate.remove(index);
		}
		else
		{
			return null;
		}
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
		
		for(Iterator<Parameter> iter = iterator(); iter.hasNext(); )
		{
			param = iter.next();
			if(equals(param.getName(), name, ignoreCase))
			{
				iter.remove();
				changed = true;
			}
		}
		
		return changed;
	}
	
	public boolean removeAll(Collection<?> c)
	{
		if(this.delegate != null)
		{
			return this.delegate.removeAll(c);
		}
		else
		{
			return false;
		}
	}

	public boolean retainAll(Collection<?> c)
	{
		if(this.delegate != null)
		{
			return this.delegate.retainAll(c);
		}
		else
		{
			return false;
		}
	}

	public Parameter set(int index, Parameter element)
	{
		if(this.delegate != null)
		{
			return this.delegate.set(index, element);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
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
		
		for(Iterator<Parameter> iter = iterator(); iter.hasNext();)
		{
			param = iter.next();
			
			if(equals(param.getName(), name, ignoreCase))
			{
				if(found)
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
		
		if(!found)
		{
			add(name, value);
		}
	
		return result;
	}
	
	public int size()
	{
		if(this.delegate != null)
		{
			return this.delegate.size();
		}
		else
		{
			return 0;
		}
	}

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
		
		for(Parameter param : this)
		{
			if(equals(param.getName(), name, ignoreCase))
			{
				result.add(param);
			}
		}
		
		return result;
	}

	public Object[] toArray()
	{
		return getDelegate().toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		return getDelegate().toArray(a);
	}
	
}
