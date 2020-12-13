package com.github.jamesnetherton.liquibase.arquillian;

/*-
 * #%L
 * wildfly-liquibase-testextension
 * %%
 * Copyright (C) 2017 - 2019 James Netherton
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class TestExtensionUtils {

    private TestExtensionUtils(){
        // Utility class
    }

    public static String inputStreamToString(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        StringBuilder builder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading InputStream " + in);
        }
        return builder.toString();
    }
}
