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
import java.util.Map;
import java.util.Set;

/**
 * Modifiable list of parameters with many helper methods. Note that this class implements the java.util.List
 * interface using the Parameter class as the template type. This allows you to use an instance of this class
 * as any other java.util.List, in particular all the helper methods in java.util.Collections.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see java.util.Collections
 * @see java.util.List
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
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(Parameter parameter)
	{
		return getDelegate().add(parameter);
	}

	/**
	 * Inserts the specified parameter at the specified position in this list.
	 * @param index The insertion position.
	 * @param parameter The parameter to insert.
	 */
	public void add(int index, Parameter parameter)
	{
		getDelegate().add(index, parameter);
	}

	/**
	 * Appends all of the parameters in the specified collection to the end of this list.
	 * @param parameters The collection of parameters to append.
	 */
	public boolean addAll(Collection<? extends Parameter> parameters)
	{
		return getDelegate().addAll(parameters);
	}

	/**
	 * Inserts all of the parameters in the specified collection into this list at the specified position.
	 * @param index The insertion position.
	 * @param parameters The collection of parameters to insert.
	 */
	public boolean addAll(int index, Collection<? extends Parameter> parameters)
	{
		return getDelegate().addAll(index, parameters);
	}

	/**
	 * Removes all of the parameters from this list.
	 */
	public void clear()
	{
		if(this.delegate != null)
		{
			getDelegate().clear();
			this.delegate = null;
		}
	}

	/**
	 * Returns true if this list contains the specified element.
	 * @param element The element to find.
	 * @return True if this list contains the specified element.
	 */
	public boolean contains(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.contains(element);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns true if this list contains all of the elements of the specified collection.
	 * @param elements The collection of elements to find.
	 * @return True if this list contains all of the elements of the specified collection.
	 */
	public boolean containsAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.containsAll(elements);
		}
		else
		{
			return false;
		}
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

	/**
	 * Returns the parameter at the specified position in this list.
	 * @param index The parameter position.
	 * @return The parameter at the specified position in this list.
	 */
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
	 * Returns the first parameter found with the given name. 
	 * @param name The parameter name.
    * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The first parameter found with the given name.
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
	 * @param name The parameter name.
    * @param ignoreCase Indicates if the name comparison is case sensitive.
	 * @return The value of the first parameter found with the given name.
	 */
	public String getFirstValue(String name, boolean ignoreCase)
	{
		String result = null;
		Parameter param = getFirst(name);
		
		if(param != null)
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

   /**
    * Returns the index in this list of the first occurrence of the specified element, 
    * or -1 if this list does not contain this element.
    * @param element The element to find.
    * @return The index of the first occurrence.
    */
	public int indexOf(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.indexOf(element);
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Returns true if this list contains no elements.
	 */
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

	/**
	 * Returns an iterator over the parameters in this list in proper sequence.
	 * @return An iterator over the parameters in this list in proper sequence.
	 */
	public Iterator<Parameter> iterator()
	{
		return getDelegate().iterator();
	}

	/**
	 * Returns the index in this list of the last occurrence of the specified element, 
	 * or -1 if this list does not contain this element.
	 */
	public int lastIndexOf(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.lastIndexOf(element);
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Returns a list iterator of the parameters in this list (in proper sequence).
	 * @return A list iterator of the parameters in this list (in proper sequence).
	 */
	public ListIterator<Parameter> listIterator()
	{
		return getDelegate().listIterator();
	}

	/**
	 * Returns a list iterator of the parameters in this list (in proper sequence), starting at the 
	 * specified position in this list.
	 * @param index The starting position.
	 */
	public ListIterator<Parameter> listIterator(int index)
	{
		return getDelegate().listIterator(index);
	}

	/**
	 * Removes the first occurrence in this list of the specified element.
	 * @return True if the list was changed.
	 */
	public boolean remove(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.remove(element);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Removes the parameter at the specified position in this list.
	 * @return The removed parameter.
	 */
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
	
	/**
	 * Removes from this list all the elements that are contained in the specified collection.
	 * @param elements The collection of element to remove.
	 * @return True if the list changed.
	 */
	public boolean removeAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.removeAll(elements);
		}
		else
		{
			return false;
		}
	}

	/**
	 * RemovesRetains only the elements in this list that are contained in the specified collection.
	 * @param elements The collection of element to retain.
	 * @return True if the list changed.
	 */
	public boolean retainAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.retainAll(elements);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Replaces the parameter at the specified position in this list with the specified parameter.
	 * @param index The position of the parameter to replace.
	 * @param parameter The new parameter.
	 */
	public Parameter set(int index, Parameter parameter)
	{
		if(this.delegate != null)
		{
			return this.delegate.set(index, parameter);
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
	
	/**
	 * Returns the number of parameters in this list.
	 * @return The number of parameters in this list.
	 */
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
		
		for(Parameter param : this)
		{
			if(equals(param.getName(), name, ignoreCase))
			{
				result.add(param);
			}
		}
		
		return result;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence.
	 * @return An array containing all of the elements in this list in proper sequence.
	 */
	public Object[] toArray()
	{
		return getDelegate().toArray();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence; 
	 * the runtime type of the returned array is that of the specified array.
	 * @param a The sample array.
	 */
	public <T> T[] toArray(T[] a)
	{
		return getDelegate().toArray(a);
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
      for(Iterator<Parameter> iter = iterator(); iter.hasNext();)
      {
         param = iter.next();
         
         if(params.containsKey(param.getName()))
         {
            currentValue = params.get(param.getName());

            if(currentValue != null)
            {
               List<Object> values = null;

               if(currentValue instanceof List)
               {
                  // Multiple values already found for this parameter
                  values = (List<Object>)currentValue;
               }
               else
               {
                  // Second value found for this parameter
                  // Create a list of values
                  values = new ArrayList<Object>();
                  values.add(currentValue);
                  params.put(param.getName(), values);
               }

               if(param.getValue() == null)
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
               if(param.getValue() == null)
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
