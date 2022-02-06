/*   Copyright 2018 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package dom.checkin;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.DocumentHelper;
import org.apache.xmlbeans.impl.common.SAXHelper;
import org.apache.xmlbeans.impl.common.StaxHelper;
import org.apache.xmlbeans.impl.common.XMLBeansConstants;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for XML Parser settings
 */

public class ParserTest {

    @Test
    void testXmlOptionsDefaults() {
        XmlOptions options = new XmlOptions();
        assertEquals(2048, options.getEntityExpansionLimit());
        assertFalse(options.isLoadDTDGrammar());
        assertFalse(options.isLoadExternalDTD());
        assertFalse(options.disallowDocTypeDeclaration());
    }

    @Test
    void testXMLBeansConstantsOverrides() {
        XmlOptions options = new XmlOptions();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        options.setDisallowDocTypeDeclaration(true);
        assertEquals(1, options.getEntityExpansionLimit());
        assertTrue(options.isLoadDTDGrammar());
        assertTrue(options.isLoadExternalDTD());
        assertTrue(options.disallowDocTypeDeclaration());
    }

    @Test
    void testXmlInputFactoryPropertyDefaults() {
        XmlOptions options = new XmlOptions();
        XMLInputFactory factory = StaxHelper.newXMLInputFactory(options);
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertEquals(false, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    void testXmlInputFactoryPropertyOverrides() {
        XmlOptions options = new XmlOptions();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        XMLInputFactory factory = StaxHelper.newXMLInputFactory(options);
        assertEquals(true, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    void testXMLReader() throws Exception {
        XmlOptions options = new XmlOptions();
        XMLReader reader = SAXHelper.newXMLReader(options);
        assertNotSame(reader, SAXHelper.newXMLReader(options));
        assertFalse(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR));
        assertFalse(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD));
        assertFalse(reader.getFeature(XMLBeansConstants.FEATURE_DISALLOW_DOCTYPE_DECL));
        assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
        assertNotNull(reader.getProperty(XMLBeansConstants.SECURITY_MANAGER));

        String xmlWithDtd = "<!DOCTYPE foo [<!ELEMENT t ANY><!ENTITY xe \"TEST XXE\"> ]>\n<xml>&xe;</xml>";
        reader.parse(new InputSource(new ByteArrayInputStream(xmlWithDtd.getBytes(UTF_8))));
    }

    @Test
    void testXMLReaderOverrides() throws Exception {
        XmlOptions options = new XmlOptions();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        options.setDisallowDocTypeDeclaration(true);
        XMLReader reader = SAXHelper.newXMLReader(options);
        assertNotSame(reader, SAXHelper.newXMLReader(options));
        assertTrue(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR));
        assertTrue(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD));
        assertTrue(reader.getFeature(XMLBeansConstants.FEATURE_DISALLOW_DOCTYPE_DECL));
        assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
        assertNotNull(reader.getProperty(XMLBeansConstants.SECURITY_MANAGER));

        String xmlWithDtd = "<!DOCTYPE foo [<!ELEMENT t ANY><!ENTITY xe \"TEST XXE\"> ]>\n<xml>&xe;</xml>";
        InputSource is = new InputSource(new ByteArrayInputStream(xmlWithDtd.getBytes(UTF_8)));
        assertThrows(SAXException.class, () -> reader.parse(is));
    }

    @Test
    void testDocumentBuilder() throws Exception {
        XmlOptions options = new XmlOptions();
        DocumentBuilder builder = DocumentHelper.newDocumentBuilder(options);
        assertNotSame(builder, DocumentHelper.newDocumentBuilder(options));

        String xmlWithDtd = "<!DOCTYPE foo [<!ELEMENT t ANY><!ENTITY xe \"TEST XXE\"> ]>\n<xml>&xe;</xml>";
        builder.parse(new InputSource(new ByteArrayInputStream(xmlWithDtd.getBytes(UTF_8))));
    }

    @Test
    void testDocumentBuilderOverrides() {
        XmlOptions options = new XmlOptions();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        options.setDisallowDocTypeDeclaration(true);
        DocumentBuilder builder = DocumentHelper.newDocumentBuilder(options);
        assertNotSame(builder, DocumentHelper.newDocumentBuilder(options));

        String xmlWithDtd = "<!DOCTYPE foo [<!ELEMENT t ANY><!ENTITY xe \"TEST XXE\"> ]>\n<xml>&xe;</xml>";
        InputSource is = new InputSource(new ByteArrayInputStream(xmlWithDtd.getBytes(UTF_8)));
        assertThrows(SAXException.class, () -> builder.parse(is));
    }
}
