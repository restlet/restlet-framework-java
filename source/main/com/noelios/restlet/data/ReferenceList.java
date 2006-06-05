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

package com.noelios.restlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.restlet.data.MediaTypes;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

/**
 * List of URI references.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ReferenceList implements List<Reference>
{
	/** The list reference. */
	protected Reference listRef;
	
	/** The delegate list. */
	protected List<Reference> delegate;

	/**
	 * Constructor.
	 */
	public ReferenceList()
	{
		this.delegate = null;
	}

	/**
	 * Constructor.
	 * @param initialCapacity The initial list capacity.
	 */
	public ReferenceList(int initialCapacity)
	{
		this(new ArrayList<Reference>(initialCapacity));
	}
	
	/**
	 * Constructor.
	 * @param delegate The delegate list.
	 */
	public ReferenceList(List<Reference> delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * Constructor from a "text/uri-list" representation.
	 * @param uriList The "text/uri-list" representation to parse.
	 * @throws IOException 
	 */
	public ReferenceList(StringRepresentation uriList) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(uriList.getStream()));
		String line = br.readLine();
		
		// Check if the list reference is specified as the first comment
		if(line.startsWith("#"))
		{
			setListRef(new Reference(line.substring(1).trim()));
			line = br.readLine();
		}
		
		while(line != null)
		{
			if(!line.startsWith("#"))
			{
				add(new Reference(line.trim()));
			}
			
			line = br.readLine();
		}
	}
	
	/** 
	 * Returns the list reference.
	 * @return The list reference.
	 */
	public Reference getListRef()
	{
		return this.listRef;
	}

	/** 
	 * Sets the list reference.
	 * @param listRef The list reference.
	 */
	public void setListRef(Reference listRef)
	{
		this.listRef = listRef;
	}

	/**
	 * Returns the delegate list.
	 * @return The delegate list.
	 */
	protected List<Reference> getDelegate()
	{
		if(this.delegate == null)
		{
			this.delegate = new ArrayList<Reference>();
		}
		
		return this.delegate;
	}
	
	/**
	 * Creates then adds a reference at the end of the list.
	 * @param uri The uri of the reference to add.
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(String uri)
	{
		return add(new Reference(uri));
	}
	
	/**
	 * Adds a reference at the end of the list.
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(Reference ref)
	{
		return getDelegate().add(ref);
	}

	/**
	 * Inserts the specified reference at the specified position in this list.
	 * @param index The insertion position.
	 * @param ref The reference to insert.
	 */
	public void add(int index, Reference ref)
	{
		getDelegate().add(index, ref);
	}

	/**
	 * Appends all of the references in the specified collection to the end of this list.
	 * @param refs The collection of references to append.
	 */
	public boolean addAll(Collection<? extends Reference> refs)
	{
		return getDelegate().addAll(refs);
	}

	/**
	 * Inserts all of the references in the specified collection into this list at the specified position.
	 * @param index The insertion position.
	 * @param refs The collection of references to insert.
	 */
	public boolean addAll(int index, Collection<? extends Reference> refs)
	{
		return getDelegate().addAll(index, refs);
	}

	/**
	 * Removes all of the references from this list.
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
	 * Returns the reference at the specified position in this list.
	 * @param index The reference position.
	 * @return The reference at the specified position in this list.
	 */
	public Reference get(int index)
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
	 * Returns an iterator over the references in this list in proper sequence.
	 * @return An iterator over the references in this list in proper sequence.
	 */
	public Iterator<Reference> iterator()
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
	public ListIterator<Reference> listIterator()
	{
		return getDelegate().listIterator();
	}

	/**
	 * Returns a list iterator of the parameters in this list (in proper sequence), starting at the 
	 * specified position in this list.
	 * @param index The starting position.
	 */
	public ListIterator<Reference> listIterator(int index)
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
	public Reference remove(int index)
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
	 * Replaces the reference at the specified position in this list with the specified reference.
	 * @param index The position of the reference to replace.
	 * @param ref The new reference.
	 */
	public Reference set(int index, Reference ref)
	{
		if(this.delegate != null)
		{
			return this.delegate.set(index, ref);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
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
	public ReferenceList subList(int fromIndex, int toIndex)
	{
		return new ReferenceList(getDelegate().subList(fromIndex, toIndex));
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence.
	 * @return An array containing all of the elements in this list in proper sequence.
	 */
	public Reference[] toArray()
	{
		return (Reference[])getDelegate().toArray();
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
	 * Returns a representation of the list in the "text/uri-list" format. 
	 * @return A representation of the list in the "text/uri-list" format.
	 */
	public Representation getRepresentation()
	{
		StringBuilder sb = new StringBuilder();
		
		if(getListRef() != null)
		{
			sb.append("# ").append(getListRef().toString()).append("\r\n");
		}
		
		for(Reference ref : this)
		{
			sb.append(ref.toString()).append("\r\n");
		}
		
		return new StringRepresentation(sb.toString(), MediaTypes.TEXT_URI_LIST);
	}
	
}
