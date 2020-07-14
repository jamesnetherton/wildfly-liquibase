/*-
 * #%L
 * wildfly-liquibase-subsystem
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
package com.github.jamesnetherton.extension.liquibase.resource;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class VFSResourceAccessor extends ClassLoaderResourceAccessor {

    protected final ChangeLogConfiguration configuration;
    private static final String VFS_CONTENTS_PATH_MARKER = "contents";

    public VFSResourceAccessor(ChangeLogConfiguration configuration) {
        super(configuration.getClassLoader());
        this.configuration = configuration;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String path) throws IOException {
        InputStreamList resources = new InputStreamList();
        ClassLoader classLoader = configuration.getClassLoader();

        // TODO: Improve this as it could potentially fail in some edge case scenarios
        if (path.contains("/vfs/")) {
            int index = path.indexOf(VFS_CONTENTS_PATH_MARKER);
            if (index > -1) {
                String resolvedPath = path.substring(index + VFS_CONTENTS_PATH_MARKER.length());
                InputStream resource = classLoader.getResourceAsStream(resolvedPath);
                if (resource != null) {
                    try {
                        resources.add(new URI(resolvedPath), resource);
                    } catch (URISyntaxException e) {
                        throw new IllegalStateException("Invalid URI path: " + resolvedPath);
                    }
                }
            }
            return resources;
        }

        InputStream resource = classLoader.getResourceAsStream(path);
        if (resource != null) {
            try {
                resources.add(new URI(path), resource);
                return resources;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid URI path: " + path);
            }
        }

        return super.openStreams(relativeTo, path);
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) {
        SortedSet<String> resources = new TreeSet<>();
        ClassLoader classLoader = configuration.getClassLoader();

        if (relativeTo != null) {
            String tempPath =  configuration.getPath().replace("/content/" + configuration.getDeployment(), "");
            final String parentPath = tempPath.replace(configuration.getFileName(), "");
            URL parentUrl = classLoader.getResource(parentPath + path);

            if (parentUrl == null) {
                throw new IllegalStateException("Cannot locate resource parent of " + relativeTo);
            }

            URI parentUri;
            try {
                parentUri = parentUrl.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid parent resource URI " + parentUrl.toString());
            }

            VirtualFile parentFile = VFS.getChild(parentUri);
            VirtualFile parentDir = parentFile.getParent();
            VirtualFile changeLogFiles = parentDir.getChild(path);
            changeLogFiles.getChildren()
                .stream()
                .map(VirtualFile::getName)
                .map(name -> parentPath + path + name)
                .forEach(resources::add);
        }

        URL url = classLoader.getResource(path);
        if (url != null) {
            URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid resource URI " + path);
            }

            VFS.getChild(uri)
                .getChildren()
                .stream()
                .map(VirtualFile::getName)
                .map(name -> path + name)
                .forEach(resources::add);
        }
        return resources;
    }
}
