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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import freemarker.template.Configuration;

/**
 * Code generator for WCF data services.
 * 
 * @author Thierry Boileau
 */
public class Generator {

    /**
     * Takes two parameters :<br>
     * <ol>
     * <li>the URI of the WCF data service</li>
     * <li>the output directory</li>
     * </ol>
     * 
     * @param args
     *            The list of arguments.
     */
    public static void main(String[] args) {
        boolean error = (args.length != 2);
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
            Service session = new Service(dataServiceUri);
            if ((metadata = session.getMetadata()) == null) {
                error = true;
            } else {
                step = "step 5 - generate source code";
                Generator svcUtil = new Generator(
                        new Reference(dataServiceUri), metadata);
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
        }
    }

    /** The WCF Data Services metadata. */
    private Metadata metadata;

    /** The URI of the target data service. */
    private Reference dataServiceRef;

    /**
     * Constructor.
     * 
     * @param dataServiceRef
     *            The URI of the WCF data service.
     * @param metadata
     *            The metadata descriptor.
     */
    public Generator(Reference dataServiceRef, Metadata metadata) {
        super();
        this.metadata = metadata;
        this.dataServiceRef = dataServiceRef;
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
        String rootTemplates = "clap://class/org/restlet/ext/dataservices/internal/templates";
        Representation entityTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/entityType.ftl").get()
                        .getText());
        Representation sessionTmpl = new StringRepresentation(
                new ClientResource(rootTemplates + "/session.ftl").get()
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

            // Generate Session subclass.
            StringBuffer className = new StringBuffer();
            className.append(schema.getNamespace().getNormalizedName()
                    .substring(0, 1).toUpperCase());
            className.append(schema.getNamespace().getNormalizedName()
                    .substring(1));
            className.append("Session");

            Map<String, Object> dataModel = new HashMap<String, Object>();
            dataModel.put("schema", schema);
            dataModel.put("metadata", metadata);
            dataModel.put("className", className);
            dataModel.put("dataServiceUri", this.dataServiceRef.getTargetRef());
            dataModel.put("entityContainers", metadata.getContainers());

            TemplateRepresentation templateRepresentation = new TemplateRepresentation(
                    sessionTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
            templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

            // Write the template representation as a Java class
            templateRepresentation.write(new FileOutputStream(new File(
                    outputDir, className + ".java")));
        }
    }
}
