/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.restlet.Context;
import org.restlet.data.Digest;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.WrapperRepresentation;

import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Representation capable of computing a digest. It wraps another representation
 * and allows to get the computed digest of the wrapped entity after reading or
 * writing operations. The final digest value is guaranteed to be correct only
 * after the wrapped representation has been entirely exhausted (that is to say
 * read or written).<br>
 * <br>
 * This wrapper allows getting the computed digest at the same time the
 * representation is read or written. It does not need two separate operations
 * which may require specific attention for transient representations.
 *
 * @author Jerome Louvel
 * @see Representation#isTransient() .
 */
public class DigesterRepresentation extends WrapperRepresentation {
	/** The digest algorithm. */
	private final String algorithm;

	/** The computed digest value. */
	private volatile MessageDigest computedDigest;

	/**
	 * Constructor.<br>
	 * By default, the instance relies on the {@link Digest#ALGORITHM_MD5} digest
	 * algorithm.
	 *
	 * @param wrappedRepresentation The wrapped representation.
	 * @throws NoSuchAlgorithmException
	 */
	public DigesterRepresentation(Representation wrappedRepresentation) throws NoSuchAlgorithmException {
		this(wrappedRepresentation, Digest.ALGORITHM_MD5);
	}

	/**
	 * Constructor.<br>
	 *
	 * @param wrappedRepresentation The wrapped representation.
	 * @param algorithm             The digest algorithm.
	 * @throws NoSuchAlgorithmException
	 */
	public DigesterRepresentation(Representation wrappedRepresentation, String algorithm)
			throws NoSuchAlgorithmException {
		super(wrappedRepresentation);
		this.algorithm = algorithm;
		this.computedDigest = MessageDigest.getInstance(algorithm);
	}

	/**
	 * Check that the digest computed from the representation content and the digest
	 * declared by the representation are the same.<br>
	 * Since this method relies on the {@link #computeDigest(String)} method, and
	 * since this method reads entirely the representation's stream, user must take
	 * care of the content of the representation in case the latter is transient.
	 *
	 * @return True if both digests are not null and equals.
	 */
	public boolean checkDigest() {
		Digest digest = getDigest();
		return (digest != null && digest.equals(getComputedDigest()));
	}

	/**
	 * Check that the digest computed from the representation content and the digest
	 * declared by the representation are the same. It also first checks that the
	 * algorithms are the same.<br>
	 * Since this method relies on the {@link #computeDigest(String)} method, and
	 * since this method reads entirely the representation's stream, user must take
	 * care of the content of the representation in case the latter is transient.
	 *
	 * @param algorithm The algorithm used to compute the digest to compare with.
	 *                  See constant values in {@link org.restlet.data.Digest}.
	 * @return True if both digests are not null and equals.
	 */
	public boolean checkDigest(String algorithm) {
		boolean result = false;

		if (this.algorithm != null && this.algorithm.equals(algorithm)) {
			result = checkDigest();
		} else {
			Digest digest = getDigest();

			if (digest != null) {
				if (algorithm.equals(digest.getAlgorithm())) {
					result = digest.equals(computeDigest(algorithm));
				}
			}
		}

		return result;
	}

	/**
	 * Compute the representation digest according to MD5 algorithm.<br>
	 * If case this algorithm is the same as the one provided at instantiation,
	 * the computation operation is made with the current stored computed value and
	 * does not require to entirely exhaust the representation's stream.
	 */
	public Digest computeDigest() {
		return computeDigest(Digest.ALGORITHM_MD5);
	}

	/**
	 * Compute the representation digest according to the given algorithm.<br>
	 * Since this method entirely reads the representation stream, user must take
	 * care of the content of the representation in case the latter is transient.
	 *
	 * @param algorithm The algorithm used to compute the digest. See constant
	 *                  values in {@link org.restlet.data.Digest}.
	 * @return The computed digest or null if the digest cannot be computed.
	 */
	public Digest computeDigest(String algorithm) {
		Digest result = null;

		if (this.algorithm != null && this.algorithm.equals(algorithm)) {
			result = getComputedDigest();
		} else if (isAvailable()) {
			try {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
				try (java.security.DigestInputStream dis = new java.security.DigestInputStream(getStream(), md)) {
					org.restlet.engine.io.IoUtils.exhaust(dis);
				}
				result = new org.restlet.data.Digest(algorithm, md.digest());
			} catch (NoSuchAlgorithmException | IOException e) {
				Context.getCurrentLogger().log(Level.WARNING, "Unable to check the digest of the representation.", e);
			}
		}

		return result;
	}

	/**
	 * Exhausts the content of the representation by reading it and silently
	 * discarding anything read.
	 *
	 * @return The number of bytes consumed or -1 if unknown.
	 */
	@Override
	public long exhaust() throws IOException {
		long result = -1L;

		if (isAvailable()) {
			result = IoUtils.exhaust(getStream());
		}

		return result;
	}

	/**
	 * Returns the current computed digest value of the representation. User must be
	 * aware that, if the representation has not been entirely read or written, the
	 * computed digest value may not be accurate.
	 *
	 * @return The current computed digest value.
	 */
	public Digest getComputedDigest() {
		return new Digest(this.algorithm, computedDigest.digest());
	}

	@Override
	public Reader getReader() throws IOException {
		return IoUtils.getReader(getStream(), getCharacterSet());
	}

	/**
	 * {@inheritDoc}<br>
	 *
	 * The stream of the underlying representation is wrapped with a new instance of
	 * the {@link DigestInputStream} class, which allows to compute progressively
	 * the digest value.
	 */
	@Override
	public InputStream getStream() throws IOException {
		return new DigestInputStream(getWrappedRepresentation().getStream(), this.computedDigest);
	}

	@Override
	public String getText() throws IOException {
		String result = null;

		if (isAvailable()) {
			if (getSize() == 0) {
				result = "";
			} else {
				java.io.StringWriter sw = new java.io.StringWriter();
				write(sw);
				result = sw.toString();
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}<br>
	 *
	 * The output stream is wrapped with a new instance of the
	 * {@link DigestOutputStream} class, which allows to compute progressively the
	 * digest value.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		OutputStream dos = new DigestOutputStream(outputStream, this.computedDigest);
		getWrappedRepresentation().write(dos);
		dos.flush();
	}

	@Override
	public void write(Writer writer) throws IOException {
		OutputStream os = IoUtils.getStream(writer, getCharacterSet());
		write(os);
		os.flush();
	}
}
