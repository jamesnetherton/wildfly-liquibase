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
package com.github.jamesnetherton.extension.liquibase;

public enum ChangeLogFormat {
    JSON(".json"),
    SQL(".sql"),
    UNKNOWN(".unknown"),
    XML(".xml"),
    YAML(".yaml"),
    YML(".yml");

    private final String extension;

    ChangeLogFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getFileName() {
        return "wildfly-liquibase-changelog" + this.extension;
    }

    public static ChangeLogFormat fromFileName(String fileName) {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("File name must not be null or empty");
        }

        String extension;
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index).toLowerCase();
            for (ChangeLogFormat format : ChangeLogFormat.values()) {
                if (format.getExtension().equals(extension)) {
                    return format;
                }
            }
        }

        return UNKNOWN;
    }
}
