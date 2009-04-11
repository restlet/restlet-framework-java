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

package org.restlet.gae.engine.local;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.restlet.gae.Context;
import org.restlet.gae.Uniform;
import org.restlet.gae.data.MediaType;
import org.restlet.gae.data.Method;
import org.restlet.gae.data.Preference;
import org.restlet.gae.data.Reference;
import org.restlet.gae.data.ReferenceList;
import org.restlet.gae.data.Request;
import org.restlet.gae.data.Response;
import org.restlet.gae.data.Status;
import org.restlet.gae.representation.Representation;
import org.restlet.gae.representation.Variant;
import org.restlet.gae.resource.Directory;
import org.restlet.gae.resource.Resource;
import org.restlet.gae.resource.ResourceException;

/**
 * Resource supported by a set of context representations (from file system,
 * class loaders and webapp context). A content negotiation mechanism (similar
 * to Apache HTTP server) is available. It is based on path extensions to detect
 * variants (languages, media types or character sets).
 * 
 * @see <a
 *      href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache
 *      mod_negotiation module</a>
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class DirectoryResource extends Resource {

    /**
     * Returns the set of extensions contained in a given directory entry name.
     * 
     * @param entryName
     *            The directory entry name.
     * @return The set of extensions.
     */
    public static Set<String> getExtensions(String entryName) {
        final Set<String> result = new TreeSet<String>();
        final String[] tokens = entryName.split("\\.");
        for (int i = 1; i < tokens.length; i++) {
            result.add(tokens[i].toLowerCase());
        }
        return result;
    }

    /** The base set of extensions. */
    private Set<String> baseExtensions;

    /**
     * The local base name of the resource. For example, "foo.en" and
     * "foo.en-GB.html" return "foo".
     */
    private String baseName;

    /** The parent directory handler. */
    private final Directory directory;

    /** If the resource is a directory, this contains its content. */
    private ReferenceList directoryContent;

    /**
     * If the resource is a directory, the non-trailing slash character leads to
     * redirection.
     */
    private boolean directoryRedirection;

    /** Indicates if the target resource is a directory. */
    private boolean directoryTarget;

    /** The context's directory URI (file, clap URI). */
    private String directoryUri;

    /** If the resource is a file, this contains its content. */
    private Representation fileContent;

    /** Indicates if the target resource is a file. */
    private boolean fileTarget;

    /** Indicates if the target resource is a directory with an index. */
    private boolean indexTarget;

    /** The original target URI, in case of extensions tunneling. */
    private Reference originalRef;

    /** The resource path relative to the directory URI. */
    private String relativePart;

    /** The context's target URI (file, clap URI). */
    private String targetUri;

    /** The unique representation of the target URI, if it exists. */
    private Reference uniqueReference;

    /**
     * This constructor aims at answering the following questions:<br>
     * <ul>
     * <li>does this request target a directory?</li>
     * <li>does this request target a directory, with an index file?</li>
     * <li>should this request be redirected (target is a directory with no
     * trailing "/")?</li>
     * <li>does this request target a file?</li>
     * </ul>
     * <br>
     * The following constraints must be taken into account:<br>
     * <ul>
     * <li>the underlying helper may not support content negotiation and be able
     * to return the list of possible variants of the target file (e.g. the CLAP
     * helper).</li>
     * <li>the underlying helper may not support directory listing</li>
     * <li>the extensions tunneling cannot apply on a directory</li>
     * <li>underlying helpers that do not support content negotiation cannot
     * support extensions tunneling</li>
     * </ul>
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     * @throws IOException
     */
    public DirectoryResource(Context context, Request request, Response response)
            throws IOException {
        super(context, request, response);

        // Update the member variables
        this.directory = (Directory) request.getAttributes().get(
                "org.restlet.directory");
        this.relativePart = request.getResourceRef().getRemainingPart(false,
                false);
        setModifiable(this.directory.isModifiable());
        setNegotiateContent(this.directory.isNegotiateContent());

        // Restore the original URI in case the call has been tunneled.
        if ((getApplication() != null)
                && getApplication().getTunnelService().isExtensionsTunnel()) {
            this.originalRef = request.getOriginalRef();

            if (this.originalRef != null) {
                this.originalRef.setBaseRef(request.getResourceRef()
                        .getBaseRef());
                this.relativePart = this.originalRef.getRemainingPart();
            }
        }

        if (this.relativePart.startsWith("/")) {
            // We enforce the leading slash on the root URI
            this.relativePart = this.relativePart.substring(1);
        }

        // The target URI does not take into account the query and fragment
        // parts of the resource.
        this.targetUri = new Reference(directory.getRootRef().toString()
                + this.relativePart).normalize().toString(false, false);
        if (!this.targetUri.startsWith(directory.getRootRef().toString())) {
            // Prevent the client from accessing resources in upper directories
            this.targetUri = directory.getRootRef().toString();
        }

        if (getClientDispatcher() == null) {
            getLogger().warning(
                    "No client dispatcher is available on the context. Can't get the target URI: "
                            + this.targetUri);
        } else {
            // Try to detect the presence of a directory
            Response contextResponse = getRepresentation(this.targetUri);
            if (contextResponse.getEntity() != null) {
                // As a convention, underlying client connectors return the
                // directory listing with the media-type
                // "MediaType.TEXT_URI_LIST" when handling directories
                if (MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity()
                        .getMediaType())) {
                    this.directoryTarget = true;
                    this.fileTarget = false;
                    this.directoryContent = new ReferenceList(contextResponse
                            .getEntity());
                    if (!request.getResourceRef().getIdentifier().endsWith("/")) {
                        // All requests will be automatically redirected
                        this.directoryRedirection = true;
                    }

                    if (!this.targetUri.endsWith("/")) {
                        this.targetUri += "/";
                        this.relativePart += "/";
                    }

                    // Append the index name
                    if ((getDirectory().getIndexName() != null)
                            && (getDirectory().getIndexName().length() > 0)) {
                        this.directoryUri = this.targetUri;
                        this.baseName = getDirectory().getIndexName();
                        this.targetUri = this.directoryUri + this.baseName;
                        this.indexTarget = true;
                    } else {
                        this.directoryUri = this.targetUri;
                        this.baseName = null;
                    }
                } else {
                    // Allows underlying helpers that do not support "content
                    // negotiation" to return the targeted file.
                    this.directoryTarget = false;
                    this.fileTarget = true;
                    this.fileContent = contextResponse.getEntity();
                }
            } else {
                this.directoryTarget = false;
                this.fileTarget = false;

                // Let's try with the optional index, in case the underlying
                // client connector does not handle directory listing.
                if (this.targetUri.endsWith("/")) {
                    // In this case, the trailing "/" shows that the URI must
                    // point to a directory
                    if ((getDirectory().getIndexName() != null)
                            && (getDirectory().getIndexName().length() > 0)) {
                        this.directoryUri = this.targetUri;
                        this.directoryTarget = true;

                        contextResponse = getRepresentation(this.directoryUri
                                + getDirectory().getIndexName());
                        if (contextResponse.getEntity() != null) {
                            this.baseName = getDirectory().getIndexName();
                            this.targetUri = this.directoryUri + this.baseName;
                            this.directoryContent = new ReferenceList();
                            this.directoryContent.add(new Reference(
                                    this.targetUri));
                            this.indexTarget = true;
                        }
                    }
                } else {
                    // Try to determine if this target URI with no trailing "/"
                    // is a directory, in order to force the redirection.
                    if ((getDirectory().getIndexName() != null)
                            && (getDirectory().getIndexName().length() > 0)) {
                        // Append the index name
                        contextResponse = getRepresentation(this.targetUri
                                + "/" + getDirectory().getIndexName());
                        if (contextResponse.getEntity() != null) {
                            this.directoryUri = this.targetUri + "/";
                            this.baseName = getDirectory().getIndexName();
                            this.targetUri = this.directoryUri + this.baseName;
                            this.directoryTarget = true;
                            this.directoryRedirection = true;
                            this.directoryContent = new ReferenceList();
                            this.directoryContent.add(new Reference(
                                    this.targetUri));
                            this.indexTarget = true;
                        }
                    }
                }
            }

            // In case the request does not target a directory and the file has
            // not been found, try with the tunneled URI.
            if (isNegotiateContent() && !this.directoryTarget
                    && !this.fileTarget && (this.originalRef != null)) {
                this.relativePart = request.getResourceRef().getRemainingPart();

                // The target URI does not take into account the query and
                // fragment parts of the resource.
                this.targetUri = new Reference(directory.getRootRef()
                        .toString()
                        + this.relativePart).normalize().toString(false, false);
                if (!this.targetUri.startsWith(directory.getRootRef()
                        .toString())) {
                    // Prevent the client from accessing resources in upper
                    // directories
                    this.targetUri = directory.getRootRef().toString();
                }
            }

            // Try to get the directory content, in case the request does not
            // target a directory
            if (!this.directoryTarget) {
                final int lastSlashIndex = this.targetUri.lastIndexOf('/');
                if (lastSlashIndex == -1) {
                    this.directoryUri = "";
                    this.baseName = this.targetUri;
                } else {
                    this.directoryUri = this.targetUri.substring(0,
                            lastSlashIndex + 1);
                    this.baseName = this.targetUri
                            .substring(lastSlashIndex + 1);
                }

                contextResponse = getRepresentation(this.directoryUri);
                if ((contextResponse.getEntity() != null)
                        && MediaType.TEXT_URI_LIST.equals(contextResponse
                                .getEntity().getMediaType())) {
                    this.directoryContent = new ReferenceList(contextResponse
                            .getEntity());
                }
            }

            if (this.baseName != null) {
                // Remove the extensions from the base name
                final int firstDotIndex = this.baseName.indexOf('.');
                if (firstDotIndex != -1) {
                    // Store the set of extensions
                    this.baseExtensions = getExtensions(this.baseName);

                    // Remove stored extensions from the base name
                    this.baseName = this.baseName.substring(0, firstDotIndex);
                }

            }
        }

        // Check if the resource exists or not.
        final List<Variant> variants = getVariants();
        if ((variants == null) || (variants.isEmpty())) {
            setAvailable(false);
        }

        // Check if the resource is located in a sub directory.
        if (isAvailable() && !this.directory.isDeeplyAccessible()) {
            // Count the number of "/" character.
            int index = this.relativePart.indexOf("/");
            if (index != -1) {
                index = this.relativePart.indexOf("/", index);
                setAvailable((index == -1));
            }
        }

        // Log results
        getLogger().info("Converted target URI: " + this.targetUri);
        getLogger().fine("Converted base name : " + this.baseName);
    }

    /**
     * Returns the local base name of the file. For example, "foo.en" and
     * "foo.en-GB.html" return "foo".
     * 
     * @return The local name of the file.
     */
    public String getBaseName() {
        return this.baseName;
    }

    /**
     * Returns a client dispatcher.
     * 
     * @return A client dispatcher.
     */
    protected Uniform getClientDispatcher() {
        return getDirectory().getContext().getClientDispatcher();
    }

    /**
     * Returns the parent directory handler.
     * 
     * @return The parent directory handler.
     */
    public Directory getDirectory() {
        return this.directory;
    }

    /**
     * If the resource is a directory, this returns its content.
     * 
     * @return The directory content.
     */
    protected ReferenceList getDirectoryContent() {
        return directoryContent;
    }

    /**
     * Returns the context's directory URI (file, clap URI).
     * 
     * @return The context's directory URI (file, clap URI).
     */
    public String getDirectoryUri() {
        return this.directoryUri;
    }

    /**
     * Returns a representation of the resource at the target URI. Leverages the
     * client dispatcher of the parent directory's context.
     * 
     * @param resourceUri
     *            The URI of the target resource.
     * @return A response with the representation if success.
     */
    private Response getRepresentation(String resourceUri) {
        return getClientDispatcher().get(resourceUri);
    }

    /**
     * Returns a representation of the resource at the target URI. Leverages the
     * client dispatcher of the parent directory's context.
     * 
     * @param resourceUri
     *            The URI of the target resource.
     * @param acceptedMediaType
     *            The accepted media type or null.
     * @return A response with the representation if success.
     */
    protected Response getRepresentation(String resourceUri,
            MediaType acceptedMediaType) {
        if (acceptedMediaType == null) {
            return getClientDispatcher().get(resourceUri);
        } else {
            Request request = new Request(Method.GET, resourceUri);
            request.getClientInfo().getAcceptedMediaTypes().add(
                    new Preference<MediaType>(acceptedMediaType));
            return getClientDispatcher().handle(request);
        }
    }

    /**
     * Allows to sort the list of representations set by the resource.
     * 
     * @return A Comparator instance imposing a sort order of representations or
     *         null if no special order is wanted.
     */
    private Comparator<Representation> getRepresentationsComparator() {
        // Sort the list of representations by their identifier.
        final Comparator<Representation> identifiersComparator = new Comparator<Representation>() {
            public int compare(Representation rep0, Representation rep1) {
                final boolean bRep0Null = (rep0.getIdentifier() == null);
                final boolean bRep1Null = (rep1.getIdentifier() == null);

                if (bRep0Null && bRep1Null) {
                    return 0;
                }
                if (bRep0Null) {
                    return -1;
                }

                if (bRep1Null) {
                    return 1;
                }

                return rep0.getIdentifier().getLastSegment().compareTo(
                        rep1.getIdentifier().getLastSegment());
            }
        };
        return identifiersComparator;
    }

    /**
     * Returns the context's target URI (file, clap URI).
     * 
     * @return The context's target URI (file, clap URI).
     */
    public String getTargetUri() {
        return this.targetUri;
    }

    /**
     * Returns the representation variants.
     * 
     * @return The representation variants.
     */
    @Override
    public List<Variant> getVariants() {
        final List<Variant> results = super.getVariants();

        if (!results.isEmpty()) {
            return results;
        }

        getLogger().info("Getting variants for : " + getTargetUri());

        if ((this.directoryContent != null)
                && (getRequest().getResourceRef() != null)
                && (getRequest().getResourceRef().getBaseRef() != null)) {

            // Allows to sort the list of representations
            final SortedSet<Representation> resultSet = new TreeSet<Representation>(
                    getRepresentationsComparator());

            // Compute the base reference (from a call's client point of view)
            String baseRef = getRequest().getResourceRef().getBaseRef()
                    .toString(false, false);

            if (!baseRef.endsWith("/")) {
                baseRef += "/";
            }

            final int lastIndex = this.relativePart.lastIndexOf("/");

            if (lastIndex != -1) {
                baseRef += this.relativePart.substring(0, lastIndex);
            }

            final int rootLength = getDirectoryUri().length();

            if (this.baseName != null) {
                String filePath;
                for (final Reference ref : getVariantsReferences()) {
                    // Add the new variant to the result list
                    final Response contextResponse = getRepresentation(ref
                            .toString());
                    if (contextResponse.getStatus().isSuccess()
                            && (contextResponse.getEntity() != null)) {
                        filePath = ref.toString(false, false).substring(
                                rootLength);
                        final Representation rep = contextResponse.getEntity();
                        if (filePath.startsWith("/")) {
                            rep.setIdentifier(baseRef + filePath);
                        } else {
                            rep.setIdentifier(baseRef + "/" + filePath);
                        }
                        resultSet.add(rep);
                    }
                }
            }

            results.addAll(resultSet);

            if (resultSet.isEmpty()) {
                if (this.directoryTarget && getDirectory().isListingAllowed()) {
                    final ReferenceList userList = new ReferenceList(
                            this.directoryContent.size());
                    // Set the list identifier
                    userList.setIdentifier(baseRef);

                    final SortedSet<Reference> sortedSet = new TreeSet<Reference>(
                            getDirectory().getComparator());
                    sortedSet.addAll(this.directoryContent);

                    for (final Reference ref : sortedSet) {
                        final String filePart = ref.toString(false, false)
                                .substring(rootLength);
                        final StringBuilder filePath = new StringBuilder();
                        if ((!baseRef.endsWith("/"))
                                && (!filePart.startsWith("/"))) {
                            filePath.append('/');
                        }
                        filePath.append(filePart);
                        userList.add(baseRef + filePath);
                    }
                    final List<Variant> list = getDirectory().getIndexVariants(
                            userList);
                    for (final Variant variant : list) {
                        results.add(getDirectory().getIndexRepresentation(
                                variant, userList));
                    }

                }
            }
        } else if (this.fileTarget && (this.fileContent != null)) {
            // Sets the identifier of the target representation.
            if (getRequest().getOriginalRef() != null) {
                this.fileContent.setIdentifier(getRequest().getOriginalRef());
            } else {
                this.fileContent.setIdentifier(getRequest().getResourceRef());
            }
            results.add(this.fileContent);
        }

        return results;
    }

    /**
     * Returns the references of the representations of the target resource
     * according to the directory handler property
     * 
     * @return The list of variants references
     */
    private ReferenceList getVariantsReferences() {
        this.uniqueReference = null;
        final ReferenceList result = new ReferenceList(0);
        try {
            // Ask for the list of all variants of this resource
            final Response contextResponse = getRepresentation(this.targetUri,
                    MediaType.TEXT_URI_LIST);
            if (contextResponse.getEntity() != null) {
                // Test if the given response is the list of all variants for
                // this resource
                if (MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity()
                        .getMediaType())) {
                    final ReferenceList listVariants = new ReferenceList(
                            contextResponse.getEntity());
                    Set<String> extensions = null;
                    String entryUri;
                    String fullEntryName;
                    String baseEntryName;
                    int lastSlashIndex;
                    int firstDotIndex;
                    for (final Reference ref : listVariants) {
                        entryUri = ref.toString();
                        lastSlashIndex = entryUri.lastIndexOf('/');
                        fullEntryName = (lastSlashIndex == -1) ? entryUri
                                : entryUri.substring(lastSlashIndex + 1);
                        baseEntryName = fullEntryName;

                        // Remove the extensions from the base name
                        firstDotIndex = fullEntryName.indexOf('.');
                        if (firstDotIndex != -1) {
                            baseEntryName = fullEntryName.substring(0,
                                    firstDotIndex);
                        }

                        // Check if the current file is a valid variant
                        if (baseEntryName.equals(this.baseName)) {
                            boolean validVariant = true;

                            // Verify that the extensions are compatible
                            extensions = getExtensions(fullEntryName);
                            validVariant = (((extensions == null) && (this.baseExtensions == null))
                                    || (this.baseExtensions == null) || extensions
                                    .containsAll(this.baseExtensions));

                            if (validVariant
                                    && (this.baseExtensions != null)
                                    && this.baseExtensions
                                            .containsAll(extensions)) {
                                // The unique reference has been found.
                                this.uniqueReference = ref;
                            }

                            if (validVariant) {
                                result.add(ref);
                            }
                        }
                    }
                } else {
                    result.add(contextResponse.getEntity().getIdentifier());
                }
            }
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to get resource variants",
                    ioe);
        }

        return result;
    }

    @Override
    public void handleGet() {
        if (this.directoryRedirection) {
            // If this request targets a directory and if the target URI does
            // not end with a trailing "/", the client is told to redirect to a
            // correct URI.

            // Restore the cut extensions in case the call has been tunnelled.
            if (this.originalRef != null) {
                getResponse().redirectPermanent(
                        this.originalRef.getIdentifier() + "/");
            } else {
                getResponse().redirectPermanent(
                        getRequest().getResourceRef().getIdentifier() + "/");
            }
        } else {
            super.handleGet();
        }
    }

    /**
     * Indicates if the target resource is a directory.
     * 
     * @return True if the target resource is a directory.
     */
    public boolean isDirectoryTarget() {
        return this.directoryTarget;
    }

    /**
     * Indicates if the target resource is a file.
     * 
     * @return True if the target resource is a file.
     */
    public boolean isFileTarget() {
        return this.fileTarget;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        if (this.directoryRedirection) {
            if (this.originalRef != null) {
                getResponse().redirectSeeOther(
                        this.originalRef.getIdentifier() + "/");
            } else {
                getResponse().redirectSeeOther(
                        getRequest().getResourceRef().getIdentifier() + "/");
            }
        } else {
            final Request contextRequest = new Request(Method.DELETE,
                    this.targetUri);
            final Response contextResponse = new Response(contextRequest);

            if (this.directoryTarget && !this.indexTarget) {
                contextRequest.setResourceRef(this.targetUri);
                getClientDispatcher().handle(contextRequest, contextResponse);
            } else {
                // Check if there is only one representation

                // Try to get the unique representation of the resource
                final ReferenceList references = getVariantsReferences();
                if (!references.isEmpty()) {
                    if (this.uniqueReference != null) {
                        contextRequest.setResourceRef(this.uniqueReference);
                        getClientDispatcher().handle(contextRequest,
                                contextResponse);
                    } else {
                        // We found variants, but not the right one
                        contextResponse
                                .setStatus(new Status(
                                        Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                                        "Unable to process properly the request. Several variants exist but none of them suits precisely. "));
                    }
                } else {
                    contextResponse.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }

            getResponse().setStatus(contextResponse.getStatus());
        }
    }

    /**
     * Sets the context's target URI (file, clap URI).
     * 
     * @param targetUri
     *            The context's target URI.
     */
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        if (this.directoryRedirection) {
            if (this.originalRef != null) {
                getResponse().redirectSeeOther(
                        this.originalRef.getIdentifier() + "/");
            } else {
                getResponse().redirectSeeOther(
                        getRequest().getResourceRef().getIdentifier() + "/");
            }
        } else {
            // Transfer of PUT calls is only allowed if the readOnly flag is not
            // set.
            final Request contextRequest = new Request(Method.PUT,
                    this.targetUri);
            // Add support of partial PUT calls.
            contextRequest.getRanges().addAll(getRequest().getRanges());
            contextRequest.setEntity(entity);
            final Response contextResponse = new Response(contextRequest);
            contextRequest.setResourceRef(this.targetUri);
            getClientDispatcher().handle(contextRequest, contextResponse);
            getResponse().setStatus(contextResponse.getStatus());
        }
    }
}
