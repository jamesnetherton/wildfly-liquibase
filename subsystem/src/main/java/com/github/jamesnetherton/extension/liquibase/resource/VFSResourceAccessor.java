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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class VFSResourceAccessor extends ClassLoaderResourceAccessor {

    public VFSResourceAccessor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) {
        ClassLoader classLoader = toClassLoader();

        if (relativeTo != null) {
            URL parentUrl = classLoader.getResource(relativeTo);
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
            return changeLogFiles.getChildren()
                .stream()
                .map(VirtualFile::getName)
                .map(name -> path + name)
                .collect(Collectors.toSet());
        }

        URL url = classLoader.getResource(path);
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid resource URI " + path);
        }

        return VFS.getChild(uri)
            .getChildren()
            .stream()
            .map(VirtualFile::getName)
            .map(name -> path + name)
            .collect(Collectors.toSet());
    }

}
