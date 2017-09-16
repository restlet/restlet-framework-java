package org.restlet.edition;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-edition")
public class EditionGeneratorMojo extends AbstractMojo {

    @Parameter( property = "edition", defaultValue = "jse" )
    private String edition;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        MavenProject project = session.getCurrentProject();


        for (String compileSourceRoot: project.getCompileSourceRoots()) {
            Path compileRoot = Paths.get(compileSourceRoot);
            try {
                Files.walkFileTree(compileRoot, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                        if (Files.isRegularFile(file)) {
                            System.out.println("Generating " + file);
                            CodeBlockFilter filter = new CodeBlockFilter(new InputStreamReader(Files.newInputStream(file)), edition);
                            System.out.println(readAll(filter));
                        }

                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("project project.getResources() " + project.getResources());
        }

    }

    private static List<String> readAll(Reader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader b = new BufferedReader(reader)) {
            String line;
            while ((line = b.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }
}
