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

import org.apache.xmlbeans.XmlOptionsBean;
import org.apache.xmlbeans.impl.common.SAXHelper;
import org.apache.xmlbeans.impl.common.StaxHelper;
import org.apache.xmlbeans.impl.common.XMLBeansConstants;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Tests for XML Parser settings
 */

public class ParserTest {

    @Test
    public void testXmlOptionsDefaults() {
        XmlOptionsBean options = new XmlOptionsBean();
        assertEquals(2048, options.getEntityExpansionLimit());
        assertFalse(options.isLoadDTDGrammar());
        assertFalse(options.isLoadExternalDTD());
    }

    @Test
    public void testXMLBeansConstantsOverrides() {
        XmlOptionsBean options = new XmlOptionsBean();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        assertEquals(1, options.getEntityExpansionLimit());
        assertTrue(options.isLoadDTDGrammar());
        assertTrue(options.isLoadExternalDTD());
    }

    @Test
    public void testXmlInputFactoryPropertyDefaults() {
        XmlOptionsBean options = new XmlOptionsBean();
        XMLInputFactory factory = StaxHelper.newXMLInputFactory(options);
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertEquals(false, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    public void testXmlInputFactoryPropertyOverrides() {
        XmlOptionsBean options = new XmlOptionsBean();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        XMLInputFactory factory = StaxHelper.newXMLInputFactory(options);
        assertEquals(true, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    public void testXMLReader() throws Exception {
        XmlOptionsBean options = new XmlOptionsBean();
        XMLReader reader = SAXHelper.newXMLReader(options);
        assertNotSame(reader, SAXHelper.newXMLReader(options));
        assertFalse(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR));
        assertFalse(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD));
        assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
        assertNotNull(reader.getProperty(XMLBeansConstants.SECURITY_MANAGER));

        reader.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes("UTF-8"))));
    }

    @Test
    public void testXMLReaderOverrides() throws Exception {
        XmlOptionsBean options = new XmlOptionsBean();
        options.setEntityExpansionLimit(1);
        options.setLoadDTDGrammar(true);
        options.setLoadExternalDTD(true);
        XMLReader reader = SAXHelper.newXMLReader(options);
        assertNotSame(reader, SAXHelper.newXMLReader(options));
        assertTrue(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR));
        assertTrue(reader.getFeature(XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD));
        assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
        assertNotNull(reader.getProperty(XMLBeansConstants.SECURITY_MANAGER));

        reader.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes("UTF-8"))));
    }
}
