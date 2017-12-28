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
import liquibase.resource.UtfBomAwareReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jamesnetherton.extension.liquibase.ModelConstants;

public class WildFlyFormattedSqlChangeLogParser extends FormattedSqlChangeLogParser {

    private static final Pattern datasourceRefPattern = Pattern.compile("\\-\\-[\\s]*" + ModelConstants.DATASOURCE_REF + "\\s(.*)", Pattern.CASE_INSENSITIVE);

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new UtfBomAwareReader(openChangeLogFile(physicalChangeLogLocation, resourceAccessor)));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = datasourceRefPattern.matcher(line);
                if (matcher.matches()) {
                    changeLogParameters.set(ModelConstants.DATASOURCE_REF, matcher.group(1).trim());
                    break;
                }
            }
            return super.parse(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
