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

import javax.xml.stream.XMLInputFactory;

import junit.framework.*;

import org.apache.xmlbeans.impl.common.*;

/**
 * Tests for XML Parser settings
 */

public class ParserTest extends TestCase {

    public void testXMLBeansConstantsDefaults() {
        assertEquals(2048, XMLBeansConstants.getEntityExpansionLimit());
        assertFalse(XMLBeansConstants.isLoadDtdGrammar());
        assertFalse(XMLBeansConstants.isLoadExternalDtd());
    }

    public void testXMLBeansConstantsOverrides() {
        try {
            System.setProperty(XMLBeansConstants.PROPERTY_ENTITY_EXPANSION_LIMIT, "1");
            System.setProperty(XMLBeansConstants.PROPERTY_LOAD_DTD_GRAMMAR, "true");
            System.setProperty(XMLBeansConstants.PROPERTY_LOAD_EXTERNAL_DTD, "true");
            assertEquals(1, XMLBeansConstants.getEntityExpansionLimit());
            assertTrue(XMLBeansConstants.isLoadDtdGrammar());
            assertTrue(XMLBeansConstants.isLoadExternalDtd());
        } finally {
            System.clearProperty(XMLBeansConstants.PROPERTY_ENTITY_EXPANSION_LIMIT);
            System.clearProperty(XMLBeansConstants.PROPERTY_LOAD_DTD_GRAMMAR);
            System.clearProperty(XMLBeansConstants.PROPERTY_LOAD_EXTERNAL_DTD);
        }
    }

    public void testXmlInputFactoryPropertyDefaults() {
        XMLInputFactory factory = StaxHelper.newXMLInputFactory();
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertEquals(false, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    public void testXmlInputFactoryPropertyOverrides() {
        try {
            System.setProperty(XMLBeansConstants.PROPERTY_LOAD_DTD_GRAMMAR, "true");
            System.setProperty(XMLBeansConstants.PROPERTY_LOAD_EXTERNAL_DTD, "true");
            XMLInputFactory factory = StaxHelper.newXMLInputFactory();
            assertEquals(true, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
            assertEquals(true, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
        } finally {
            System.clearProperty(XMLBeansConstants.PROPERTY_LOAD_DTD_GRAMMAR);
            System.clearProperty(XMLBeansConstants.PROPERTY_LOAD_EXTERNAL_DTD);
        }
    }
}
