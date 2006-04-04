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

package com.noelios.restlet.ext.atom;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Reference;

/**
 * Either contains or links to the content of the entry.
 */
public class Content
{
	protected MediaType type;
	protected Language language;
	protected Reference baseRef;
	protected String mode;
	protected String source;
	protected String value;
	
	public Content()
	{
		this.type = MediaTypes.TEXT_PLAIN;
	}

}
