package org.restlet.ext.odata.batch.request.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.restlet.data.MediaType;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.odata.batch.request.ChangeSetRequest;
import org.restlet.ext.odata.batch.request.ClientBatchRequest;
import org.restlet.ext.odata.batch.util.BatchConstants;


/**
 * The Class ChangeSetRequestImpl.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class ChangeSetRequestImpl implements ChangeSetRequest {

	/** The reqs. */
	private List<ClientBatchRequest> reqs = new ArrayList<ClientBatchRequest>();

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ChangeSetRequest#addRequest(org.restlet.ext.odata.batch.request.impl.CreateEntityRequest)
	 */
	@Override
	public ChangeSetRequest addRequest(CreateEntityRequest createEntityRequest) {
		reqs.add(createEntityRequest);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ChangeSetRequest#getReqs()
	 */
	@Override
	public List<ClientBatchRequest> getReqs() {
		return reqs;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ClientBatchRequest#format(org.restlet.data.MediaType)
	 */
	@Override
	public String format(MediaType formatType) {

		StringBuilder sb = new StringBuilder();
		// nothing to add
		if (reqs == null || reqs.size() == 0) {
			return "";
		}

		String boundary = BatchConstants.CHANGESET_UNDERSCORE
				+ UUID.randomUUID().toString();
		String cType = MediaType.MULTIPART_MIXED + "; "
				+ BatchConstants.BATCH_BOUNDARY + "=" + boundary;
		sb.append(HeaderConstants.HEADER_CONTENT_TYPE).append(": ")
				.append(cType).append(BatchConstants.NEW_LINE);
		sb.append(BatchConstants.NEW_LINE);

		
		for (ClientBatchRequest req : reqs) {
			sb.append(BatchConstants.NEW_LINE_BATCH_START).append(boundary).append(BatchConstants.NEW_LINE);
			sb.append(req.format(formatType));
		}

		// ending the change set		
		sb.append(BatchConstants.NEW_LINE_BATCH_START).append(boundary).append(BatchConstants.NEW_LINE_BATCH_END);

		return sb.toString();

	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ChangeSetRequest#addRequest(org.restlet.ext.odata.batch.request.impl.UpdateEntityRequest)
	 */
	@Override
	public ChangeSetRequest addRequest(UpdateEntityRequest updateEntityRequest) {
		reqs.add(updateEntityRequest);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.ext.odata.batch.request.ChangeSetRequest#addRequest(org.restlet.ext.odata.batch.request.impl.DeleteEntityRequest)
	 */
	@Override
	public ChangeSetRequest addRequest(DeleteEntityRequest deleteEntityRequest) {
		reqs.add(deleteEntityRequest);
		return this;
	}

}
