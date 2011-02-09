/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

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
public class DirectoryServerResource extends ServerResource {

    /** The list of variants for the GET method. */
    private volatile List<Variant> variantsGet;

    /**
     * The local base name of the resource. For example, "foo.en" and
     * "foo.en-GB.html" return "foo".
     */
    private volatile String baseName;

    /** The base variant. */
    private volatile Variant baseVariant;

    /** The parent directory handler. */
    private volatile Directory directory;

    /** If the resource is a directory, this contains its content. */
    private volatile ReferenceList directoryContent;

    /**
     * If the resource is a directory, the non-trailing slash character leads to
     * redirection.
     */
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
        if (this.directory.isModifiable()) {
            Request contextRequest = new Request(Method.DELETE, this.targetUri);
            Response contextResponse = new Response(contextRequest);

            if (this.directoryTarget && !this.indexTarget) {
                contextRequest.setResourceRef(this.targetUri);
                getClientDispatcher().handle(contextRequest, contextResponse);
            } else {
                // Check if there is only one representation
                // Try to get the unique representation of the resource
                ReferenceList references = getVariantsReferences();
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

            setStatus(contextResponse.getStatus());
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
                    "The directory is not modifiable.");
        }

        return null;
    }

    /**
     * This initialization method aims at answering the following questions:<br>
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
     */
    @Override
    public void doInit() throws ResourceException {
        try {
            // Update the member variables
            this.directory = (Directory) getRequestAttributes().get(
                    "org.restlet.directory");
            this.relativePart = getReference().getRemainingPart(false, false);
            setNegotiated(this.directory.isNegotiatingContent());

            // Restore the original URI in case the call has been tunneled.
            if ((getApplication() != null)
                    && getApplication().getTunnelService().isExtensionsTunnel()) {
                this.originalRef = getOriginalRef();

                if (this.originalRef != null) {
                    this.originalRef.setBaseRef(getReference().getBaseRef());
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
                // Prevent the client from accessing resources in upper
                // directories
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
                    if (MediaType.TEXT_URI_LIST.equals(contextResponse
                            .getEntity().getMediaType())) {
                        this.directoryTarget = true;
                        this.fileTarget = false;
                        this.directoryContent = new ReferenceList(
                                contextResponse.getEntity());

                        if (!getReference().getPath().endsWith("/")) {
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
                        // Allows underlying helpers that do not support
                        // "content negotiation" to return the targeted file.
                        // Sometimes we immediately reach the target entity, so
                        // we return it directly.
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
                        // In this case, the trailing "/" shows that the URI
                        // must point to a directory
                        if ((getDirectory().getIndexName() != null)
                                && (getDirectory().getIndexName().length() > 0)) {
                            this.directoryUri = this.targetUri;
                            this.directoryTarget = true;

                            contextResponse = getRepresentation(this.directoryUri
                                    + getDirectory().getIndexName());
                            if (contextResponse.getEntity() != null) {
                                this.baseName = getDirectory().getIndexName();
                                this.targetUri = this.directoryUri
                                        + this.baseName;
                                this.directoryContent = new ReferenceList();
                                this.directoryContent.add(new Reference(
                                        this.targetUri));
                                this.indexTarget = true;
                            }
                        }
                    } else {
                        // Try to determine if this target URI with no trailing
                        // "/" is a directory, in order to force the
                        // redirection.
                        if ((getDirectory().getIndexName() != null)
                                && (getDirectory().getIndexName().length() > 0)) {
                            // Append the index name
                            contextResponse = getRepresentation(this.targetUri
                                    + "/" + getDirectory().getIndexName());
                            if (contextResponse.getEntity() != null) {
                                this.directoryUri = this.targetUri + "/";
                                this.baseName = getDirectory().getIndexName();
                                this.targetUri = this.directoryUri
                                        + this.baseName;
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

                // In case the request does not target a directory and the file
                // has not been found, try with the tunneled URI.
                if (isNegotiated() && !this.directoryTarget && !this.fileTarget
                        && (this.originalRef != null)) {
                    this.relativePart = getReference().getRemainingPart();

                    // The target URI does not take into account the query and
                    // fragment parts of the resource.
                    this.targetUri = new Reference(directory.getRootRef()
                            .toString() + this.relativePart).normalize()
                            .toString(false, false);
                    if (!this.targetUri.startsWith(directory.getRootRef()
                            .toString())) {
                        // Prevent the client from accessing resources in upper
                        // directories
                        this.targetUri = directory.getRootRef().toString();
                    }
                }

                if (!fileTarget || (fileContent == null)
                        || !getRequest().getMethod().isSafe()) {
                    // Try to get the directory content, in case the request
                    // does not target a directory
                    if (!this.directoryTarget) {
                        int lastSlashIndex = this.targetUri.lastIndexOf('/');
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
                                && MediaType.TEXT_URI_LIST
                                        .equals(contextResponse.getEntity()
                                                .getMediaType())) {
                            this.directoryContent = new ReferenceList(
                                    contextResponse.getEntity());
                        }
                    }

                    if (this.baseName != null) {
                        // Analyze extensions
                        this.baseVariant = new Variant();
                        Entity.updateMetadata(this.baseName, this.baseVariant,
                                true, getMetadataService());
                        this.protoVariant = new Variant();
                        Entity.updateMetadata(this.baseName, this.protoVariant,
                                false, getMetadataService());

                        // Remove stored extensions from the base name
                        this.baseName = Entity.getBaseName(this.baseName,
                                getMetadataService());
                    }

                    // Check if the resource exists or not.
                    List<Variant> variants = getVariants(Method.GET);
                    if ((variants == null) || (variants.isEmpty())) {
                        setExisting(false);
                    }
                }

                // Check if the resource is located in a sub directory.
                if (isExisting() && !this.directory.isDeeplyAccessible()) {
                    // Count the number of "/" character.
                    int index = this.relativePart.indexOf("/");
                    if (index != -1) {
                        index = this.relativePart.indexOf("/", index);
                        setExisting((index == -1));
                    }
                }
            }

            // Log results
            getLogger().info("Converted target URI: " + this.targetUri);
            getLogger().fine("Converted base name : " + this.baseName);
        } catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
    }

    @Override
    protected Representation get() throws ResourceException {
        // Content negotiation has been disabled
        // The variant that may need to meet the request conditions
        Representation result = null;

        List<Variant> variants = getVariants(Method.GET);
        if ((variants == null) || (variants.isEmpty())) {
            // Resource not found
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            if (variants.size() == 1) {
                result = (Representation) variants.get(0);
            } else {
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

                if (variantRefs.size() > 0) {
                    // Return the list of variants
                    setStatus(Status.REDIRECTION_MULTIPLE_CHOICES);
                    result = variantRefs.getTextRepresentation();
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }
        }

        return result;
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
    protected Client getClientDispatcher() {
        return getDirectory().getContext() == null ? null : getDirectory()
                .getContext().getClientDispatcher();
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
        return getClientDispatcher().handle(
                new Request(Method.GET, resourceUri));
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
            return getClientDispatcher().handle(
                    new Request(Method.GET, resourceUri));
        }

        Request request = new Request(Method.GET, resourceUri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(acceptedMediaType));
        return getClientDispatcher().handle(request);
    }

    /**
     * Allows to sort the list of representations set by the resource.
     * 
     * @return A Comparator instance imposing a sort order of representations or
     *         null if no special order is wanted.
     */
    private Comparator<Representation> getRepresentationsComparator() {
        // Sort the list of representations by their identifier.
        Comparator<Representation> identifiersComparator = new Comparator<Representation>() {
            public int compare(Representation rep0, Representation rep1) {
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

                return rep0.getLocationRef().getLastSegment()
                        .compareTo(rep1.getLocationRef().getLastSegment());
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

    @Override
    public List<Variant> getVariants() {
        return getVariants(getMethod());
    }

    /**
     * Returns the list of variants for the given method.
     * 
     * @param method
     *            The related method.
     * @return The list of variants for the given method.
     */
    protected List<Variant> getVariants(Method method) {
        List<Variant> result = null;

        if ((Method.GET.equals(method) || Method.HEAD.equals(method))) {
            if (variantsGet != null) {
                result = variantsGet;
            } else {
                getLogger().info("Getting variants for : " + getTargetUri());

                if ((this.directoryContent != null) && (getReference() != null)
                        && (getReference().getBaseRef() != null)) {

                    // Allows to sort the list of representations
                    SortedSet<Representation> resultSet = new TreeSet<Representation>(
                            getRepresentationsComparator());

                    // Compute the base reference (from a call's client point of
                    // view)
                    String baseRef = getReference().getBaseRef().toString(
                            false, false);

                    if (!baseRef.endsWith("/")) {
                        baseRef += "/";
                    }

                    int lastIndex = this.relativePart.lastIndexOf("/");

                    if (lastIndex != -1) {
                        baseRef += this.relativePart.substring(0, lastIndex);
                    }

                    int rootLength = getDirectoryUri().length();

                    if (this.baseName != null) {
                        String filePath;
                        for (Reference ref : getVariantsReferences()) {
                            // Add the new variant to the result list
                            Response contextResponse = getRepresentation(ref
                                    .toString());
                            if (contextResponse.getStatus().isSuccess()
                                    && (contextResponse.getEntity() != null)) {
                                filePath = ref.toString(false, false)
                                        .substring(rootLength);
                                Representation rep = contextResponse
                                        .getEntity();

                                if (filePath.startsWith("/")) {
                                    rep.setLocationRef(baseRef + filePath);
                                } else {
                                    rep.setLocationRef(baseRef + "/" + filePath);
                                }

                                resultSet.add(rep);
                            }
                        }
                    }

                    if (!resultSet.isEmpty()) {
                        result = new ArrayList<Variant>(resultSet);
                    }

                    if (resultSet.isEmpty()) {
                        if (this.directoryTarget
                                && getDirectory().isListingAllowed()) {
                            ReferenceList userList = new ReferenceList(
                                    this.directoryContent.size());
                            // Set the list identifier
                            userList.setIdentifier(baseRef);

                            SortedSet<Reference> sortedSet = new TreeSet<Reference>(
                                    getDirectory().getComparator());
                            sortedSet.addAll(this.directoryContent);

                            for (Reference ref : sortedSet) {
                                String filePart = ref.toString(false, false)
                                        .substring(rootLength);
                                StringBuilder filePath = new StringBuilder();
                                if ((!baseRef.endsWith("/"))
                                        && (!filePart.startsWith("/"))) {
                                    filePath.append('/');
                                }
                                filePath.append(filePart);
                                userList.add(baseRef + filePath);
                            }
                            List<Variant> list = getDirectory()
                                    .getIndexVariants(userList);
                            for (Variant variant : list) {
                                if (result == null) {
                                    result = new ArrayList<Variant>();
                                }

                                result.add(getDirectory()
                                        .getIndexRepresentation(variant,
                                                userList));
                            }

                        }
                    }
                } else if (this.fileTarget && (this.fileContent != null)) {
                    // Sets the location of the target representation.
                    if (getOriginalRef() != null) {
                        this.fileContent.setLocationRef(getRequest()
                                .getOriginalRef());
                    } else {
                        this.fileContent.setLocationRef(getReference());
                    }

                    result = new ArrayList<Variant>();
                    result.add(this.fileContent);
                }

                this.variantsGet = result;
            }
        }

        return result;
    }

    /**
     * Returns the references of the representations of the target resource
     * according to the directory handler property
     * 
     * @return The list of variants references
     */
    private ReferenceList getVariantsReferences() {
        ReferenceList result = new ReferenceList(0);

        try {
            this.uniqueReference = null;

            // Ask for the list of all variants of this resource
            Response contextResponse = getRepresentation(this.targetUri,
                    MediaType.TEXT_URI_LIST);
            if (contextResponse.getEntity() != null) {
                // Test if the given response is the list of all variants for
                // this resource
                if (MediaType.TEXT_URI_LIST.equals(contextResponse.getEntity()
                        .getMediaType())) {
                    ReferenceList listVariants = new ReferenceList(
                            contextResponse.getEntity());
                    String entryUri;
                    String fullEntryName;
                    String baseEntryName;
                    int lastSlashIndex;
                    int firstDotIndex;

                    for (Reference ref : listVariants) {
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
                            // Test if the variant is included in the base
                            // prototype variant
                            Variant variant = new Variant();
                            Entity.updateMetadata(fullEntryName, variant, true,
                                    getMetadataService());
                            if (this.protoVariant.includes(variant)) {
                                result.add(ref);
                            }

                            // Test if the variant is equal to the base variant
                            if (this.baseVariant.equals(variant)) {
                                // The unique reference has been found.
                                this.uniqueReference = ref;
                            }
                        }
                    }
                } else {
                    result.add(contextResponse.getEntity().getLocationRef());
                }
            }
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to get resource variants",
                    ioe);
        }

        return result;
    }

    @Override
    public Representation handle() {
        Representation result = null;

        if (this.directoryRedirection) {
            if (this.originalRef != null) {
                if (this.originalRef.hasQuery()) {
                    redirectSeeOther(this.originalRef.getPath() + "/?"
                            + this.originalRef.getQuery());
                } else {
                    redirectSeeOther(this.originalRef.getPath() + "/");
                }
            } else {
                if (getReference().hasQuery()) {
                    redirectSeeOther(getReference().getPath() + "/?"
                            + getReference().getQuery());
                } else {
                    redirectSeeOther(getReference().getPath() + "/");
                }
            }
        } else {
            result = super.handle();
        }

        return result;
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
    public Representation put(Representation entity) throws ResourceException {
        if (this.directory.isModifiable()) {
            // Transfer of PUT calls is only allowed if the readOnly flag is
            // not set.
            Request contextRequest = new Request(Method.PUT, this.targetUri);

            // Add support of partial PUT calls.
            contextRequest.getRanges().addAll(getRanges());
            contextRequest.setEntity(entity);
            Response contextResponse = new Response(contextRequest);
            contextRequest.setResourceRef(this.targetUri);
            getClientDispatcher().handle(contextRequest, contextResponse);
            setStatus(contextResponse.getStatus());
        } else {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
                    "The directory is not modifiable.");
        }

        return null;
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
}
