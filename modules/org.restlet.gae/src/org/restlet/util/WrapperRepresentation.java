/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.Digest;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;

/**
 * Representation wrapper. Useful for application developer who need to enrich
 * the representation with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @author Jerome Louvel
 */
public class WrapperRepresentation extends Representation {
    /** The wrapped representation. */
    private final Representation wrappedRepresentation;

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation.
     */
    public WrapperRepresentation(Representation wrappedRepresentation) {
        this.wrappedRepresentation = wrappedRepresentation;
    }

    @Override
    @Deprecated
    public boolean checkDigest() {
        return getWrappedRepresentation().checkDigest();
    }

    @Override
    @Deprecated
    public boolean checkDigest(String algorithm) {
        return getWrappedRepresentation().checkDigest(algorithm);
    }

    @Override
    @Deprecated
    public Digest computeDigest(String algorithm) {
        return getWrappedRepresentation().computeDigest(algorithm);
    }

    @Override
    public long exhaust() throws IOException {
        return getWrappedRepresentation().exhaust();
    }

    @Override
    public long getAvailableSize() {
        return getWrappedRepresentation().getAvailableSize();
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return getWrappedRepresentation().getChannel();
    }

    @Override
    public CharacterSet getCharacterSet() {
        return getWrappedRepresentation().getCharacterSet();
    }

    @Override
    public Digest getDigest() {
        return getWrappedRepresentation().getDigest();
    }

    @Override
    public String getDownloadName() {
        return getWrappedRepresentation().getDownloadName();
    }

    @Override
    public List<Encoding> getEncodings() {
        return getWrappedRepresentation().getEncodings();
    }

    @Override
    public Date getExpirationDate() {
        return getWrappedRepresentation().getExpirationDate();
    }

    @Override
    public Reference getIdentifier() {
        return getWrappedRepresentation().getIdentifier();
    }

    @Override
    public List<Language> getLanguages() {
        return getWrappedRepresentation().getLanguages();
    }

    @Override
    public MediaType getMediaType() {
        return getWrappedRepresentation().getMediaType();
    }

    @Override
    public Date getModificationDate() {
        return getWrappedRepresentation().getModificationDate();
    }

    @Override
    public Range getRange() {
        return getWrappedRepresentation().getRange();
    }

    @Override
    public Reader getReader() throws IOException {
        return getWrappedRepresentation().getReader();
    }

    @Override
    public long getSize() {
        return getWrappedRepresentation().getSize();
    }

    @Override
    public InputStream getStream() throws IOException {
        return getWrappedRepresentation().getStream();
    }

    @Override
    public Tag getTag() {
        return getWrappedRepresentation().getTag();
    }

    @Override
    public String getText() throws IOException {
        return getWrappedRepresentation().getText();
    }

    /**
     * Returns the wrapped representation.
     * 
     * @return The wrapped representation.
     */
    public Representation getWrappedRepresentation() {
        return this.wrappedRepresentation;
    }

    @Override
    public boolean isAvailable() {
        return getWrappedRepresentation().isAvailable();
    }

    @Override
    public boolean isDownloadable() {
        return getWrappedRepresentation().isDownloadable();
    }

    @Override
    public boolean isTransient() {
        return getWrappedRepresentation().isTransient();
    }

    @Override
    public void release() {
        getWrappedRepresentation().release();
    }

    @Override
    public void setAvailable(boolean isAvailable) {
        getWrappedRepresentation().setAvailable(isAvailable);
    }

    @Override
    public void setCharacterSet(CharacterSet characterSet) {
        getWrappedRepresentation().setCharacterSet(characterSet);
    }

    @Override
    public void setDigest(Digest digest) {
        getWrappedRepresentation().setDigest(digest);
    }

    @Override
    public void setDownloadable(boolean downloadable) {
        getWrappedRepresentation().setDownloadable(downloadable);
    }

    @Override
    public void setDownloadName(String fileName) {
        getWrappedRepresentation().setDownloadName(fileName);
    }

    @Override
    public void setEncodings(List<Encoding> encodings) {
        getWrappedRepresentation().setEncodings(encodings);
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        getWrappedRepresentation().setExpirationDate(expirationDate);
    }

    @Override
    public void setIdentifier(Reference identifier) {
        getWrappedRepresentation().setIdentifier(identifier);
    }

    @Override
    public void setIdentifier(String identifierUri) {
        getWrappedRepresentation().setIdentifier(identifierUri);
    }

    @Override
    public void setLanguages(List<Language> languages) {
        getWrappedRepresentation().setLanguages(languages);
    }

    @Override
    public void setMediaType(MediaType mediaType) {
        getWrappedRepresentation().setMediaType(mediaType);
    }

    @Override
    public void setModificationDate(Date modificationDate) {
        getWrappedRepresentation().setModificationDate(modificationDate);
    }

    @Override
    public void setRange(Range range) {
        getWrappedRepresentation().setRange(range);
    }

    @Override
    public void setSize(long expectedSize) {
        getWrappedRepresentation().setSize(expectedSize);
    }

    @Override
    public void setTag(Tag tag) {
        getWrappedRepresentation().setTag(tag);
    }

    @Override
    public void setTransient(boolean isTransient) {
        getWrappedRepresentation().setTransient(isTransient);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        getWrappedRepresentation().write(outputStream);
    }

    @Override
    public void write(WritableByteChannel writableChannel)
            throws IOException {
        getWrappedRepresentation().write(writableChannel);
    }

    @Override
    public void write(Writer writer) throws IOException {
        getWrappedRepresentation().write(writer);
    }
}
