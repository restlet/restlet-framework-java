/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.local;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.service.MetadataService;

/**
 * Resource supported by a set of context representations (from file system,
 * class loaders and webapp context). A content negotiation mechanism (similar
 * to Apache HTTP server) is available. It is based on path extensions to detect
 * variants (languages, media types or character sets).
 * 
 * @see <a
 *      href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache
 *      mod_negotiation module</a>
 * @author Jerome Louvel (contact@noelios.com)
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
        Set<String> result = new TreeSet<String>();
        String[] tokens = entryName.split("\\.");
        for (int i = 1; i < tokens.length; i++) {
            result.add(tokens[i].toLowerCase());
        }
        return result;
    }

    /** The parent directory handler. */
    private Directory directory;

    /** The resource path relative to the directory URI. */
    private String relativePart;

    /** The context's target URI (file, clap or war URI). */
    private String targetUri;

    /** Indicates if the target resource is a directory or a file. */
    private boolean targetDirectory;

    /** Indicates if the target resource is a directory with an index. */
    private boolean targetIndex;

    /** The context's directory URI (file, clap or war URI). */
    private String directoryUri;

    /**
     * The local base name of the resource. For example, "foo.en" and
     * "foo.en-GB.html" return "foo".
     */
    private String baseName;

    /** The base set of extensions. */
    private Set<String> baseExtensions;

    /** The unique representation of the target URI, if it exists. */
    private Reference uniqueReference;

    /** If the resource is a directory, this contains its content. */
    private ReferenceList directoryContent;

    /**
     * If the resource is a directory, the non-trailing slash caracter leads to
     * redirection.
     */
    private boolean directoryRedirection;

    /**
     * Constructor.
     * 
     * @param directory
     *            The parent directory handler.
     * @param request
     *            The handled call.
     * @throws IOException
     */
    public DirectoryResource(Directory directory, Request request,
            Response response) throws IOException {
        super(directory.getContext(), request, response);

        // Update the member variables
        this.directory = directory;
        this.relativePart = request.getResourceRef().getRemainingPart();

        if (this.relativePart.startsWith("/")) {
            // We enforce the leading slash on the root URI
            this.relativePart = this.relativePart.substring(1);
        }
        // The target uri does not take into account the query and fragment
        // parts of the resource.
        this.targetUri = new Reference(directory.getRootRef().toString()
                + this.relativePart).normalize().toString(false, false);
        if (!this.targetUri.startsWith(directory.getRootRef().toString())) {
            // Prevent the client from accessing resources in upper directories
            this.targetUri = directory.getRootRef().toString();
        }

        // Try to detect the presence of a directory
        Response contextResponse = getDispatcher().get(this.targetUri);
        if ((contextResponse.getEntity() != null)
                && contextResponse.getEntity().getMediaType().equals(
                        MediaType.TEXT_URI_LIST)) {
            this.targetDirectory = true;
            this.directoryContent = new ReferenceList(contextResponse
                    .getEntity());
            if (!request.getResourceRef().getIdentifier().endsWith("/")) {
                this.directoryRedirection = true; // all request will be
                // automatically redirected
            }

            if (!this.targetUri.endsWith("/")) {
                this.targetUri += "/";
                this.relativePart += "/";
            }

            // Append the index name
            if (getMetadataService().getIndexName() != null
                    && getMetadataService().getIndexName().length() > 0) {
                this.directoryUri = this.targetUri;
                this.baseName = getMetadataService().getIndexName();
                this.targetUri = this.directoryUri + this.baseName;
                this.targetIndex = true;
            } else {
                this.directoryUri = this.targetUri;
                this.baseName = null;
            }
        } else {
            this.targetDirectory = false;
            int lastSlashIndex = targetUri.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                this.directoryUri = "";
                this.baseName = targetUri;
            } else {
                this.directoryUri = targetUri.substring(0, lastSlashIndex + 1);
                this.baseName = targetUri.substring(lastSlashIndex + 1);
            }

            contextResponse = getDispatcher().get(this.directoryUri);
            if ((contextResponse.getEntity() != null)
                    && contextResponse.getEntity().getMediaType().equals(
                            MediaType.TEXT_URI_LIST)) {
                this.directoryContent = new ReferenceList(contextResponse
                        .getEntity());
            }
        }

        if (this.baseName != null) {
            // Remove the extensions from the base name
            int firstDotIndex = this.baseName.indexOf('.');
            if (firstDotIndex != -1) {
                // Store the set of extensions
                this.baseExtensions = getExtensions(this.baseName);

                // Remove stored extensions from the base name
                this.baseName = this.baseName.substring(0, firstDotIndex);
            }

        }

        // Log results
        getLogger().info("Converted base path: " + this.targetUri);
        getLogger().info("Converted base name: " + this.baseName);
    }

    /**
     * Indicates if it is allowed to delete the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowDelete() {
        return getDirectory().isModifiable();
    }

    /**
     * Indicates if it is allowed to put to the resource. The default value is
     * false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPut() {
        return getDirectory().isModifiable();
    }

    @Override
    public void handleGet() {
        if (directoryRedirection) {
            // If this request targets a directory and if the target URI does
            // not end with a tailing "/", the client is told to redirect to a
            // correct URI.
            getResponse().setStatus(Status.REDIRECTION_FOUND);
            getResponse().setEntity(null);
            getResponse().setRedirectRef(
                    getRequest().getResourceRef().getIdentifier() + "/");
        } else {
            super.handleGet();
        }

    }

    /**
     * Asks the resource to delete itself and all its representations.
     */
    public void delete() {
        Status status;

        if (directoryRedirection && !targetIndex) {
            getResponse().setStatus(Status.REDIRECTION_SEE_OTHER);
            getResponse().setRedirectRef(this.targetUri);
        } else {
            // We allow the transfer of the PUT calls only if the readOnly flag
            // is not set
            if (!getDirectory().isModifiable()) {
                status = Status.CLIENT_ERROR_FORBIDDEN;
            } else {
                Request contextRequest = new Request(Method.DELETE,
                        this.targetUri);
                Response contextResponse = new Response(contextRequest);

                if (targetDirectory && !targetIndex) {
                    contextRequest.setResourceRef(this.targetUri);
                    getDispatcher().handle(contextRequest, contextResponse);
                } else {
                    // Check if there is only one representation

                    // Try to get the unique representation of the resource
                    ReferenceList references = getVariantsReferences();
                    if (!references.isEmpty()) {
                        if (uniqueReference != null) {
                            contextRequest.setResourceRef(uniqueReference);
                            getDispatcher().handle(contextRequest,
                                    contextResponse);
                        } else {
                            // We found variants, but not the right one
                            contextResponse
                                    .setStatus(new Status(
                                            Status.CLIENT_ERROR_UNAUTHORIZED,
                                            "Unable to process properly the request. Several variants exist but none of them suits precisely. "));
                        }
                    } else {
                        contextResponse
                                .setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    }
                }

                status = contextResponse.getStatus();
            }

            getResponse().setStatus(status);
        }
    }

    /**
     * Puts a variant representation in the resource.
     * 
     * @param variant
     *            A new or updated variant representation.
     */
    public void put(Representation variant) {
        Status status;

        if (directoryRedirection && !targetIndex) {
            getResponse().setStatus(Status.REDIRECTION_SEE_OTHER);
            getResponse().setRedirectRef(this.targetUri);
        }

        // We allow the transfer of the PUT calls only if the readOnly flag is
        // not set
        if (!getDirectory().isModifiable()) {
            status = Status.CLIENT_ERROR_FORBIDDEN;
        } else {
            Request contextRequest = new Request(Method.PUT, this.targetUri);
            contextRequest.setEntity(variant);
            Response contextResponse = new Response(contextRequest);
            contextRequest.setResourceRef(this.targetUri);
            getDispatcher().handle(contextRequest, contextResponse);
            status = contextResponse.getStatus();
        }

        getResponse().setStatus(status);
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
     * Returns the parent directory handler.
     * 
     * @return The parent directory handler.
     */
    public Directory getDirectory() {
        return this.directory;
    }

    /**
     * Returns the context's directory URI (file, clap or war URI).
     * 
     * @return The context's directory URI (file, clap or war URI).
     */
    public String getDirectoryUri() {
        return this.directoryUri;
    }

    /**
     * Returns a call dispatcher.
     * 
     * @return A call dispatcher.
     */
    private Uniform getDispatcher() {
        return getDirectory().getContext().getDispatcher();
    }

    /**
     * Returns the metadata service.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        MetadataService result = null;
        Application application = (Application) getContext().getAttributes()
                .get("org.restlet.application");

        if (application != null) {
            result = application.getMetadataService();
        } else {
            result = new MetadataService();
        }

        return result;
    }

    /**
     * Returns the context's target URI (file, clap or war URI).
     * 
     * @return The context's target URI (file, clap or war URI).
     */
    public String getTargetUri() {
        return this.targetUri;
    }

    /**
     * Returns the representation variants.
     * 
     * @return The representation variants.
     */
    public List<Variant> getVariants() {
        List<Variant> results = super.getVariants();

        // Allows to sort the list of representations
        SortedSet<Representation> resultSet = new TreeSet<Representation>(
                getRepresentationsComparator());

        getLogger().info("Getting variants for : " + getTargetUri());

        // Compute the base reference (from a call's client point of view)
        String baseRef = getRequest().getResourceRef().getBaseRef().toString(
                false, false);
        if (!baseRef.endsWith("/")) {
            baseRef += "/";
        }
        int lastIndex = this.relativePart.lastIndexOf("/");
        if (lastIndex != -1) {
            baseRef += this.relativePart.substring(0, lastIndex);
        }
        int rootLength = getDirectoryUri().length();

        if (this.directoryContent != null) {
            if (this.baseName != null) {
                String filePath;
                for (Reference ref : getVariantsReferences()) {
                    // Add the new variant to the result list
                    Response contextResponse = getDispatcher().get(
                            ref.toString());
                    if (contextResponse.getStatus().isSuccess()
                            && (contextResponse.getEntity() != null)) {
                        filePath = ref.toString(false, false).substring(
                                rootLength);
                        Representation rep = contextResponse.getEntity();
                        rep.setIdentifier(baseRef + filePath);
                        resultSet.add(rep);
                    }
                }
            }

            if (resultSet.isEmpty()) {
                if (this.targetDirectory && getDirectory().isListingAllowed()) {
                    ReferenceList userList = new ReferenceList(
                            this.directoryContent.size());
                    // Set the list identifier
                    userList.setIdentifier(baseRef);

                    for (Reference ref : this.directoryContent) {
                        String filePart = ref.toString(false, false).substring(
                                rootLength);
                        StringBuilder filePath = new StringBuilder();
                        if ((!baseRef.endsWith("/"))
                                && (!filePart.startsWith("/"))) {
                            filePath.append('/');
                        }
                        filePath.append(filePart);
                        userList.add(baseRef + filePath);
                    }
                    List<Variant> list = getDirectory().getIndexVariants(
                            userList);
                    for (Variant variant : list) {
                        resultSet.add(getDirectory().getIndexRepresentation(
                                variant, userList));
                    }

                }
            }
        }

        results.addAll(resultSet);
        return results;
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
                boolean bRep0Null = (rep0.getIdentifier() == null);
                boolean bRep1Null = (rep1.getIdentifier() == null);

                if (bRep0Null && bRep1Null) {
                    return 0;
                } else {
                    if (bRep0Null) {
                        return -1;
                    } else {
                        if (bRep1Null) {
                            return 1;
                        } else {
                            return rep0.getIdentifier().getLastSegment()
                                    .compareTo(
                                            rep1.getIdentifier()
                                                    .getLastSegment());
                        }
                    }
                }
            }
        };
        return identifiersComparator;
    }

    /**
     * Returns the references of the representations of the target resource
     * according to the directory handler property
     * 
     * @return The list of variants references
     */
    private ReferenceList getVariantsReferences() {
        uniqueReference = null;
        ReferenceList result = new ReferenceList(0);
        try {
            Request contextCall = new Request(Method.GET, this.targetUri);
            contextCall.getClientInfo().getAcceptedMediaTypes().add(
                    new Preference<MediaType>(MediaType.TEXT_URI_LIST));
            Response contextResponse = getDispatcher().handle(contextCall);
            if (contextResponse.getEntity() != null) {
                ReferenceList listVariants = new ReferenceList(contextResponse
                        .getEntity());
                Set<String> extensions = null;
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
                        boolean validVariant = true;

                        // Verify that the extensions are compatible
                        extensions = getExtensions(fullEntryName);
                        validVariant = (((extensions == null) && (this.baseExtensions == null))
                                || (this.baseExtensions == null) || extensions
                                .containsAll(this.baseExtensions));

                        if (validVariant && (this.baseExtensions != null)
                                && this.baseExtensions.containsAll(extensions)) {
                            // The unique reference has been found.
                            uniqueReference = ref;
                        }

                        if (validVariant) {
                            result.add(ref);
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to get resource variants",
                    ioe);
        }

        return result;
    }

    /**
     * Sets the context's target URI (file, clap or war URI).
     * 
     * @param targetUri
     *            The context's target URI.
     */
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }
}
