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

import java.util.TreeSet;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

final class LiquibaseSubsystemWriter implements XMLStreamConstants, XMLElementWriter<SubsystemMarshallingContext> {

    static LiquibaseSubsystemWriter INSTANCE = new LiquibaseSubsystemWriter();

    private LiquibaseSubsystemWriter() {
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
        ModelNode node = context.getModelNode();

        if (node.hasDefined(ModelConstants.DATABASE_CHANGELOG)) {
            ModelNode properties = node.get(ModelConstants.DATABASE_CHANGELOG);

            for (String key : new TreeSet<>(properties.keys())) {
                String val = properties.get(key).get(ModelConstants.VALUE).asString();
                String datasourceRef = properties.get(key).get(ModelConstants.DATASOURCE_REF).asString();
                String contextNames = properties.get(key).get(ModelConstants.CONTEXT_NAMES).asStringOrNull();
                String labels = properties.get(key).get(ModelConstants.LABELS).asStringOrNull();

                writer.writeStartElement(Namespace10.Element.DATABASE_CHANGELOG.getLocalName());
                writer.writeAttribute(Namespace10.Attribute.NAME.getLocalName(), key);
                writer.writeAttribute(Namespace10.Attribute.DATASOURCE_REF.getLocalName(), datasourceRef);

                if (contextNames != null) {
                    writer.writeAttribute(Namespace10.Attribute.CONTEXT_NAMES.getLocalName(), contextNames);
                }

                if (labels != null) {
                    writer.writeAttribute(Namespace10.Attribute.LABELS.getLocalName(), labels);
                }

                writer.writeCharacters(val);
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }
}
