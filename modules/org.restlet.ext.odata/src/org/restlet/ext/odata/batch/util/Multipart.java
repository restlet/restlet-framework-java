package org.restlet.ext.odata.batch.util;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.mimepull.MIMEPart;
import org.restlet.data.MediaType;


/**
 * The Class Multipart is logical representation of the multipart batch request.
 * 
 * 
 * copyright 2014 Halliburton
 * 
 * @author <a href="mailto:Amit.Jahagirdar@synerzip.com">Amit.Jahagirdar</a>
 */
public class Multipart extends BodyPart {

	/**
	 * Instantiates a new multipart.
	 */
	public Multipart() {
		super();
	}

	/**
	 * Instantiates a new multipart.
	 * 
	 * @param mimePart
	 *            the mime part
	 */
	public Multipart(MIMEPart mimePart) {
		super(mimePart);
	}

	/** The body parts. */
	List<BodyPart> bodyParts = new ArrayList<BodyPart>();

	/**
	 * Gets the body parts.
	 * 
	 * @return the body parts
	 */
	public List<BodyPart> getBodyParts() {
		return bodyParts;
	}

	/**
	 * Adds the body parts.
	 * 
	 * @param bodyPart
	 *            the body part
	 */
	public void addBodyParts(BodyPart bodyPart) {
		// sets the parent multipart on the bodypart and then adds it to the
		// list
		bodyPart.setParent(this);
		this.bodyParts.add(bodyPart);
	}

	
	@Override
	public void setMediaType(MediaType mediaType) {

		if (!mediaType.isCompatible(MediaType.MULTIPART_MIXED)) {
			throw new IllegalArgumentException(mediaType.toString());
		}
		super.setMediaType(mediaType);
	}

}
