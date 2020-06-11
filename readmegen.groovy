#!/usr/bin/groovy

/*-
 * #%L
 * wildfly-liquibase
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

def pom = new XmlSlurper().parse("./pom.xml")
def file = new File("./README.md")
def readme = file.text
def version = pom.version.toString().replace("-SNAPSHOT", "")

def modifiedReadme = readme.replaceAll("download/[0-9].[0-9].[0-9]", "download/${version}")
        .replaceAll("wildfly-liquibase-distro-[0-9].[0-9].[0-9]", "wildfly-liquibase-distro-${version}")
        .replaceAll("wildfly-liquibase-galleon-pack:[0-9].[0-9].[0-9]", "wildfly-liquibase-galleon-pack:${version}")

file.write(modifiedReadme)
