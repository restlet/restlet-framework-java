package org.restlet.test.batch.crud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.jvnet.mimepull.Header;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.odata.batch.util.BatchConstants;
import org.restlet.ext.odata.batch.util.BodyPart;
import org.restlet.ext.odata.batch.util.Multipart;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Router;
import org.restlet.util.Series;

/**
 * Sample application that simulates the CUD operation on entities. 
 */

public class CafeCrudApplication extends Application {

	/**
	 * The Class dataLogger.
	 */
	private static class BatchTestRestlet extends Restlet {

		/** The Constant LOGGER. */
		private static final Logger LOGGER1 = Logger
				.getLogger(CreateCafeTestCase.class.getName());
		
		/** The batch id added to response. */
		private static boolean batchIdAddedToResponse = false;

		/** The builder. */
		private static StringBuilder builder = new StringBuilder();

		/** The batch id. */
		private static String batchId = null;

		/** The batch xmlFilePath. */
		private static final String xmlFilePath = "/org/restlet/test/batch/xml/";

		/** The file. */
		String fileName;

		/**
		 * Instantiates a new data logger.
		 * 
		 * @param context
		 *            the context
		 * @param file
		 *            the file
		 * @param updatable
		 *            the updatable
		 */
		public BatchTestRestlet(Context context, String xmlFileName,
				boolean updatable) {
			super(context);
			this.fileName = xmlFileName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.restlet.Restlet#handle(org.restlet.Request,
		 * org.restlet.Response)
		 */
		@Override
		public void handle(Request request, Response response) {

			if (Method.GET.equals(request.getMethod())) {
				String filePath = xmlFilePath;
				Response r = getContext().getClientDispatcher().handle(
						new Request(Method.GET, LocalReference
								.createClapReference(LocalReference.CLAP_CLASS,
										filePath + fileName + ".xml")));
				response.setEntity(r.getEntity());
				response.setStatus(r.getStatus());

			} else if (Method.POST.equals(request.getMethod())) {
				String rep = null;
				try {
					rep = request.getEntity().getText();
					String[] split = rep.split("\r\n");
					for (int i = 0; i < split.length; i++) {
						createResponse(split[i], builder);
					}
					response.setEntity(new StringRepresentation(builder
							.toString()));
					setResponse(response, Status.SUCCESS_OK);
				} catch (IOException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				}
				if (null != rep && !rep.isEmpty()) {
					response.setStatus(Status.SUCCESS_OK);
				}

			}
		}

		/**
		 * Creates the response as per the method type.
		 * 
		 * @param rep
		 *            the rep
		 * @param sb
		 *            the sb
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void createResponse(String rep, StringBuilder sb)
				throws IOException {
			String filePath = null;
			if (rep.contains(Method.GET.toString())) {
				LOGGER1.info("This is Get method");
				filePath = getFilePath("getCafeResponse.xml");
				readResponseAndFillBuffer(sb, filePath);

			} else if (rep.contains(Method.DELETE.toString())) {
				LOGGER1.info("This is delete method");
				filePath = getFilePath("deleteCafeResponse.xml");
				readResponseAndFillBuffer(sb, filePath);

			} else if (rep.contains(Method.PUT.toString())) {
				LOGGER1.info("This is put method");
				filePath = getFilePath("updateCafeResponse.xml");
				readResponseAndFillBuffer(sb, filePath);

			} else if (rep.contains(Method.POST.toString())) {
				LOGGER1.info("This is post method");
				filePath = getFilePath("createCafeResponse.xml");
				readResponseAndFillBuffer(sb, filePath);
			} else if (rep.contains("--changeset")) {

				sb.append("\n\n").append(
						rep.replace("changeset", "changesetresponse"));

			} else if (rep.contains("--batch")) {
				if (!batchIdAddedToResponse) {
					filePath = getFilePath("parentBatchResponse.xml");
					String replace = rep.replace("batch", "batchresponse");
					if (replace.contains("batch")) {
						String[] split = replace.split("_");
						batchId = split[1];
					}
					readResponseAndFillBuffer(sb, filePath);
					String str = sb.toString();
					str = str
							.replace(
									"batchresponse_1f82a90b-43f1-4276-b20a-59da3437dfa8",
									"batchresponse_" + batchId);
					String timeStamp = new SimpleDateFormat(
							"yyyy:MM:dd_HH:mm:ss").format(Calendar
							.getInstance().getTime());
					str = str.replace("Date: Mon, 23 June 2014 09:44:08 GMT",
							"Date:" + timeStamp);
					builder = new StringBuilder(str);
					batchIdAddedToResponse = true;
				}
				builder.append("\n\n").append(
						rep.replace("batch", "batchresponse"));

			} else if (rep
					.contains("Content-Type: application/atom+xml;charset=utf-8")) {

				sb.append("\n\n")
						.append(rep
								.replace(
										"Content-Type: application/atom+xml;charset=utf-8",
										""));

			} else if (rep.contains("Content-Type")) {
				if (rep.contains("changeset")) {
					sb.append("\n").append(
							rep.replace("changeset", "changesetresponse"));
				} else {
					sb.append("\n").append(
							rep.replace("Content-Type", "Content-Type"));
				}
			} else if (rep.contains("changeset_")) {
				sb.append("\n").append(
						rep.replace("changeset_", "changesetresponse_"));
			} else if (rep.contains("Content-Transfer-Encoding:")) {
				sb.append("\n").append(
						rep.replace("Content-Transfer-Encoding",
								"Content-Transfer-Encoding"));
			}

		}

		/**
		 * Gets the complete xml filePath
		 * 
		 * @param fileName
		 *            the file name
		 * @return the file path
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String getFilePath(String fileName) throws IOException {
			return new File(".").getCanonicalPath() + "/src" + xmlFilePath
					+ fileName;
		}

		/**
		 * Read response and fill buffer.
		 * 
		 * @param sb
		 *            the sb
		 * @param filePath
		 *            the file path
		 * @throws FileNotFoundException
		 *             the file not found exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void readResponseAndFillBuffer(StringBuilder sb, String filePath)
				throws FileNotFoundException, IOException {
			InputStream is = new FileInputStream(new File(filePath));
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while (br.ready()) {
				sb.append(br.readLine()).append("\n");

			}
			br.close();
		}

		/**
		 * Sets the response.
		 * 
		 * @param response
		 *            the response
		 * @param status
		 *            the status
		 */
		private void setResponse(Response response, Status status) {
			Series<Parameter> parameters = new Series<Parameter>(
					Parameter.class);
			parameters.add("boundary", "batchresponse_" + batchId);
			MediaType mediaType = new MediaType(
					MediaType.MULTIPART_MIXED.toString(), parameters);
			response.getEntity().setMediaType(mediaType);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.Application#createInboundRoot()
	 */
	@Override
	public Restlet createInboundRoot() {
		getMetadataService().setDefaultCharacterSet(CharacterSet.ISO_8859_1);
		getConnectorService().getClientProtocols().add(Protocol.CLAP);
		Router router = new Router(getContext());

		router.attach("/$metadata", new BatchTestRestlet(getContext(),
				"metadata", true));
		router.attach("/Cafes", new BatchTestRestlet(getContext(), "cafes",
				true));
		router.attach("/Cafes('40')", new BatchTestRestlet(getContext(),
				"cafesUpdatedRequest", true));
		router.attach("/$batch", new BatchTestRestlet(getContext(), "cafes",
				true));

		return router;
	}

	/**
	 * Creates the multipart.
	 * 
	 * A Multipart is a logical representation of a batch request or Chnagset. <br>
	 * It is a set of multiple http requests/response.
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
	 * Copy headers.
	 * 
	 * @param bodyPart
	 *            the body part
	 * @param mimePart
	 *            the mime part
	 */
	private static void copyHeaders(BodyPart bodyPart, MIMEPart mimePart) {
		MultivaluedMap<String, String> bpHeaders = bodyPart.getHeaders();
		List<? extends Header> mHeaders = mimePart.getAllHeaders();
		for (Header header : mHeaders) {
			bpHeaders.add(header.getName(), header.getValue());
		}

	}

}
