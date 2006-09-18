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

package org.restlet;

/**
 * Chainer to an attached Restlet allowing some filtering. The purpose is to do some pre-processing or 
 * post-processing on the calls going through it before or after they are actually handled by an attached 
 * Restlet. Note that during this processing, the call's context path and resource path are not expected 
 * to be modified. Also note that you can attach and detach targets while handling incoming calls as the filter is ensured to 
 * be thread-safe.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Filters and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Filter extends Chainer
{
	/** The chained Restlet. */
	private Restlet next;

	/**
	 * Constructor.
	 */
	public Filter()
	{
		this(null, null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Filter(Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 * @param next The attached Restlet.
	 */
	public Filter(Context context, Restlet next)
	{
		super(context);
		this.next = next;
	}

	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		return getNext();
	}

	/**
	 * Sets the chained Restlet shared by all calls going through this filter.
	 * @param next The chained Restlet.
	 */
	public void setNext(Restlet next)
	{
		this.next = next;
	}

	/**
	 * Returns the chained Restlet.
	 * @return The chained Restlet or null.
	 */
	public Restlet getNext()
	{
		return this.next;
	}

	/**
	 * Indicates if there is a chained Restlet.
	 * @return True if there is a chained Restlet.
	 */
	public boolean hasNext()
	{
		return getNext() != null;
	}

	/**
	 * Handles a call by first invoking the beforeHandle() method for pre-filtering, then distributing the call 
	 * to the target Restlet via the doHandle() method. When the target handling is completed, it finally 
	 * invokes the afterHandle() method for post-filtering.
	 * @param call The call to handle.
	 */
	public void handle(Call call)
	{
		beforeHandle(call);
		doHandle(call);
		afterHandle(call);
	}

	/**
	 * Allows filtering before its handling by the target Restlet. Does nothing by default.
	 * @param call The call to filter.
	 */
	protected void beforeHandle(Call call)
	{
		// To be overriden
	}

	/**
	 * Handles the call by distributing it to the target handler. 
	 * @param call The call to handle.
	 */
	protected void doHandle(Call call)
	{
		super.handle(call);
	}

	/**
	 * Allows filtering after its handling by the target Restlet. Does nothing by default.
	 * @param call The call to filter.
	 */
	protected void afterHandle(Call call)
	{
		// To be overriden
	}
}
