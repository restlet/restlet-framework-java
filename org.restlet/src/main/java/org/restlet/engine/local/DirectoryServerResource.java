/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource supported by a set of context representations (from file system, class loaders, and
 * webapp context). A content negotiation mechanism (similar to Apache HTTP Server) is available. It
 * is based on path extensions to detect variants (languages, media types, or character sets).
 *
 * @author Jerome Louvel
 * @author Thierry Boileau
 * @see <a href= "https://httpd.apache.org/docs/current/content-negotiation.html">Apache
 *     mod_negotiation module</a>
 */
public class DirectoryServerResource extends ServerResource {

    /** The list of variants for the GET method. */
    private volatile List<Variant> variantsGet;

    /**
     * The local base name of the resource. For example, "foo.en" and "foo.en-GB.html" return "foo".
     */
    private volatile String baseName;

    /** The base variant. */
    private volatile Variant baseVariant;

    /** The parent directory client dispatcher. */
    private volatile Restlet directoryClientDispatcher;

    /** The parent directory handler. */
    private volatile Directory directory;

    /** If the resource is a directory, this contains its content. */
    private volatile ReferenceList directoryContent;

    /** If the resource is a directory, the non-trailing slash character leads to redirection. */
    private volatile boolean directoryRedirection;

    /** Indicates if the target resource is a directory. */
    private volatile boolean directoryTarget;

    /** The context's directory URI (file, clap URI). */
    private volatile String directoryUri;

    /** If the resource is a file, this contains its content. */
    private volatile Representation fileContent;

    /** Indicates if the target resource is a file. */
    private volatile boolean fileTarget;

    /** Indicates if the target resource is a directory with an index. */
    private volatile boolean indexTarget;

    /** The original target URI, in case of extensions tunneling. */
    private volatile Reference originalRef;

    /** The prototype variant. */
    private volatile Variant protoVariant;

    /** The resource path relative to the directory URI. */
    private volatile String relativePart;

    /** The context's target URI (file, clap URI). */
    private volatile String targetUri;

    /** The unique representation of the target URI, if it exists. */
    private volatile Reference uniqueReference;

    @Override
    public Representation delete() throws ResourceException {
        if (!this.directory.isModifiable()) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "The directory is not modifiable.");
        } else {
            Request contextRequest = new Request(Method.DELETE, this.targetUri);
            Response contextResponse = new Response(contextRequest);

            if (this.directoryTarget && !this.indexTarget) {
                // let the client handle the directory's deletion
                contextRequest.setResourceRef(this.targetUri);
                dispatchRequest(contextRequest, contextResponse);

                setStatus(contextResponse.getStatus());
            } else {
                ReferenceList references = getVariantsReferences();

                if (references.isEmpty()) {
                    // no representation found
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                } else if (this.uniqueReference != null) {
                    // only one representation
                    contextRequest.setResourceRef(this.uniqueReference);
                    dispatchRequest(contextRequest, contextResponse);
                    setStatus(contextResponse.getStatus());
                } else {
                    // several variants found, but not the right one
                    setStatus(
                            Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                            "Unable to process properly the request. Several variants exist but none of them suits precisely. ");
                }
            }
        }
        return null;
    }

    /**
     * This initialization method aims at answering the following questions:<br>
     *
     * <ul>
     *   <li>does this request target a directory?
     *   <li>does this request target a directory, with an index file?
     *   <li>should this request be redirected (target is a directory with no trailing "/")?
     *   <li>does this request target a file?
     * </ul>
     *
     * <br>
     * The following constraints must be taken into account:<br>
     *
     * <ul>
     *   <li>the underlying helper may not support content negotiation and be able to return the
     *       list of possible variants of the target file (e.g., the CLAP helper).
     *   <li>the underlying helper may not support directory listing
     *   <li>the extensions tunneling cannot apply on a directory
     *   <li>underlying helpers that do not support content negotiation cannot support extensions
     *       tunneling
     * </ul>
     */
    @Override
    public void doInit() throws ResourceException {
        this.directory = (Directory) getRequestAttributes().get("org.restlet.directory");
        this.directoryClientDispatcher =
                getDirectory().getContext() != null
                        ? getDirectory().getContext().getClientDispatcher()
                        : null;
        validateClientDispatcher();
        setNegotiated(this.directory.isNegotiatingContent());

        resolveRelativePart();
        this.targetUri =
                new Reference(directory.getRootRef().toString() + this.relativePart)
                        .toString(false, false);
        preventUpperDirectoryAccess();

        detectTarget();
        applyTunneledUriIfNeeded();
        resolveContentAndBaseName();
        checkSubdirectoryAccess();

        getLogger().log(Level.FINE, "Converted target URI: {0}", this.targetUri);
        getLogger().log(Level.FINE, "Converted base name: {0}", this.baseName);
    }

    /**
     * Resolves {@link #relativePart} from the current reference, accounting for extensions
     * tunneling when applicable.
     */
    private void resolveRelativePart() {
        this.relativePart = getReference().getRemainingPart(false, false);
        this.originalRef = getOriginalRef();
        if (this.originalRef != null
                && (getApplication() != null)
                && getApplication().getTunnelService().isExtensionsTunnel()) {
            // Restore the original URI in case the call has been tunneled.
            Reference originalBaseRef = new Reference(this.originalRef);
            originalBaseRef.setPath(getReference().getBaseRef().getPath());
            this.originalRef.setBaseRef(originalBaseRef);
            this.relativePart = this.originalRef.getRemainingPart(false, false);
        }
        if (this.relativePart.startsWith("/")) {
            // We enforce the leading slash on the root URI
            this.relativePart = this.relativePart.substring(1);
        }
    }

    /**
     * Probes the context for the current {@link #targetUri} and dispatches to {@link
     * #handleEntityResponse(Response)} or {@link #handleNoEntityResponse()} accordingly.
     */
    private void detectTarget() {
        Response contextResponse = getRepresentation(this.targetUri);
        if (contextResponse.getEntity() != null) {
            handleEntityResponse(contextResponse);
        } else {
            handleNoEntityResponse();
        }
    }

    /**
     * Handles the case where the context returned an entity for {@link #targetUri}.
     *
     * <p>If the entity is a URI list the target is treated as a directory; otherwise it is treated
     * as a directly-resolved file.
     *
     * @param contextResponse The successful response from the context dispatcher.
     */
    private void handleEntityResponse(Response contextResponse) {
        if (MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity().getMediaType())) {
            // As a convention, underlying client connectors return the directory listing with
            // the media-type "MediaType.TEXT_URI_LIST" when handling directories.
            this.directoryTarget = true;
            this.fileTarget = false;
            this.directoryContent = tryToConvertAsReferenceList(contextResponse.getEntity());

            if (!getReference().getPath().endsWith("/")) {
                // All requests will be automatically redirected
                this.directoryRedirection = true;
            }

            if (!this.targetUri.endsWith("/")) {
                this.targetUri += "/";
                this.relativePart += "/";
            }

            // Append the index name
            if (!StringUtils.isNullOrEmpty(getDirectory().getIndexName())) {
                this.directoryUri = this.targetUri;
                this.baseName = getDirectory().getIndexName();
                this.targetUri = this.directoryUri + this.baseName;
                this.indexTarget = true;
            } else {
                this.directoryUri = this.targetUri;
                this.baseName = null;
            }
        } else {
            // Allows underlying helpers that do not support "content negotiation" to return
            // the targeted file directly.
            this.directoryTarget = false;
            this.fileTarget = true;
            this.fileContent = contextResponse.getEntity();
        }
    }

    /**
     * Handles the case where the context returned no entity for {@link #targetUri}.
     *
     * <p>Falls back to probing for an index file, both for URIs that end with "/" (directory hint)
     * and for URIs without a trailing slash (potential directory requiring redirection).
     */
    private void handleNoEntityResponse() {
        this.directoryTarget = false;
        this.fileTarget = false;

        // Let's try with the optional index, in case the underlying
        // client connector does not handle directory listing.
        if (this.targetUri.endsWith("/")) {
            // The trailing "/" signals that the URI must point to a directory.
            if (!StringUtils.isNullOrEmpty(getDirectory().getIndexName())) {
                this.directoryUri = this.targetUri;
                this.directoryTarget = true;

                Response contextResponse =
                        getRepresentation(this.directoryUri + getDirectory().getIndexName());
                if (contextResponse.getEntity() != null) {
                    this.baseName = getDirectory().getIndexName();
                    this.targetUri = this.directoryUri + this.baseName;
                    this.directoryContent = new ReferenceList();
                    this.directoryContent.add(new Reference(this.targetUri));
                    this.indexTarget = true;
                }
            }
        } else {
            // Try to determine if this target URI with no trailing "/" is a directory to force
            // redirection.
            if (!StringUtils.isNullOrEmpty(getDirectory().getIndexName())) {
                Response contextResponse =
                        getRepresentation(this.targetUri + "/" + getDirectory().getIndexName());
                if (contextResponse.getEntity() != null) {
                    this.directoryUri = this.targetUri + "/";
                    this.baseName = getDirectory().getIndexName();
                    this.targetUri = this.directoryUri + this.baseName;
                    this.directoryTarget = true;
                    this.directoryRedirection = true;
                    this.directoryContent = new ReferenceList();
                    this.directoryContent.add(new Reference(this.targetUri));
                    this.indexTarget = true;
                }
            }
        }
    }

    /**
     * When content negotiation is active and the target has not been resolved yet, retries with the
     * tunneled URI (i.e., the URI before extension-based tunneling was applied).
     */
    private void applyTunneledUriIfNeeded() {
        if (!isNegotiated()
                || this.directoryTarget
                || this.fileTarget
                || this.originalRef == null) {
            return;
        }
        this.relativePart = getReference().getRemainingPart();
        this.targetUri =
                new Reference(directory.getRootRef().toString() + this.relativePart)
                        .normalize()
                        .toString(false, false);
        if (!this.targetUri.startsWith(directory.getRootRef().toString())) {
            // Prevent the client from accessing resources in upper directories
            this.targetUri = directory.getRootRef().toString();
        }
    }

    /**
     * Resolves the directory content and base name for the current target, analyzes file extensions
     * to populate {@link #baseVariant} and {@link #protoVariant}, and marks the resource as
     * non-existing when no variants are found.
     */
    private void resolveContentAndBaseName() {
        if (fileTarget && fileContent != null && getRequest().getMethod().isSafe()) {
            return;
        }

        if (!this.directoryTarget) {
            int lastSlashIndex = this.targetUri.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                this.directoryUri = "";
                this.baseName = this.targetUri;
            } else {
                this.directoryUri = this.targetUri.substring(0, lastSlashIndex + 1);
                this.baseName = this.targetUri.substring(lastSlashIndex + 1);
            }

            Response contextResponse = getRepresentation(this.directoryUri);
            if ((contextResponse.getEntity() != null)
                    && MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity().getMediaType())) {
                this.directoryContent = tryToConvertAsReferenceList(contextResponse.getEntity());
            }
        }

        if (this.baseName != null) {
            // Analyze extensions to populate base and proto variants
            this.baseVariant = new Variant();
            Entity.updateMetadata(this.baseName, this.baseVariant, true, getMetadataService());
            this.protoVariant = new Variant();
            Entity.updateMetadata(this.baseName, this.protoVariant, false, getMetadataService());
            // Remove stored extensions from the base name
            this.baseName = Entity.getBaseName(this.baseName, getMetadataService());
        }

        List<Variant> variants = getVariants(Method.GET);
        if ((variants == null) || (variants.isEmpty())) {
            setExisting(false);
        }
    }

    /**
     * When deep access is disabled, marks the resource as non-existing if it is located inside a
     * subdirectory (i.e., {@link #relativePart} contains more than one path segment).
     */
    private void checkSubdirectoryAccess() {
        if (isExisting() && !this.directory.isDeeplyAccessible()) {
            int index = this.relativePart.indexOf('/');
            if (index != -1) {
                index = this.relativePart.indexOf('/', index);
                setExisting((index == -1));
            }
        }
    }

    private void validateClientDispatcher() {
        if (getClientDispatcher() == null) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "No client dispatcher is available. Can''t get the target URI: {0}",
                            this.targetUri);

            throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL, "No client dispatcher is available.");
        }
    }

    private ReferenceList tryToConvertAsReferenceList(Representation entity)
            throws ResourceException {
        try {
            return new ReferenceList(entity);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    @Override
    protected Representation get() throws ResourceException {
        // Content negotiation has been disabled
        // The variant that may need to meet the request conditions

        List<Variant> variants = getVariants(Method.GET);
        if ((variants == null) || (variants.isEmpty())) {
            // Resource not found
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        }

        if (variants.size() == 1) {
            return (Representation) variants.getFirst();
        }

        ReferenceList variantRefs = new ReferenceList();

        for (Variant variant : variants) {
            if (variant.getLocationRef() != null) {
                variantRefs.add(variant.getLocationRef());
            } else {
                getLogger()
                        .warning(
                                "A resource with multiple variants should provide a location for each variant when content negotiation is turned off");
            }
        }

        if (!variantRefs.isEmpty()) {
            // Return the list of variants
            setStatus(Status.REDIRECTION_MULTIPLE_CHOICES);
            return variantRefs.getTextRepresentation();
        }

        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }

    /**
     * Returns the local base name of the file. For example, "foo.en" and "foo.en-GB.html" return
     * "foo".
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
    protected Restlet getClientDispatcher() {
        return directoryClientDispatcher;
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
     * Returns a representation of the resource at the target URI. Leverages the client dispatcher
     * of the parent directory's context.
     *
     * @param resourceUri The URI of the target resource.
     * @return A response with the representation if success.
     */
    private Response getRepresentation(String resourceUri) {
        return dispatchRequest(new Request(Method.GET, resourceUri));
    }

    /**
     * Returns a representation of the resource at the target URI. Leverages the client dispatcher
     * of the parent directory's context.
     *
     * @param resourceUri The URI of the target resource.
     * @param acceptedMediaType The accepted media type or null.
     * @return A response with the representation if success.
     */
    protected Response getRepresentation(String resourceUri, MediaType acceptedMediaType) {
        Request request = new Request(Method.GET, resourceUri);

        if (acceptedMediaType != null) {
            request.getClientInfo().accept(acceptedMediaType);
        }

        return dispatchRequest(request);
    }

    /**
     * Allows sorting the list of representations set by the resource.
     *
     * @return A Comparator instance imposing a sort order of representations or null if no special
     *     order is wanted.
     */
    private Comparator<Representation> getRepresentationsComparator() {
        // Sort the list of representations by their identifier.
        return (rep0, rep1) -> {
            boolean bRep0Null = (rep0.getLocationRef() == null);
            boolean bRep1Null = (rep1.getLocationRef() == null);

            if (bRep0Null && bRep1Null) {
                return 0;
            }
            if (bRep0Null) {
                return -1;
            }
            if (bRep1Null) {
                return 1;
            }

            return rep0.getLocationRef()
                    .getLastSegment()
                    .compareTo(rep1.getLocationRef().getLastSegment());
        };
    }

    /**
     * Returns the context's target URI (file, clap URI).
     *
     * @return The context's target URI (file, clap URI).
     */
    public String getTargetUri() {
        return this.targetUri;
    }

    @Override
    public List<Variant> getVariants() {
        return getVariants(getMethod());
    }

    /**
     * Returns the list of variants for the given method.
     *
     * @param method The related method.
     * @return The list of variants for the given method.
     */
    @Override
    protected List<Variant> getVariants(Method method) {
        if (!Method.GET.equals(method) && !Method.HEAD.equals(method)) {
            return List.of();
        }

        if (variantsGet == null) {
            getLogger().fine("Getting variants for: " + getTargetUri());

            if (this.fileTarget && (this.fileContent != null)) {
                variantsGet = getVariantsFromFileContent();
            } else if ((this.directoryContent != null)
                    && (getReference() != null)
                    && (getReference().getBaseRef() != null)) {
                variantsGet = getVariantsFromDirectoryContent();
            }
        }

        return this.variantsGet;
    }

    private List<Variant> getVariantsFromDirectoryContent() {
        // filter the directory listing

        // Allow sorting the list of representations
        SortedSet<Representation> resultSet = new TreeSet<>(getRepresentationsComparator());

        // Compute the base reference (from a call's client point of view)
        String baseReference = getVariantsBaseReference();

        int rootLength = getDirectoryUri().length();

        if (this.baseName != null) {
            for (Reference ref : getVariantsReferences()) {
                // Add the new variant to the result list
                Representation rep =
                        getVariantRepresentationIfExists(ref, rootLength, baseReference);
                if (rep != null) {
                    resultSet.add(rep);
                }
            }
        }

        if (resultSet.isEmpty()) {
            if (this.directoryTarget && getDirectory().isListingAllowed()) {
                return getVariantsFromDirectoryListing(baseReference, rootLength);
            } else {
                return null;
            }
        } else {
            return new ArrayList<>(resultSet);
        }
    }

    private Representation getVariantRepresentationIfExists(
            final Reference ref, final int rootLength, final String baseReference) {
        final Response contextResponse = getRepresentation(ref.toString());
        final Representation rep = contextResponse.getEntity();
        if (contextResponse.getStatus().isSuccess() && rep != null) {
            final String filePath = ref.toString(false, false).substring(rootLength);

            if (filePath.startsWith("/")) {
                rep.setLocationRef(baseReference + filePath);
            } else {
                rep.setLocationRef(baseReference + "/" + filePath);
            }
            return rep;
        }
        return null;
    }

    private List<Variant> getVariantsFromDirectoryListing(
            final String baseReference, final int rootLength) {
        // computes variants from the directory listing
        ReferenceList userList = new ReferenceList(this.directoryContent.size());
        // Set the list identifier
        userList.setIdentifier(baseReference);

        SortedSet<Reference> sortedSet = new TreeSet<>(getDirectory().getComparator());
        sortedSet.addAll(this.directoryContent);

        for (Reference ref : sortedSet) {
            String filePart = ref.toString(false, false).substring(rootLength);
            StringBuilder filePath = new StringBuilder();
            if ((!baseReference.endsWith("/")) && (!filePart.startsWith("/"))) {
                filePath.append('/');
            }
            filePath.append(filePart);
            userList.add(baseReference + filePath);
        }
        List<Variant> list = getDirectory().getIndexVariants(userList);

        if (list != null && !list.isEmpty()) {
            List<Variant> result = new ArrayList<>();
            for (Variant variant : list) {
                result.add(getDirectory().getIndexRepresentation(variant, userList));
            }
            return result;
        }
        return null;
    }

    private List<Variant> getVariantsFromFileContent() {
        // found a target file, set its content location
        if (getOriginalRef() != null) {
            this.fileContent.setLocationRef(getRequest().getOriginalRef());
        } else {
            this.fileContent.setLocationRef(getReference());
        }

        return Arrays.asList(this.fileContent);
    }

    private String getVariantsBaseReference() {
        String baseRef = getReference().getBaseRef().toString(false, false);

        if (!baseRef.endsWith("/")) {
            baseRef += "/";
        }

        int lastIndex = this.relativePart.lastIndexOf('/');

        if (lastIndex != -1) {
            baseRef += this.relativePart.substring(0, lastIndex);
        }
        return baseRef;
    }

    /**
     * Returns the references of the representations of the target resource according to the
     * directory handler property
     *
     * @return The list of variants references
     */
    private ReferenceList getVariantsReferences() {
        this.uniqueReference = null;

        // Ask for the list of all variants of this resource
        Response contextResponse = getRepresentation(this.targetUri, MediaType.TEXT_URI_LIST);

        if (contextResponse.getEntity() == null) {
            return new ReferenceList(0);
        }

        if (!MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity().getMediaType())) {
            // The unique reference has been found.
            this.uniqueReference = contextResponse.getEntity().getLocationRef();
            return new ReferenceList(
                    Collections.singletonList(contextResponse.getEntity().getLocationRef()));
        }

        try {
            // Test if the given response is the list of all variants for this resource
            final ReferenceList listVariants = new ReferenceList(contextResponse.getEntity());
            return filterValidVariants(listVariants);
        } catch (IOException ioe) {
            getLogger()
                    .log(Level.WARNING, "Unable to get resource variants, return empty list", ioe);
            return new ReferenceList(0);
        }
    }

    /**
     * Filter the variants that have the right base name
     *
     * @param listVariants The list of variants to filter.
     * @return a filtered list of variants.
     */
    private ReferenceList filterValidVariants(final ReferenceList listVariants) {
        final ReferenceList variantsReferences = new ReferenceList(0);
        for (Reference variantReference : listVariants) {
            String entryUri = variantReference.toString();
            int lastSlashIndex = entryUri.lastIndexOf('/');
            String fullEntryName =
                    (lastSlashIndex == -1) ? entryUri : entryUri.substring(lastSlashIndex + 1);

            // Remove the extensions from the base name
            int firstDotIndex = fullEntryName.indexOf('.');
            String baseEntryName =
                    (firstDotIndex != -1)
                            ? fullEntryName.substring(0, firstDotIndex)
                            : fullEntryName;

            if (baseEntryName.equals(this.baseName)) { // Valid variant
                // Test if the variant is included in the base prototype variant
                Variant variant = new Variant();
                Entity.updateMetadata(fullEntryName, variant, true, getMetadataService());

                if (this.protoVariant.includes(variant)) { // Valid variant
                    variantsReferences.add(variantReference);

                    if (variant.equals(this.baseVariant)) {
                        // The unique reference has been found.
                        this.uniqueReference = variantReference;
                    }
                }
            }
        }

        return variantsReferences;
    }

    @Override
    public Representation handle() {
        if (!this.directoryRedirection) {
            return super.handle();
        }

        // detected a directory, but the current reference lacks the trailing "/", let's
        // redirect.
        Reference directoryReference =
                (this.originalRef != null) ? this.originalRef : getReference().getTargetRef();
        if (directoryReference.hasQuery()) {
            redirectSeeOther(
                    directoryReference.toString(false, false)
                            + "/?"
                            + directoryReference.getQuery());
        } else {
            redirectSeeOther(directoryReference.toString(false, false) + "/");
        }

        return null;
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

    /**
     * Transmit the given request to the clientDispatcher.<br>
     * It completes the request's attributes map with the current Directory ("org.restlet.directory"
     * key).
     *
     * @param request The request to send.
     * @return The response
     */
    private Response dispatchRequest(final Request request) {
        final Response response = new Response(request);
        dispatchRequest(request, response);

        if (response.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
            throw new ResourceException(response.getStatus());
        }

        return response;
    }

    /**
     * Transmit the given request to the clientDispatcher.<br>
     * It completes the request's attributes map with the current Directory ("org.restlet.directory"
     * key).
     *
     * @param request The request to send.
     * @param response The related response.
     */
    private void dispatchRequest(final Request request, final Response response) {
        request.getAttributes().put("org.restlet.directory", this.directory);
        getClientDispatcher().handle(request, response);
    }

    /** Prevent the client from accessing resources in upper directories */
    public void preventUpperDirectoryAccess() {
        String targetUriPath = Reference.decode(targetUri);

        if (!targetUriPath.startsWith(Reference.decode(directory.getRootRef().toString()))) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

    @Override
    public Representation put(Representation entity) throws ResourceException {
        if (this.directory
                .isModifiable()) { // Transfer of PUT calls is only allowed if the readOnly flag is
            // not set.
            Request contextRequest = new Request(Method.PUT, this.targetUri);

            // Add support for partial PUT calls.
            contextRequest.getRanges().addAll(getRanges());
            contextRequest.setEntity(entity);
            Response contextResponse = new Response(contextRequest);
            contextRequest.setResourceRef(this.targetUri);
            dispatchRequest(contextRequest, contextResponse);
            setStatus(contextResponse.getStatus());
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "The directory is not modifiable.");
        }

        return null;
    }

    /**
     * Sets the context's target URI (file, clap URI).
     *
     * @param targetUri The context's target URI.
     */
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }
}
