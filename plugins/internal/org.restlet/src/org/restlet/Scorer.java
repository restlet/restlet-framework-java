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
 * Scoring handler typically attached to a Router. It is also able to score calls indicating its affinity. 
 * The score is typically used by the Router in order to determine the best handler for a given call. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Scorer extends Chainer
{
	/** The parent router. */
	protected Router router;
	
   /** The Restlet target. */
   protected Restlet target;

   /**
    * Constructor.
    * @param router The parent router.
    * @param target The Restlet target.
    */
   public Scorer(Router router, Restlet target)
   {
   	super(router == null ? null : router.getContext());
   	this.router = router;
      this.target = target;
   }
	
	/**
	 * Returns the score for a given call (between 0.0 and 1.0).
	 * @param call The call to score.
	 * @return The score for a given call (between 0.0 and 1.0).
	 */
	public float score(Call call)
	{
		return 0.0F;
	}

   /**
	 * Returns the parent router.
	 * @return The parent router.
	 */
	public Router getRouter()
	{
		return this.router;
	}

	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		return this.target;
	}

}
