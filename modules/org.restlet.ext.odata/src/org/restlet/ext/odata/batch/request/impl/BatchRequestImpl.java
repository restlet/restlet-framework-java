package org.restlet.ext.odata.batch.request.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.batch.request.BatchRequest;
import org.restlet.ext.odata.batch.request.ChangeSetRequest;
import org.restlet.ext.odata.batch.request.ClientBatchRequest;
import org.restlet.ext.odata.batch.response.BatchResponse;
import org.restlet.ext.odata.batch.util.BatchConstants;
import org.restlet.ext.odata.batch.util.BodyPart;
import org.restlet.ext.odata.batch.util.Multipart;
import org.restlet.ext.odata.batch.util.RestletBatchRequestHelper;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * The Class BatchRequestImpl forms the base class for batch request. <br>
 * It maintains the list of clientBatchRequests within a batch.
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class BatchRequestImpl implements BatchRequest {

	/** The service. */
	private Service service;

	/** The requests. */
	private List<ClientBatchRequest> requests = new ArrayList<ClientBatchRequest>();

	/**
	 * Instantiates a new batch request impl.
	 * 
	 * @param service
	 *            the service
	 */
	public BatchRequestImpl(Service service) {
		this.service = service;
	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchRequest#addRequest(org.restlet.ext.odata.batch.request.impl.GetEntityRequest)
	 */
	@Override
	public BatchRequest addRequest(GetEntityRequest getEntityRequest) {
		requests.add(getEntityRequest);
		return this;

	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchRequest#addRequest(org.restlet.ext.odata.batch.request.ChangeSetRequest)
	 */
	@Override
	public BatchRequest addRequest(ChangeSetRequest changeSetRequest) {
		requests.add(changeSetRequest);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.BatchRequest#execute()
	 */
	@Override
	public List<BatchResponse> execute() {

		String batchId = generateBatchId();
		
		ClientResource clientResource = service.createResource(new Reference(
				service.getServiceRef()));
		Reference resourceRef = clientResource.getRequest().getResourceRef();		
		
		// create the client Info
		setClientContext(clientResource, resourceRef);
		
		StringBuilder sb = createBatchString(batchId, this.requests);
		//Finally posting the batch request.
		Representation r = clientResource.post(new StringRepresentation(sb
				.toString(), new MediaType(MediaType.MULTIPART_MIXED
				+ ";boundary=" + batchId)));
		
		List<BatchResponse> batchResponses = null;
		try {
			batchResponses = parseRepresentation(r, this.requests);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return batchResponses;
	}

	/**
	 * Creates the batch request string,which would then be converted into String Representation for further processing.
	 * 
	 * @param batchId
	 * @param list
	 * @return
	 */
	private StringBuilder createBatchString(String batchId,
			List<ClientBatchRequest> list) {
		StringBuilder sb = new StringBuilder();


		for (ClientBatchRequest restletBatchRequest : list) {
			if (restletBatchRequest instanceof GetEntityRequest) {
				sb.append(BatchConstants.NEW_LINE_BATCH_START).append(batchId).append(BatchConstants.NEW_LINE);
				sb.append(restletBatchRequest.format(MediaType.APPLICATION_ATOM));
			} else if (restletBatchRequest instanceof ChangeSetRequest) {
				sb.append(BatchConstants.NEW_LINE_BATCH_START).append(batchId).append(BatchConstants.NEW_LINE);
				sb.append(restletBatchRequest.format(MediaType.APPLICATION_ATOM));
			}
		}
		sb.append(BatchConstants.NEW_LINE_BATCH_START).append(batchId).append(BatchConstants.NEW_LINE_BATCH_END);
		return sb;
	}

	/**
	 * Sets the client information and the context onto client resource.
	 * @param clientResource
	 * @param resourceRef
	 */
	private void setClientContext(ClientResource clientResource,
			Reference resourceRef) {
		ClientInfo clientInfo = new ClientInfo();
		clientResource.getRequest().setClientInfo(clientInfo);

		Context context = new Context();
		Client client = new Client(context, Protocol.HTTP);
		client.getContext().getParameters()
				.add("useForwardedForHeader", "false");

		
		clientResource.getRequest().setResourceRef(
				new Reference(resourceRef.getTargetRef()
						+ BatchConstants.BATCH_ENDPOINT_URI));
		clientResource.getRequest().setMethod(Method.POST);
		clientResource.setNext(client);
	}

	/**
	 * Generates a unique batch Id for each batch request.
	 * @return
	 */
	private String generateBatchId() {
		String batchId = BatchConstants.BATCH_UNDERSCORE
				+ UUID.randomUUID().toString();
		return batchId;
	}

	/**
	 * This method parses the representation and returns the list of batch
	 * responses.
	 * 
	 * @param r
	 *            representation
	 * @param list
	 *            the list
	 * @return list of batchResponses.
	 * @throws IOException
	 */
	private List<BatchResponse> parseRepresentation(Representation r,
			List<ClientBatchRequest> list) throws IOException {

		// This would hold individual batch responses
		List<BatchResponse> batchResultList = new ArrayList<BatchResponse>(
				list.size());

		MediaType mediaType = r.getMediaType();
		Multipart baseMultiPart = RestletBatchRequestHelper.createMultipart(
				r.getStream(), mediaType);
		int i = 0;
		BatchResponse bResponse = null;
		List<BodyPart> subBodyParts = baseMultiPart.getBodyParts();
		for (BodyPart bp : subBodyParts) {
			// Its a changeset
			if (bp.getMediaType().isCompatible(MediaType.MULTIPART_MIXED)) {
				Multipart mp = RestletBatchRequestHelper.createMultipart(
						bp.getInputStream(), bp.getMediaType());
				List<String> contentList = new ArrayList<String>();
				List<BodyPart> bodyParts = mp.getBodyParts();
				for (BodyPart bodyPart : bodyParts) {
					contentList
							.add(RestletBatchRequestHelper
									.getStringFromInputStream(bodyPart
											.getInputStream()));
				}
				ChangeSetRequest csr = (ChangeSetRequest) list.get(i);
				bResponse = RestletBatchRequestHelper.parseChangeSetResponse(
						BatchConstants.ODATA_VERSION_V3, contentList, csr,
						mediaType, service);
			} else {
				ClientBatchRequest batchRequestOfTypeGet = list.get(i);
				String content = RestletBatchRequestHelper
						.getStringFromInputStream(bp.getInputStream());
				bResponse = RestletBatchRequestHelper
						.parseSingleOperationResponse(
								BatchConstants.ODATA_VERSION_V3, content,
								batchRequestOfTypeGet, bp.getMediaType(),
								service);
			}
			batchResultList.add(bResponse);
			i++;
		}
		return batchResultList;

	}

}
