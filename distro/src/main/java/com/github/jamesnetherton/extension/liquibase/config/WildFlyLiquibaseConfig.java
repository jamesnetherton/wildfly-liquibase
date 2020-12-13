/*-
 * #%L
 * wildfly-liquibase-distro
 * %%
 * Copyright (C) 2017 James Netherton
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.jamesnetherton.extension.liquibase.config;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class WildFlyLiquibaseConfig {

    private static final String WILDFLY_LIQUIBASE_EXTENSION_XML = "extension.xml";
    private static final String WILDFLY_LIQUIBASE_SUBSYTEM_XML = "subsystem.xml";

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new RuntimeException("Usage WildFlyLiquibaseConfig <config source> <config target>");
        }

        final String configSource = args[0];
        final String configDest = args[1];
        final Path destPath = Paths.get(configDest);
        final SAXBuilder builder = new SAXBuilder();

        System.out.println("Applying wildfly-liquibase configuration:");

        destPath.toFile().mkdirs();
        File source = new File(configSource);
        File[] files = source.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            }
        });
        if (files != null) {
            for (File file : files) {
                try {
                    applyWildFlyLiquibaseConfig(builder.build(file), destPath, file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException("No usable files found in source: " + configSource);
        }
    }

    private static void applyWildFlyLiquibaseConfig(Document document, Path configDest, File file) throws IOException {
        Path configFilePath = getLiquibaseConfigFile(configDest, file);
        applyWildFlyLiquibaseExtension(document);
        applyWildFlyLiquibaseSubsystem(document);
        writeConfigChanges(configFilePath, document);
    }

    private static void applyWildFlyLiquibaseExtension(Document document) {
        Element rootElement = document.getRootElement();
        Element extensions = rootElement.getChild("extensions", rootElement.getNamespace());
        extensions.addContent("    ");
        extensions.addContent(readResource(WILDFLY_LIQUIBASE_EXTENSION_XML));
        extensions.addContent("\n    ");
    }

    private static void applyWildFlyLiquibaseSubsystem(Document document) {
        Element rootElement = document.getRootElement();
        Element profile = rootElement.getChild("profile", rootElement.getNamespace());
        profile.addContent("    ");
        profile.addContent(readResource(WILDFLY_LIQUIBASE_SUBSYTEM_XML));
        profile.addContent("\n    ");
    }

    private static Element readResource(String resourceName) {
        InputStream resource = WildFlyLiquibaseConfig.class.getResourceAsStream(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Resource not found:" + resourceName);
        }

        try {
            SAXBuilder builder = new SAXBuilder();
            Document subsystemDocument = builder.build(resource);
            return subsystemDocument.detachRootElement();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Path getLiquibaseConfigFile(Path configDest, File file) {
        String fileName = file.getName();
        String filePrefix = fileName.substring(0, fileName.indexOf("."));
        return configDest.resolve(filePrefix + "-liquibase.xml");
    }

    private static void writeFile(Path path, String value, String encoding) throws IOException {
        byte[] bytes = value.getBytes(encoding);
        Files.write(path, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    private static void writeConfigChanges(Path configDest, Document document) throws IOException {
        XMLOutputter output = new XMLOutputter();
        output.setFormat(Format.getRawFormat().setLineSeparator(System.lineSeparator()));
        String newXML = output.outputString(document);
        writeFile(configDest, newXML, "UTF-8");
    }
}
