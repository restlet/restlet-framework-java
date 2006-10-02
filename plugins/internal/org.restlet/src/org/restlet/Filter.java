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
 * Chainer to an attached handler that filters calls. The purpose is to do some pre-processing or 
 * post-processing on the calls going through it before or after they are actually handled by an attached 
 * Restlet. Note that during this processing, the call's context path and resource path are not expected 
 * to be modified. Also note that you can attach and detach targets while handling incoming calls as the filter is ensured to 
 * be thread-safe.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Filters and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Filter extends Chainer
{
	/** The next handler. */
	private UniformInterface next;

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
	 * @param next The next handler.
	 */
	public Filter(Context context, UniformInterface next)
	{
		super(context);
		this.next = next;
	}

	/**
	 * Returns the next handler if available.
	 * @param call The current call.
	 * @return The next handler if available or null.
	 */
	public final UniformInterface getNext(Call call)
	{
		return getNext();
	}

	/**
	 * Sets the next handler.
	 * @param next The next handler.
	 */
	public void setNext(UniformInterface next)
	{
		this.next = next;
	}

	/**
	 * Returns the next handler.
	 * @return The next handler or null.
	 */
	public UniformInterface getNext()
	{
		return this.next;
	}

	/**
	 * Indicates if there is a next handler.
	 * @return True if there is a next handler.
	 */
	public boolean hasNext()
	{
		return getNext() != null;
	}

	/**
	 * Handles a call by first invoking the beforeHandle() method for pre-filtering, then distributing the call 
	 * to the next handler via the doHandle() method. When the handling is completed, it finally 
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
	 * Allows filtering before its processing by the next handler. Does nothing by default.
	 * @param call The call to filter.
	 */
	protected void beforeHandle(Call call)
	{
		// To be overriden
	}

	/**
	 * Handles the call by distributing it to the next handler. 
	 * @param call The call to handle.
	 */
	protected void doHandle(Call call)
	{
		super.handle(call);
	}

	/**
	 * Allows filtering after its processing by the next handler. Does nothing by default.
	 * @param call The call to filter.
	 */
	protected void afterHandle(Call call)
	{
		// To be overriden
	}
}
