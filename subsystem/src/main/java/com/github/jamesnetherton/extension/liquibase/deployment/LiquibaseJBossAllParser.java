package com.github.jamesnetherton.extension.liquibase.deployment;

/*-
 * #%L
 * wildfly-liquibase-subsystem
 * %%
 * Copyright (C) 2017 - 2020 James Netherton
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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration.Builder;
import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration.BuilderCollection;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.jbossallxml.JBossAllXMLParser;
import org.jboss.staxmapper.XMLExtendedStreamReader;

public class LiquibaseJBossAllParser implements JBossAllXMLParser<BuilderCollection> {

    public static final String NAMESPACE_1_0 = "urn:com.github.jamesnetherton.liquibase:1.0";
    public static final QName ROOT_ELEMENT = new QName(NAMESPACE_1_0, "liquibase");

    @Override
    public BuilderCollection parse(XMLExtendedStreamReader xmlExtendedStreamReader, DeploymentUnit deploymentUnit) throws XMLStreamException {
        BuilderCollection collection = new BuilderCollection();
        parseLiquibaseElement(xmlExtendedStreamReader, collection);
        return collection;
    }

    enum Element {
        LIQUIBASE(ROOT_ELEMENT),
        CONTEXTS(new QName(NAMESPACE_1_0, "contexts")),
        LABELS(new QName(NAMESPACE_1_0, "labels")),
        UNKNOWN(null);

        private static final Map<QName, Element> elements;

        static {
            Map<QName, Element> elementsMap = new HashMap<>();
            for (Element element : Element.values()) {
                if( element!=UNKNOWN ) {
                    elementsMap.put(element.getQName(), element);
                }
            }
            elements = elementsMap;
        }

        private final QName qname;

        Element(QName qname) {
            this.qname = qname;
        }

        public QName getQName() {
            return qname;
        }

        static Element of(QName qName) {
            QName name;
            if (qName.getNamespaceURI().isEmpty()) {
                name = new QName(NAMESPACE_1_0, qName.getLocalPart());
            } else {
                name = qName;
            }
            final Element element = elements.get(name);
            return element == null ? UNKNOWN : element;
        }
    }

    enum Attribute {
        CHANGELOG(new QName("changelog")),
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            Map<QName, Attribute> attributesMap = new HashMap<>();
            for (Attribute element : Attribute.values()) {
                if( element != UNKNOWN ) {
                    attributesMap.put(element.getQName(), element);
                }
            }
            attributes = attributesMap;
        }

        private final QName qname;

        Attribute(QName qname) {
            this.qname = qname;
        }

        public QName getQName() {
            return qname;
        }
    }

    private void parseLiquibaseElement(XMLExtendedStreamReader reader, BuilderCollection result) throws XMLStreamException {
        final Builder builder = new Builder();
        final Element rootElement = Element.of(reader.getName());
        switch (rootElement) {
            case LIQUIBASE:
                final String value = getAttributeValue(reader, Attribute.CHANGELOG);
                builder.name(value);
                break;
            default:
                throw unexpectedContent(reader);
        }

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CONTEXTS:
                            builder.contextNames(parseElement(reader, builder));
                            break;
                        case LABELS:
                            builder.labels(parseElement(reader, builder));
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }

        result.addBuilder(builder);
    }

    private String parseElement(XMLExtendedStreamReader reader, Builder result) throws XMLStreamException {
        switch (reader.next()) {
            case XMLStreamConstants.CHARACTERS:
                return reader.getText();
            case XMLStreamConstants.END_ELEMENT:
                return null;
            default:
                throw unexpectedContent(reader);
        }
    }

    private XMLStreamException unexpectedContent(final XMLStreamReader reader) {
        final String kind;
        switch (reader.getEventType()) {
            case XMLStreamConstants.ATTRIBUTE:
                kind = "attribute";
                break;
            case XMLStreamConstants.CDATA:
                kind = "cdata";
                break;
            case XMLStreamConstants.CHARACTERS:
                kind = "characters";
                break;
            case XMLStreamConstants.COMMENT:
                kind = "comment";
                break;
            case XMLStreamConstants.DTD:
                kind = "dtd";
                break;
            case XMLStreamConstants.END_DOCUMENT:
                kind = "document end";
                break;
            case XMLStreamConstants.END_ELEMENT:
                kind = "element end";
                break;
            case XMLStreamConstants.ENTITY_DECLARATION:
                kind = "entity declaration";
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                kind = "entity ref";
                break;
            case XMLStreamConstants.NAMESPACE:
                kind = "namespace";
                break;
            case XMLStreamConstants.NOTATION_DECLARATION:
                kind = "notation declaration";
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                kind = "processing instruction";
                break;
            case XMLStreamConstants.SPACE:
                kind = "whitespace";
                break;
            case XMLStreamConstants.START_DOCUMENT:
                kind = "document start";
                break;
            case XMLStreamConstants.START_ELEMENT:
                kind = "element start";
                break;
            default:
                kind = "unknown";
                break;
        }

        return new XMLStreamException("Unexpected content kind: "+kind+" at "+reader.getLocation());
    }

    private static String getAttributeValue(final XMLStreamReader reader, Attribute attribute) throws XMLStreamException {
        return reader.getAttributeValue(null, attribute.getQName().getLocalPart());
    }

    private static XMLStreamException endOfDocument(final Location location) {
        return new XMLStreamException("Expected end of document "+location);
    }
}
