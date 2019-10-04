package com.github.jamesnetherton.extension.liquibase.test.jpa.init;

/*-
 * #%L
 * wildfly-liquibase-itests
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

import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;

@ApplicationScoped
@Startup
@Singleton
public class DatabaseInitializer extends LiquibaseTestSupport {

    @PostConstruct
    public void postConstruct() {
        InputStream inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream("/sql/db-init.sql");
        try {
            executeSqlScript(inputStream, "java:jboss/datasources/ExampleDS");
        } catch (Exception e) {
            // Ignore
        }
    }
}
