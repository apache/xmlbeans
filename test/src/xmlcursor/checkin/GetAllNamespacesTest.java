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
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetAllNamespacesTest extends BasicCursorTestCase {

    @Test(expected = Exception.class)
    public void testCursorNotContainer() {
        //lousy message
        toNextTokenOfType(m_xc, XmlCursor.TokenType.TEXT);
        Map myHash = new HashMap();

        m_xc.getAllNamespaces(myHash);
    }


    @Test
    public void testGetAllNamespaces() {
        //parse in setUp
        int nExpectedNamespaces = 2;//2 distinct namespaces but 3
        Map namespaceMap = new HashMap();
        toNextTokenOfType(m_xc, XmlCursor.TokenType.START);
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);
        assertEquals((String) namespaceMap.get("bk"), "urn:loc.gov:books");
        //assertEquals((String)namespaceMap.get("bk"),"urn:loc.gov:booksOverridden");
        assertEquals((String) namespaceMap.get("isbn"),
                "urn:ISBN:0-395-36341-6");
    }

    @Test
    public void testGetAllNamespacesIllegalCursorPos() {
        int nExpectedNamespaces = 0;
        Map namespaceMap = new HashMap();
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);
    }

    @Test
    public void testGetAllNamespacesNull() {

        toNextTokenOfType(m_xc, XmlCursor.TokenType.START);

            m_xc.getAllNamespaces(null);
    }

    /**
     * cursor is positioned below the namespace declaration but in its scope
     */
    @Test
    public void testGetAllNamespacesInternal() {
        int nExpectedNamespaces = 2;
        Map namespaceMap = new HashMap();
        m_xc.toFirstChild();
        m_xc.toChild(2);//nestedInfo
        m_xc.getAllNamespaces(namespaceMap);
        assertEquals(namespaceMap.entrySet().size(), nExpectedNamespaces);

        assertEquals((String) namespaceMap.get("bk"),
                "urn:loc.gov:booksOverridden");
        assertEquals((String) namespaceMap.get("isbn"),
                "urn:ISBN:0-395-36341-6");

    }

    @Before
    public void setUp() throws Exception {
        String sTestXml = "<bk:book xmlns:bk='urn:loc.gov:books'" +
            " xmlns:isbn='urn:ISBN:0-395-36341-6'>" +
            "<bk:title>Cheaper by the Dozen</bk:title>" +
            "<isbn:number>1568491379</isbn:number>" +
            "<nestedInfo xmlns:bk='urn:loc.gov:booksOverridden'>" +
            "nestedText</nestedInfo>" +
            "</bk:book>";
        m_xc = XmlObject.Factory.parse(sTestXml).newCursor();
    }
}
