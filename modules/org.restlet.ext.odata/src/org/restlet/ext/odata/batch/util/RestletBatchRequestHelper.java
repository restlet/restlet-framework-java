package org.restlet.ext.odata.batch.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.jvnet.mimepull.Header;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.batch.request.BatchProperty;
import org.restlet.ext.odata.batch.request.ChangeSetRequest;
import org.restlet.ext.odata.batch.request.ClientBatchRequest;
import org.restlet.ext.odata.batch.request.impl.CreateEntityRequest;
import org.restlet.ext.odata.batch.request.impl.GetEntityRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.response.ChangeSetResponse;
import org.restlet.ext.odata.batch.response.impl.BatchResponseImpl;
import org.restlet.ext.odata.batch.response.impl.ChangeSetResponseImpl;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.xml.AtomFeedHandler;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * The Class RestletBatchRequestHelper is helper class. <br>
 * It has some important functions like parsing changesets responses and
 * formatting single requests. Validating the classname from a metadata before
 * adding the entity. <br>
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class RestletBatchRequestHelper {

	/**
	 * Format single request.
	 * 
	 * Creates a String out of the request and the format type provided.
	 * 
	 * @param req
	 *            the req
	 * @param formatType
	 *            the format type
	 * @return the string
	 */
	public static String formatSingleRequest(Request req, MediaType formatType) {
		
		StringBuilder sb = new StringBuilder();		
		boolean userDefinedContentType = false;
		sb.append(HeaderConstants.HEADER_CONTENT_TYPE).append(": ");
		
		sb.append(MediaType.APPLICATION_ALL).append(BatchConstants.NEW_LINE);
		sb.append(HeaderConstants.HEADER_TRANSFER_ENCODING).append(": ")
				.append("Binary").append(BatchConstants.NEW_LINE);
		sb.append(BatchConstants.NEW_LINE);

		Reference resourceRef = req.getResourceRef();
		String url = resourceRef.getIdentifier();

		// now, adding this request, 1st URL
		sb.append(req.getMethod()).append(" ").append(url)
				.append(" HTTP/1.1\r\n");

		if (!userDefinedContentType
				&& !(req.getMethod().equals(Method.GET) || req.getMethod()
						.equals(Method.DELETE))) {
			
			sb.append(HeaderConstants.HEADER_CONTENT_TYPE).append(": ")
					.append(formatType + BatchConstants.FORMAT_TYPE_CHARSET_UTF8).append(BatchConstants.NEW_LINE);
		}

		return sb.toString();
	}

	/**
	 * Validate and return entity set name.
	 * 
	 * This method validates if a entityType of the entityClass exists in the metadata for the given service.
	 * 
	 * If exists then returns the EntitySetName
	 * <br>else throws an exception.
	 * 
	 * 
	 * @param service
	 *            the service
	 * @param entity
	 *            the entity
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public static String validateAndReturnEntitySetName(Service service,
			Class<?> entityClass) throws Exception {
		Object object = null;
		try {
			object = service.getMetadata();
			return ((Metadata) object).getEntityType(entityClass).getName();
		} catch (SecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new Exception("Can't add entity to this entity set "
					+ entityClass.getName()
					+ " due to  lack of the service's metadata.");
		}
	}
	
	/**
	 * Gets the entity sub path.
	 * 
	 * @param service
	 *            the service
	 * @param entity
	 *            the entity
	 * @return the entity sub path
	 * @throws Exception
	 *             the exception
	 */
	public static String getEntitySubPath(Service service, Object entity)
			throws Exception {
		Object object = null;
		try {
			object = service.getMetadata();
			return ((Metadata) object).getSubpath(entity).replace("/", "");
		} catch (SecurityException e) {
			throw e;
		}
	}

	/**
	 * Gets the string representation.
	 * 
	 * @param service
	 *            the service
	 * @param entitySetName
	 *            the entity set name
	 * @param entry
	 *            the entry
	 * @return the string representation
	 */
	public static StringRepresentation getStringRepresentation(Service service,
			String entitySetName, Entry entry,MediaType type) {
		if (entry != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				entry.write(baos);
				baos.flush();
			} catch (IOException e) {
				new RuntimeException("IOException during creating a string representation"+ e);
			}

			StringRepresentation r = new StringRepresentation(baos.toString(),type);
			return r;
		}
		return null;
	}

	/**
	 * Parses the single operation response.
	 * 
	 * This method parses the response for a single request within the changeset or a singel GET request.
	 * <br> Then creates a batch response and populates the entity by parsing the response output.
	 * 
	 * @param topVersion
	 *            the top version
	 * @param content
	 *            the content
	 * @param so
	 *            the so
	 * @param formatType
	 *            the format type
	 * @param service
	 *            the service
	 * @return the batch response
	 */
	public static BatchResponse parseSingleOperationResponse(String topVersion,
			String content, ClientBatchRequest so, MediaType formatType,
			Service service) {
		// first create a buffered reader
		BufferedReader reader = new BufferedReader(new StringReader(content));
		try {
			// 1st line should be status line line HTTP/1.1 200 OK
			String line = reader.readLine();
			String[] statusLine = line.split("\\s");
			int status = Integer.parseInt(statusLine[1]);

			boolean isHeader = true;
			Map<String, String> headers = new HashMap<String, String>();
			MultivaluedMap<String, String> inboundHeaders = new HeaderMap();
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				// \n\n indicates the end of header for the response
				if (line.isEmpty()) {
					isHeader = false;
					continue;
				}
				if (isHeader) {
					int idx = line.indexOf(":");
					String key = line.substring(0, idx).toUpperCase().trim();
					String value = line.substring(idx + 1).trim();
					headers.put(key, value);
					inboundHeaders.add(key, value);
				} else {
					sb.append(line);
				}
			}

			Object result = null;
			if (inboundHeaders.containsKey(BatchConstants.HTTP_HEADER_CONTENT_TYPE)) {
				if (so instanceof CreateEntityRequest
						|| so instanceof GetEntityRequest) {
					BatchProperty bp = (BatchProperty) so;
					AtomFeedHandler<Object> aFHandler = new AtomFeedHandler<Object>(
							bp.getEntitySetName(), bp.getEntityType(),
							bp.getEntityClass(),
							(Metadata) service.getMetadata());
					result = RestletBatchRequestHelper.getEntity(
							new StringRepresentation(sb.toString()), aFHandler);
				}
			}
			BatchResponseImpl batchResponse = new BatchResponseImpl(status,
					inboundHeaders, result);
			return batchResponse;
		} catch (IOException e) {
			throw new RuntimeException(
					"IOException in ParseSingleOperationResponse", e);
		}

	}

	/**
	 * Parses the change set response.
	 * <br>This method parses the response and from within parses each request within the changeset 
	 * <br> and creates a complete changeset response out of it.
	 * 
	 * @param oDataVersion
	 *            the o data version
	 * @param contentList
	 *            the content list
	 * @param csr
	 *            the csr
	 * @param formatType
	 *            the format type
	 * @param service
	 *            the service
	 * @return the batch response
	 */
	public static BatchResponse parseChangeSetResponse(String oDataVersion,
			List<String> contentList, ChangeSetRequest csr,
			MediaType formatType, Service service) {
		// the change set will return another list of the result
		ChangeSetResponse changeSetResponse = new ChangeSetResponseImpl();
		int j = 0;
		for (String content : contentList) {
			ClientBatchRequest so = csr.getReqs().get(j);
			BatchResponse response = RestletBatchRequestHelper
					.parseSingleOperationResponse(oDataVersion, content, so,
							formatType, service);
			changeSetResponse.add(response);
			j++;
		}
		return changeSetResponse;
	}

	/**
	 * Gets the entity by parsing the representation using the feedhandler sent as a parameter.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param rep
	 *            the rep
	 * @param feedHandler
	 *            the feed handler
	 * @return the entity
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static <T> T getEntity(Representation rep,
			AtomFeedHandler<T> feedHandler) throws IOException {
		Feed feed = new Feed();
		feedHandler.setFeed(feed);
		feedHandler.parse(rep.getReader());
		return feedHandler.getEntities().get(0);
	}

	
	/**
	 * Gets the string from input stream.
	 * 
	 * @param is
	 *            the is
	 * @return the string from input stream
	 */
	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line).append(BatchConstants.NEW_LINE);
			}
		} catch (IOException e) {
			throw new RuntimeException("IOException occured while resding stream"+ e);
		} finally {
			if (br != null) {
				try {
					// br.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Creates the multipart.
	 * 
	 * A Multipart is a logical representation of a batch request or Chnagset.
	 * <br> It is a set of multiple http requests/response. 
	 * 
	 * @param is
	 *            the is
	 * @param mediaType
	 *            the media type
	 * @return the multipart
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Multipart createMultipart(InputStream is, MediaType mediaType)
			throws IOException {
		// create a multipart
		Multipart multipart = new Multipart();
		// set its mediatype
		multipart.setMediaType(mediaType);

		MIMEMessage mimeMessage = new MIMEMessage(is, mediaType.getParameters()
				.getFirstValue(BatchConstants.BATCH_BOUNDARY));
		List<MIMEPart> attachments = mimeMessage.getAttachments();
		for (MIMEPart mimePart : attachments) {
			BodyPart bodyPart = new BodyPart(mimePart);
			// copy headers into bodyparts
			copyHeaders(bodyPart, mimePart);
			bodyPart.setMediaType(new MediaType(bodyPart.getHeaders().getFirst(
					BatchConstants.HTTP_HEADER_CONTENT_TYPE)));
			multipart.addBodyParts(bodyPart);

		}
		return multipart;
	}

	/**
	 * Copies header information from mimePart to body part.
	 * 
	 * @param bodyPart
	 *            the body part
	 * @param mimePart
	 *            the mime part
	 */
	public static void copyHeaders(BodyPart bodyPart, MIMEPart mimePart) {
		MultivaluedMap<String, String> bpHeaders = bodyPart.getHeaders();
		List<? extends Header> mHeaders = mimePart.getAllHeaders();
		for (Header header : mHeaders) {
			bpHeaders.add(header.getName(), header.getValue());
		}
	}

}
