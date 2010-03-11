/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.odata;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Schema;
import org.restlet.ext.odata.internal.edm.Type;
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
     * Takes two parameters :<br>
     * <ol>
     * <li>the URI of the WCF Data Service</li>
     * <li>the output directory</li>
     * </ol>
     * 
     * @param args
     *            The list of arguments.
     */
    public static void main(String[] args) {
        boolean error = (args.length != 2 && args.length != 3);
        String step = "step 1 - check parameters";
        File outputDir = null;
        Metadata metadata;
        if (!error) {
            outputDir = new File(args[1]);
            if (outputDir.exists()) {
                step = "step 2 - check the ouput directory";
                error = !outputDir.isDirectory();
            } else {
                try {
                    step = "step 3 - create the ouput directory";
                    outputDir.mkdirs();
                } catch (Throwable e) {
                    error = true;
                }
            }
        }
        if (!error) {
            String dataServiceUri = null;
            if (args[0].endsWith("$metadata")) {
                dataServiceUri = args[0].substring(0, args[0].length() - 10);
            } else if (args[0].endsWith("/")) {
                dataServiceUri = args[0].substring(0, args[0].length() - 1);
            } else {
                dataServiceUri = args[0];
            }

            step = "step 4 - get the metadata descriptor";
            Service service = new Service(dataServiceUri);
            if ((metadata = ((Metadata) service.getMetadata())) == null) {
                error = true;
            } else {
                step = "step 5 - generate source code";
                Generator svcUtil = null;
                if (args.length == 3) {
                    svcUtil = new Generator(new Reference(dataServiceUri),
                            metadata, args[2]);
                } else {
                    svcUtil = new Generator(new Reference(dataServiceUri),
                            metadata);
                }

                try {
                    svcUtil.generateSourceCode(outputDir);
                } catch (Exception e) {
                    error = true;
                }
            }
        }

        if (error) {
            System.out.println("WCF Data Services code generation tool");
            System.out.println("******************************************");
            System.out.println("Error encountered at this step: ");
            System.out.println(step);
            System.out.println();
            System.out
                    .println("Please check that you provide the following parameters:");
            System.out.println("   - a valid URI for the remote service");
            System.out
                    .println("   - a valid directory path where to generate the files.");
            System.out
                    .println("   - a valid name for the generated service class (optional).");
        }
    }

    /** The URI of the target data service. */
    private Reference dataServiceRef;

    /** The WCF Data Services metadata. */
    private Metadata metadata;

    /** The name of the service class (in case there is only one in the schema). */
    private String serviceClassName;

    /**
     * Constructor.
     * 
     * @param dataServiceRef
     *            The URI of the WCF Data Service.
     * @param metadata
     *            The metadata descriptor.
     */
    public Generator(Reference dataServiceRef, Metadata metadata) {
        this(dataServiceRef, metadata, null);
    }

    /**
     * Constructor. The name of the service class can be provided if there is
     * only one service defined in the metadata.
     * 
     * @param dataServiceRef
     *            The URI of the WCF Data Service.
     * @param metadata
     *            The metadata descriptor.
     * @param serviceClassName
     *            The name of the service class (in case there is only one in
     *            the matadata).
     */
    public Generator(Reference dataServiceRef, Metadata metadata,
            String serviceClassName) {
        super();
        this.metadata = metadata;
        this.dataServiceRef = dataServiceRef;
        if (serviceClassName != null) {
            this.serviceClassName = ReflectUtils.normalize(serviceClassName);
            this.serviceClassName = this.serviceClassName.substring(0, 1)
                    .toUpperCase()
                    + this.serviceClassName.substring(1);
        }

    }

    /**
     * Generates the code to the given output directory.
     * 
     * @param outputDir
     *            The output directory.
     * @throws Exception
     */
    public void generateSourceCode(File outputDir) throws Exception {
        Configuration fmc = new Configuration();
        fmc.setDefaultEncoding(CharacterSet.UTF_8.getName());

        // Generate classes
        String rootTemplates = "clap://class/org/restlet/ext/odata/internal/templates";
        Representation entityTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/entityType.ftl").get()
                        .getText());
        Representation serviceTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/service.ftl").get()
                        .getText());

        for (Schema schema : metadata.getSchemas()) {
            String packageName = Type.getPackageName(schema);
            File packageDir = new File(outputDir, packageName.replace(".",
                    System.getProperty("file.separator")));
            packageDir.mkdirs();
            // For each class
            for (EntityType type : schema.getTypes()) {
                String className = type.getClassName();
                Map<String, Object> dataModel = new HashMap<String, Object>();
                dataModel.put("entityType", type);
                dataModel.put("schema", schema);
                dataModel.put("metadata", metadata);
                dataModel.put("className", className);
                dataModel.put("packageName", packageName);

                TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                        entityTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
                templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

                // Write the template representation as a Java class
                templateRepresentation.write(new FileOutputStream(new File(
                        packageDir, type.getClassName() + ".java")));
            }

            // Generate Service subclass.
            StringBuffer className = new StringBuffer();
            if (serviceClassName != null && metadata.getSchemas().size() == 1) {
                className.append(serviceClassName);
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
            dataModel.put("dataServiceUri", this.dataServiceRef.getTargetRef());
            dataModel.put("entityContainers", metadata.getContainers());

            TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                    serviceTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
            templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

            // Write the template representation as a Java class
            templateRepresentation.write(new FileOutputStream(new File(
                    outputDir, className + ".java")));
        }
    }
}
