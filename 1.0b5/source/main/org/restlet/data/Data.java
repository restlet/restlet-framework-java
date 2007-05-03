/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import org.restlet.Element;

/**
 * Element of information handled by a component via a connector.<br/><br/> "A datum is an element of
 * information that is transferred from a component, or received by a component, via a connector." Roy T.
 * Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_3">Source
 * dissertation</a>
 */
public interface Data extends Element
{
}
