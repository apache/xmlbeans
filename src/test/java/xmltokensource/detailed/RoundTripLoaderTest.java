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


package xmltokensource.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import xmlcursor.common.Common;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static xmlcursor.common.BasicCursorTestCase.compareDocTokens;


public class RoundTripLoaderTest {


    private void _newDomNodeRoundTrip(XmlOptions map) throws Exception {
        XmlObject m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        Node doc = m_xo.newDomNode(map);
        assertNotNull(doc);
        XmlObject xo = XmlObject.Factory.parse(doc, map);

        try (XmlCursor m_xc = m_xo.newCursor();
            XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    void testNewDomNodeRoundTrip() throws Exception {
        _newDomNodeRoundTrip(null);
    }

    @Test
    void testNewDomNodeWithOptionsRoundTrip() throws Exception {
        XmlOptions m_map = new XmlOptions();
        m_map.setCharacterEncoding("Big5");
        m_map.setSaveNamespacesFirst();

        _newDomNodeRoundTrip(m_map);
    }

    private void _newInputStreamRoundTrip(XmlOptions map) throws Exception {
        XmlObject m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        InputStream is = m_xo.newInputStream(map);
        assertNotNull(is);
        XmlOptions options = new XmlOptions(map);
        XmlObject xo = XmlObject.Factory.parse(is, options);

        try (XmlCursor m_xc = m_xo.newCursor();
            XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    void testNewInputStreamRoundTrip() throws Exception {
        _newInputStreamRoundTrip(null);
    }

    @Test
    void testNewInputStreamWithOptionsRoundTrip() throws Exception {
        XmlOptions m_map = new XmlOptions();
        m_map.setCharacterEncoding("Big5");
        m_map.setSaveNamespacesFirst();

        _newInputStreamRoundTrip(m_map);
    }

    private void _xmlTextRoundTrip(XmlOptions map) throws Exception {
        XmlObject m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        String sXml = m_xo.xmlText(map);
        assertNotNull(sXml);
        XmlOptions options = new XmlOptions(map);
        XmlObject xo = XmlObject.Factory.parse(sXml, options);

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    void testXmlTextRoundTrip() throws Exception {
        _xmlTextRoundTrip(null);
    }

    @Test
    void testXmlTextWithOptionsRoundTrip() throws Exception {
        XmlOptions m_map = new XmlOptions();
        m_map.setCharacterEncoding("Big5");
        m_map.setSaveNamespacesFirst();
        _xmlTextRoundTrip(m_map);
    }
}
