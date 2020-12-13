package com.github.jamesnetherton.extension.liquibase.resource;

import java.io.InputStream;
import java.net.URI;
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

public class WildFlyInputStreamList extends InputStreamList {

    @Override
    public boolean add(URI uri, InputStream inputStream) {
        boolean alreadySaw = super.alreadySaw(uri);
        if (alreadySaw) {
            return false;
        }

        if (super.getURIs().contains(uri)) {
            return false;
        }

        return super.add(uri, inputStream);
    }
}
