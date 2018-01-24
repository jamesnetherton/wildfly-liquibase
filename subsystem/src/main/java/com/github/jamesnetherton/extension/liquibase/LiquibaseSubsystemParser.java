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

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

final class LiquibaseSubsystemParser implements Namespace10, XMLStreamConstants, XMLElementReader<List<ModelNode>> {

    static XMLElementReader<List<ModelNode>> INSTANCE = new LiquibaseSubsystemParser();

    private LiquibaseSubsystemParser() {
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {

        ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, LiquibaseExtension.SUBSYSTEM_NAME);
        address.protect();

        ModelNode subsystemAdd = new ModelNode();
        subsystemAdd.get(OP).set(ADD);
        subsystemAdd.get(OP_ADDR).set(address);
        operations.add(subsystemAdd);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case VERSION_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case DATABASE_CHANGELOG: {
                            parseChangeLog(reader, address, operations);
                            break;
                        }
                        default: {
                            throw unexpectedElement(reader);
                        }
                    }
                }
            }
        }
    }

    private void parseChangeLog(XMLExtendedStreamReader reader, ModelNode address, List<ModelNode> operations) throws XMLStreamException {

        String changeLogName = null;
        String datasourceRef = null;
        String contextNames = null;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attrValue = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    changeLogName = attrValue;
                    break;
                }
                case DATASOURCE_REF:
                    datasourceRef = attrValue;
                    break;
                case CONTEXT_NAMES:
                    contextNames = attrValue;
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (changeLogName == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.NAME));
        }

        if (datasourceRef == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.DATASOURCE_REF));
        }

        StringBuffer content = new StringBuffer();
        while (reader.hasNext() && reader.next() != END_ELEMENT) {
            switch (reader.getEventType()) {
                case CHARACTERS:
                case CDATA:
                    content.append(reader.getText());
                    break;
            }
        }

        String changeLogDefinition = content.toString();

        ModelNode propNode = new ModelNode();
        propNode.get(OP).set(ADD);
        propNode.get(OP_ADDR)
            .set(address)
            .add(ModelConstants.DATABASE_CHANGELOG, changeLogName);
        propNode.get(ModelConstants.DATASOURCE_REF).set(datasourceRef);
        propNode.get(ModelConstants.VALUE).set(changeLogDefinition);

        if (contextNames != null) {
            propNode.get(ModelConstants.CONTEXT_NAMES).set(contextNames);
        }

        operations.add(propNode);
    }
}
