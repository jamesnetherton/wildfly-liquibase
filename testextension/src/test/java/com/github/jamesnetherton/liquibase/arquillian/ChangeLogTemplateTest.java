/*-
 * #%L
 * wildfly-liquibase-testextension
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
package com.github.jamesnetherton.liquibase.arquillian;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;

import com.github.jamesnetherton.extension.liquibase.ChangeLogParserFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the validity of the change log template files
 */
public class ChangeLogTemplateTest {

    @Test
    public void testParseJsonTemplate() throws Exception {
        assertChangeLogParse("target/classes/changelogs/changelog.json");
    }

    @Test
    public void testParseSqlTemplate() throws ChangeLogParseException {
        assertChangeLogParse("target/classes/changelogs/changelog.sql");
    }

    @Test
    public void testParseXmlTemplate() throws ChangeLogParseException {
        assertChangeLogParse("target/classes/changelogs/changelog.xml");
    }

    @Test
    public void testParseYamlTemplate() throws ChangeLogParseException {
        assertChangeLogParse("target/classes/changelogs/changelog.yaml");
    }

    private void assertChangeLogParse(String path) throws ChangeLogParseException {
        File file = new File(path);
        ChangeLogParser parser = ChangeLogParserFactory.createParser(file.getName());

        if (parser == null) {
            throw new ChangeLogParseException("Failed to find a suitable parser for " + file.getName());
        }

        DatabaseChangeLog changeLog = parser.parse(file.getName(), new ChangeLogParameters(), new FileSystemResourceAccessor(file.getParentFile()));
        Assert.assertNotNull(changeLog);
    }
}
