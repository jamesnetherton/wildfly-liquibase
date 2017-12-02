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

import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.json.JsonChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.yaml.YamlChangeLogParser;

import java.io.File;

public final class ChangeLogParserFactory {

    private ChangeLogParserFactory(){
    }

    public static ChangeLogParser createParser(File changeLogFile) {
        if (changeLogFile.getName().endsWith(".json")) {
            return new JsonChangeLogParser();
        } else if(changeLogFile.getName().endsWith(".xml")) {
            return new XMLChangeLogSAXParser();
        } else if(changeLogFile.getName().endsWith(".yaml") || changeLogFile.getName().endsWith(".yml")) {
            return new YamlChangeLogParser();
        } else {
            return null;
        }
    }
}
