/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.odata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.odata.internal.edm.ComplexType;
import org.restlet.ext.odata.internal.edm.EntityContainer;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Schema;
import org.restlet.ext.odata.internal.edm.TypeUtils;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import freemarker.template.Configuration;

/**
 * Code generator for accessing OData services. The generator use metadata
 * exposed by an online service to generate client-side artifacts facilitating
 * the execution of queries on the available entities.
 * 
 * @author Thierry Boileau
 */
public class Generator {

    /**
     * Takes two (or three) parameters:<br>
     * <ol>
     * <li>The URI of the OData service</li>
     * <li>The output directory (optional, used the current directory by
     * default)</li>
     * <li>The name of the generated service class name (optional)</li>
     * </ol>
     * 
     * @param args
     *            The list of arguments.
     */
    public static void main(String[] args) {
        System.out.println("---------------------------");
        System.out.println("OData client code generator");
        System.out.println("---------------------------");
        System.out.println("step 1 - check parameters");

        String errorMessage = null;

        if (args == null || args.length == 0) {
            errorMessage = "Missing mandatory argument: URI of the OData service.";
        }

        File outputDir = null;

        if (errorMessage == null) {
            if (args.length > 1) {
                outputDir = new File(args[1]);
            } else {
                try {
                    outputDir = new File(".").getCanonicalFile();
                    if (outputDir.exists()) {
                        System.out.println("step 2 - check the ouput directory");
                        if (!outputDir.isDirectory()) {
                            errorMessage = outputDir.getPath()
                                    + " is not a valid directory.";
                        }

                    } else {
                        try {
                            System.out.println("step 2 - create the ouput directory");
                            outputDir.mkdirs();
                        } catch (Throwable e) {
                            errorMessage = "Cannot create " + outputDir.getPath()
                                    + " due to: " + e.getMessage();
                        }
                    }
                } catch (IOException e) {
                    errorMessage = "Unable to get the target directory. "
                            + e.getMessage();
                }
            }
        }
        if (errorMessage == null) {
            System.out.println("step 3 - get the metadata descriptor");
            String dataServiceUri = null;

            if (args[0].endsWith("$metadata")) {
                dataServiceUri = args[0].substring(0, args[0].length() - 10);
            } else if (args[0].endsWith("/")) {
                dataServiceUri = args[0].substring(0, args[0].length() - 1);
            } else {
                dataServiceUri = args[0];
            }

            Service service = new Service(dataServiceUri);
            if (service.getMetadata() == null) {
                errorMessage = "Cannot retrieve the metadata.";
            } else {
                System.out.println("step 4 - generate source code");
                Generator svcUtil = null;
                if (args.length == 3) {
                    svcUtil = new Generator(service.getServiceRef(), args[2]);
                } else {
                    svcUtil = new Generator(service.getServiceRef());
                }

                try {
                    svcUtil.generate(outputDir);
                    System.out
                            .print("The source code has been generated in directory: ");
                    System.out.println(outputDir.getPath());
                } catch (Exception e) {
                    errorMessage = "Cannot generate the source code in directory: "
                            + outputDir.getPath();
                }
            }
        }

        if (errorMessage != null) {
            System.out.println("An error occurred: ");
            System.out.println(errorMessage);
            System.out.println();
            System.out
                    .println("Please check that you provide the following parameters:");
            System.out.println("   - Valid URI for the remote service");
            System.out
                    .println("   - Valid directory path where to generate the files");
            System.out
                    .println("   - Valid name for the generated service class (optional)");
        }
    }

    /** The name of the service class (in case there is only one in the schema). */
    private String serviceClassName;

    /** The URI of the target data service. */
    private Reference serviceRef;

    /**
     * Constructor.
     * 
     * @param serviceRef
     *            The URI of the OData service.
     */
    public Generator(Reference serviceRef) {
        this(serviceRef, null);
    }

    /**
     * Constructor. The name of the service class can be provided if there is
     * only one service defined in the metadata.
     * 
     * @param serviceRef
     *            The URI of the OData service.
     * @param serviceClassName
     *            The name of the service class (in case there is only one in
     *            the metadata).
     */
    public Generator(Reference serviceRef, String serviceClassName) {
        super();
        this.serviceRef = serviceRef;
        if (serviceClassName != null) {
            this.serviceClassName = ReflectUtils.normalize(serviceClassName);
            this.serviceClassName = this.serviceClassName.substring(0, 1)
                    .toUpperCase() + this.serviceClassName.substring(1);
        }

    }

    /**
     * Constructor.
     * 
     * @param serviceUri
     *            The URI of the OData service.
     */
    public Generator(String serviceUri) {
        this(serviceUri, null);
    }

    /**
     * Constructor. The name of the service class can be provided if there is
     * only one service defined in the metadata.
     * 
     * @param serviceUri
     *            The URI of the OData service.
     * @param serviceClassName
     *            The name of the service class (in case there is only one in
     *            the metadata).
     */
    public Generator(String serviceUri, String serviceClassName) {
        this(new Reference(serviceUri), serviceClassName);
    }

    /**
     * Generates the client code to the given output directory.
     * 
     * @param outputDir
     *            The output directory.
     * @throws Exception
     */
    public void generate(File outputDir) throws Exception {
        Service service = new Service(serviceRef);
        Metadata metadata = (Metadata) service.getMetadata();
        if (metadata == null) {
            throw new Exception("Can't get the metadata for this service: "
                    + serviceRef);
        }

        Configuration fmc = new Configuration();
        fmc.setDefaultEncoding(CharacterSet.UTF_8.getName());

        // Generate classes
        String rootTemplates = "clap://class/org/restlet/ext/odata/internal/templates";
        Representation complexTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/complexType.ftl").get()
                        .getText());
        Representation entityTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/entityType.ftl").get()
                        .getText());
        Representation serviceTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/service.ftl").get()
                        .getText());

        for (Schema schema : metadata.getSchemas()) {
            if ((schema.getEntityTypes() != null && !schema.getEntityTypes()
                    .isEmpty())
                    || (schema.getComplexTypes() != null && !schema
                            .getComplexTypes().isEmpty())) {
                String packageName = TypeUtils.getPackageName(schema);
                File packageDir = new File(outputDir, packageName.replace(".",
                        System.getProperty("file.separator")));
                packageDir.mkdirs();

                // For each entity type
                for (EntityType type : schema.getEntityTypes()) {
                    String className = type.getClassName();
                    Map<String, Object> dataModel = new HashMap<String, Object>();
                    dataModel.put("type", type);
                    dataModel.put("schema", schema);
                    dataModel.put("metadata", metadata);
                    dataModel.put("className", className);
                    dataModel.put("packageName", packageName);

                    TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                            entityTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
                    templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

                    // Write the template representation as a Java class
                    OutputStream fos = new FileOutputStream(new File(
                            packageDir, type.getClassName() + ".java"));
                    templateRepresentation.write(fos);
                    fos.flush();
                }

                for (ComplexType type : schema.getComplexTypes()) {
                    String className = type.getClassName();
                    Map<String, Object> dataModel = new HashMap<String, Object>();
                    dataModel.put("type", type);
                    dataModel.put("schema", schema);
                    dataModel.put("metadata", metadata);
                    dataModel.put("className", className);
                    dataModel.put("packageName", packageName);

                    TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                            complexTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
                    templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

                    // Write the template representation as a Java class
                    OutputStream fos = new FileOutputStream(new File(
                            packageDir, type.getClassName() + ".java"));
                    templateRepresentation.write(fos);
                    fos.flush();
                }
            }
        }
        if (metadata.getContainers() != null
                && !metadata.getContainers().isEmpty()) {
            for (EntityContainer entityContainer : metadata.getContainers()) {
                Schema schema = entityContainer.getSchema();
                // Generate Service subclass
                StringBuffer className = new StringBuffer();

                if (serviceClassName != null) {
                    // Try to use the Client preference
                    if (entityContainer.isDefaultEntityContainer()) {
                        className.append(serviceClassName);
                    } else if (metadata.getContainers().size() == 1) {
                        className.append(serviceClassName);
                    } else {
                        className.append(schema.getNamespace()
                                .getNormalizedName().substring(0, 1)
                                .toUpperCase());
                        className.append(schema.getNamespace()
                                .getNormalizedName().substring(1));
                        className.append("Service");
                    }
                } else {
                    className.append(schema.getNamespace().getNormalizedName()
                            .substring(0, 1).toUpperCase());
                    className.append(schema.getNamespace().getNormalizedName()
                            .substring(1));
                    className.append("Service");
                }

                Map<String, Object> dataModel = new HashMap<String, Object>();
                dataModel.put("schema", schema);
                dataModel.put("metadata", metadata);
                dataModel.put("className", className);
                dataModel.put("dataServiceUri", this.serviceRef.getTargetRef());
                dataModel.put("entityContainer", entityContainer);

                TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                        serviceTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
                templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

                // Write the template representation as a Java class
                OutputStream fos = new FileOutputStream(new File(outputDir,
                        className + ".java"));
                templateRepresentation.write(fos);
                fos.flush();
            }
        }
    }

    /**
     * Generates the client code to the given output directory.
     * 
     * @param outputDir
     *            The output directory.
     * @throws Exception
     */
    public void generate(String outputDir) throws Exception {
        generate(new File(outputDir));
    }
}
