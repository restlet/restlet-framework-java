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

package org.restlet.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.model.CallModel;

/**
 * Filter extracting some attributes from a request. Multiple extractions can be defined, based on the query 
 * string of the resource reference, on the request form (posted from a browser), on the context URI matches 
 * or on the request's template model.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ExtractFilter extends Filter
{
	/**
	 * List of query parameters to extract.
	 */
	private List<ExtractInfo> queryExtracts;

	/**
	 * List of request entity parameters to extract.
	 */
	private List<ExtractInfo> entityExtracts;

	/**
	 * List of request's model attributes to extract.
	 */
	private List<ExtractInfo> modelExtracts;

	/**
	 * List of segments to extract.
	 */
	private List<ExtractInfo> segmentExtracts;

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public ExtractFilter(Context context)
	{
		super(context);
		this.queryExtracts = null;
		this.entityExtracts = null;
		this.modelExtracts = null;
		this.segmentExtracts = null;
	}

	/**
	 * Extracts the attributes value from the request. 
	 * @param request The request to process.
	 * @param response The response to process.
	 * @throws IOException
	 */
	protected void extractAttributes(Request request, Response response)
			throws IOException
	{
		// Extract the query parameters
		if (this.queryExtracts != null)
		{
			Form form = request.getResourceRef().getQueryAsForm();

			if (form != null)
			{
				for (ExtractInfo qe : getQueryExtracts())
				{
					if (qe.multiple)
					{
						request.getAttributes().put(qe.attribute, form.subList(qe.value));
					}
					else
					{
						request.getAttributes().put(qe.attribute, form.getFirst(qe.value));
					}
				}
			}
		}

		// Extract the request entity parameters
		if (this.entityExtracts != null)
		{
			Form form = request.getEntityAsForm();

			if (form != null)
			{
				for (ExtractInfo ie : getEntityExtracts())
				{
					if (ie.multiple)
					{
						request.getAttributes().put(ie.attribute, form.subList(ie.value));
					}
					else
					{
						request.getAttributes().put(ie.attribute, form.getFirst(ie.value));
					}
				}
			}
		}

		// Extract the model patterns
		if (this.modelExtracts != null)
		{
			CallModel model = new CallModel(request, response, null);
			for (ExtractInfo me : getModelExtracts())
			{
				request.getAttributes().put(me.attribute, model.get(me.value));
			}
		}

		// Extract the segments
		if (this.segmentExtracts != null)
		{
			List<String> segments = request.getBaseRef().getSegments();
			for (ExtractInfo me : getSegmentExtracts())
			{
				if (me.index >= 0)
				{
					request.getAttributes().put(me.attribute, segments.get(me.index));
				}
				else if (me.index < 0)
				{
					request.getAttributes().put(me.attribute,
							segments.get(segments.size() + me.index));
				}
			}
		}
	}

	/**
	 * Allows filtering before its handling by the target Restlet. Does nothing by default.
	 * @param request The request to filter.
	 * @param response The response to filter.
	 */
	public void beforeHandle(Request request, Response response)
	{
		try
		{
			extractAttributes(request, response);
		}
		catch (Exception e)
		{
			getLogger().log(Level.SEVERE, "Unhandled error intercepted", e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	/**
	 * Returns the list of query extracts.
	 * @return The list of query extracts.
	 */
	private List<ExtractInfo> getQueryExtracts()
	{
		if (this.queryExtracts == null) this.queryExtracts = new ArrayList<ExtractInfo>();
		return this.queryExtracts;
	}

	/**
	 * Returns the list of query extracts.
	 * @return The list of query extracts.
	 */
	private List<ExtractInfo> getEntityExtracts()
	{
		if (this.entityExtracts == null)
			this.entityExtracts = new ArrayList<ExtractInfo>();
		return this.entityExtracts;
	}

	/**
	 * Returns the list of query extracts.
	 * @return The list of query extracts.
	 */
	private List<ExtractInfo> getModelExtracts()
	{
		if (this.modelExtracts == null) this.modelExtracts = new ArrayList<ExtractInfo>();
		return this.modelExtracts;
	}

	/**
	 * Returns the list of segment extracts.
	 * @return The list of segment extracts.
	 */
	private List<ExtractInfo> getSegmentExtracts()
	{
		if (this.segmentExtracts == null)
			this.segmentExtracts = new ArrayList<ExtractInfo>();
		return this.segmentExtracts;
	}

	/**
	 * Extracts an attribute from the query string of the resource reference. Only the first occurrence
	 * of a query string parameter is set.
	 * @param attributeName The name of the call attribute to set.
	 * @param parameterName The name of the query string parameter to extract.
	 * @return The current Filter.
	 */
	public ExtractFilter fromQuery(String attributeName, String parameterName)
	{
		return fromQuery(attributeName, parameterName, false);
	}

	/**
	 * Extracts an attribute from the query string of the resource reference.
	 * @param attributeName The name of the call attribute to set.
	 * @param parameterName The name of the query string parameter to extract.
	 * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
	 * @return The current Filter.
	 */
	public ExtractFilter fromQuery(String attributeName, String parameterName,
			boolean multiple)
	{
		getQueryExtracts().add(new ExtractInfo(attributeName, parameterName, multiple));
		return this;
	}

	/**
	 * Extracts an attribute from the request entity form. Only the first occurrence
	 * of a query string parameter is set.
	 * @param attributeName The name of the call attribute to set.
	 * @param parameterName The name of the entity form parameter to extract.
	 * @return The current Filter.
	 */
	public ExtractFilter fromEntity(String attributeName, String parameterName)
	{
		return fromEntity(attributeName, parameterName, false);
	}

	/**
	 * Extracts an attribute from the request entity form.
	 * @param attributeName The name of the call attribute to set.
	 * @param parameterName The name of the entity form parameter to extract.
	 * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
	 * @return The current Filter.
	 */
	public ExtractFilter fromEntity(String attributeName, String parameterName,
			boolean multiple)
	{
		getEntityExtracts().add(new ExtractInfo(attributeName, parameterName, multiple));
		return this;
	}

	/**
	 * Extracts an attribute from the call's model.
	 * @param attributeName The name of the call attribute to set.
	 * @param pattern The model pattern to resolve.
	 * @return The current Filter.
	 * @see org.restlet.ext.model.CallModel
	 */
	public ExtractFilter fromModel(String attributeName, String pattern)
	{
		getModelExtracts().add(new ExtractInfo(attributeName, pattern));
		return this;
	}

	/**
	 * Extracts a segment from the request's base reference. If the offset is equal to zero 
	 * or positive, it corresponds to the index of a start segment. If the offset is
	 * negative, it corresponds to an end segment (index == size + offset).
	 * @param attributeName The name of the call attribute to set.
	 * @param offset The position of the segment.
	 * @return The current Filter.
	 */
	public ExtractFilter fromSegment(String attributeName, int offset)
	{
		getSegmentExtracts().add(new ExtractInfo(attributeName, offset));
		return this;
	}

	/**
	 * Internal class holding extraction information.
	 */
	protected class ExtractInfo
	{
		/**
		 * Holds the attribute name.
		 */
		protected String attribute;

		/**
		 * Holds information to extract the attribute value.
		 */
		protected String value;

		/**
		 * Holds indicator on how to handle repeating values.
		 */
		protected boolean multiple;

		/**
		 * Holds information to extract the attribute value.
		 */
		protected int index;

		/**
		 * Constructor.
		 * @param attribute
		 * @param value
		 * @param multiple
		 */
		public ExtractInfo(String attribute, String value, boolean multiple)
		{
			this.attribute = attribute;
			this.value = value;
			this.multiple = multiple;
			this.index = -1;
		}

		/**
		 * Constructor.
		 * @param attribute
		 * @param index
		 */
		public ExtractInfo(String attribute, int index)
		{
			this.attribute = attribute;
			this.value = null;
			this.multiple = false;
			this.index = index;
		}

		/**
		 * Constructor.
		 * @param attribute
		 * @param value
		 */
		public ExtractInfo(String attribute, String value)
		{
			this.attribute = attribute;
			this.value = value;
			this.multiple = false;
			this.index = -1;
		}
	}

}
