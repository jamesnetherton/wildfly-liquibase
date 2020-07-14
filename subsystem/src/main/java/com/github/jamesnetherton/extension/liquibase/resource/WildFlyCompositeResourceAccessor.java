package com.github.jamesnetherton.extension.liquibase.resource;

/*-
 * #%L
 * wildfly-liquibase-subsystem
 * %%
 * Copyright (C) 2017 - 2020 James Netherton
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

import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.TreeSet;

public class WildFlyCompositeResourceAccessor implements ResourceAccessor {

    private final ResourceAccessor[] resourceAccessors;

    public WildFlyCompositeResourceAccessor(ResourceAccessor... resourceAccessors) {
        this.resourceAccessors = resourceAccessors;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        WildFlyInputStreamList answer = new WildFlyInputStreamList();
        for (ResourceAccessor accessor : resourceAccessors) {
            answer.addAll(accessor.openStreams(relativeTo, streamPath));
        }
        return answer;
    }

    @Override
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        InputStreamList streamList = this.openStreams(relativeTo, streamPath);
        if (streamList == null || streamList.size() == 0) {
            return null;
        } else {
            return streamList.iterator().next();
        }
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> returnSet = new TreeSet<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            final SortedSet<String> list = accessor.list(relativeTo, path, recursive, includeFiles, includeDirectories);
            if (list != null) {
                returnSet.addAll(list);
            }
        }
        return returnSet;
    }

    @Override
    public SortedSet<String> describeLocations() {
        return null;
    }
}
