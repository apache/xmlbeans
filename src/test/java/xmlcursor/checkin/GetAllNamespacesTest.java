/*   Copyright 2004 The Apache Software Foundation
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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;

public class GetAllNamespacesTest {

    private static final String XML =
        "<bk:book xmlns:bk='urn:loc.gov:books'" +
        " xmlns:isbn='urn:ISBN:0-395-36341-6'>" +
        "<bk:title>Cheaper by the Dozen</bk:title>" +
        "<isbn:number>1568491379</isbn:number>" +
        "<nestedInfo xmlns:bk='urn:loc.gov:booksOverridden'>" +
        "nestedText</nestedInfo>" +
        "</bk:book>";

    @Test
    void testCursorNotContainer() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            //lousy message
            toNextTokenOfType(m_xc, XmlCursor.TokenType.TEXT);
            assertThrows(Exception.class, () -> m_xc.getAllNamespaces(new HashMap<>()));
        }
    }


    @Test
    void testGetAllNamespaces() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            toNextTokenOfType(m_xc, XmlCursor.TokenType.START);
            Map<String, String> namespaceMap = new HashMap<>();
            m_xc.getAllNamespaces(namespaceMap);
            //2 distinct namespaces but 3
            assertEquals(2, namespaceMap.entrySet().size());
            assertEquals("urn:loc.gov:books", namespaceMap.get("bk"));
            assertEquals("urn:ISBN:0-395-36341-6", namespaceMap.get("isbn"));
        }
    }

    @Test
    void testGetAllNamespacesIllegalCursorPos() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            Map<String, String> namespaceMap = new HashMap<>();
            m_xc.getAllNamespaces(namespaceMap);
            assertTrue(namespaceMap.isEmpty());
        }
    }

    @Test
    void testGetAllNamespacesNull() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            toNextTokenOfType(m_xc, XmlCursor.TokenType.START);
            assertDoesNotThrow(() -> m_xc.getAllNamespaces(null));
        }
    }

    /**
     * cursor is positioned below the namespace declaration but in its scope
     */
    @Test
    void testGetAllNamespacesInternal() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            Map<String,String> namespaceMap = new HashMap<>();
            m_xc.toFirstChild();
            m_xc.toChild(2);//nestedInfo
            m_xc.getAllNamespaces(namespaceMap);
            assertEquals(2, namespaceMap.entrySet().size());

            assertEquals("urn:loc.gov:booksOverridden", namespaceMap.get("bk"));
            assertEquals("urn:ISBN:0-395-36341-6", namespaceMap.get("isbn"));
        }
    }
}
