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

package com.noelios.restlet.test;

import java.io.IOException;

import junit.framework.TestCase;

import com.noelios.restlet.util.HeaderReader;

/**
 * Unit tests for the header.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HeaderTestCase extends TestCase
{
	/**
	 * Tests the parsing.
	 */
	public void testParsing() throws IOException
	{
		String header1 = "Accept-Encoding,User-Agent";
		String header2 = "Accept-Encoding , User-Agent";
		String header3 = "Accept-Encoding,\r\tUser-Agent";
		String header4 = "Accept-Encoding,\r User-Agent";
		String header5 = "Accept-Encoding, \r \t User-Agent";
		String[] values = new String[]
		{ "Accept-Encoding", "User-Agent" };
		testValues(header1, values);
		testValues(header2, values);
		testValues(header3, values);
		testValues(header4, values);
		testValues(header5, values);

		header1 = "Accept-Encoding, Accept-Language, Accept";
		header2 = "Accept-Encoding,Accept-Language,Accept";
		values = new String[]
		{ "Accept-Encoding", "Accept-Language", "Accept" };
		testValues(header1, values);
		testValues(header2, values);
	}

	/**
	 * Test that the parsing of a header returns the given array of values.
	 * @param header The header value to parse.
	 * @param values The parsed values.
	 */
	public void testValues(String header, String[] values)
	{
		try
		{
			HeaderReader hr = new HeaderReader(header);
			String value = hr.readValue();
			int index = 0;
			while (value != null)
			{
				assertEquals(value, values[index]);
				index++;
				value = hr.readValue();
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
