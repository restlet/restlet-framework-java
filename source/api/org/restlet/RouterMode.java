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
 * Enumeration of available router modes.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public enum RouterMode
{
	/**
	 * Each call will be routed to the next attachment if the required score is reached. The next attachment is
	 * relative to the previous call routed. If the required score is not reached, then the attachment is 
	 * skipped and the next one is considered. If the last attachment is reached, the first attachment will 
	 * be considered.  
	 */
	ROUND_ROBIN,
	
	/**
	 * Each call will be routed to the first attachment if the required score is reached. If the required score 
	 * is not reached, then the attachment is skipped and the next one is considered. 
	 */
	FIRST_MATCH,
	
	/**
	 * Each call will be routed to the last attachment if the required score is reached. If the required score 
	 * is not reached, then the attachment is skipped and the previous one is considered. 
	 */
	LAST_MATCH,
	
	/**
	 * Each call will be routed to the attachment with the best score, if the required score is reached.
	 */
	BEST_MATCH,
	
	/**
	 * Each call will be randomly routed to one of the attachments that reached the required score.
	 */
	RANDOM,
	
	/**
	 * Each call will be routed according to a custome mode.
	 */
	CUSTOM;	
}
