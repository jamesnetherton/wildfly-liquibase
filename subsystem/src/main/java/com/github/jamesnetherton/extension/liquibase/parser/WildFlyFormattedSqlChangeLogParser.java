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
package com.github.jamesnetherton.extension.liquibase.parser;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.formattedsql.FormattedSqlChangeLogParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jamesnetherton.extension.liquibase.ModelConstants;

/**
 * Extended {@link FormattedSqlChangeLogParser} to extract the datasource reference property from an SQL comment.
 */
public class WildFlyFormattedSqlChangeLogParser extends FormattedSqlChangeLogParser {

    private static final Pattern DATASOURCE_PATTERN = Pattern.compile("--[\\s]*" + ModelConstants.DATASOURCE + "\\s(.*)", Pattern.CASE_INSENSITIVE);

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        try (InputStream inputStream = openChangeLogFile(physicalChangeLogLocation, resourceAccessor)) {
            try (BufferedReader reader = new BufferedReader(StreamUtil.readStreamWithReader(inputStream, null))){
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = DATASOURCE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        changeLogParameters.set(ModelConstants.DATASOURCE, matcher.group(1).trim());
                        break;
                    }
                }
                return super.parse(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
            }
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
    }
}
