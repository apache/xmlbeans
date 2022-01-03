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
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import java.io.InputStream;
import java.io.Reader;

import static org.junit.Assert.assertNotNull;


public class RoundTripLoaderTest extends BasicCursorTestCase {
    public static final String DOC_FRAGMENT = "#document-fragment";
    private XmlOptions m_map = new XmlOptions();

    @Before
    public void setUp() {
        m_map.setCharacterEncoding("Big5");
        m_map.setSaveNamespacesFirst();
    }

    private void _newDomNodeRoundTrip(XmlOptions map) throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        Node doc = m_xo.newDomNode(map);
        assertNotNull(doc);
        XmlObject xo = XmlObject.Factory.parse(doc, map);
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    public void testNewDomNodeRoundTrip() throws Exception {
        _newDomNodeRoundTrip(null);
    }

    @Test
    public void testNewDomNodeWithOptionsRoundTrip() throws Exception {
        _newDomNodeRoundTrip(m_map);
    }

    private void _newInputStreamRoundTrip(XmlOptions map) throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        InputStream is = m_xo.newInputStream(map);
        assertNotNull(is);
        XmlOptions options = new XmlOptions(map);
        XmlObject xo = XmlObject.Factory.parse(is, options);
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    public void testNewInputStreamRoundTrip() throws Exception {
        _newInputStreamRoundTrip(null);
    }

    @Test
    public void testNewInputStreamWithOptionsRoundTrip() throws Exception {
        _newInputStreamRoundTrip(m_map);
    }

    private void _newReaderRoundTrip(XmlOptions map) throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        Reader reader = m_xo.newReader(map);
        assertNotNull(reader);
        XmlOptions options = new XmlOptions(map);
        XmlObject xo = XmlObject.Factory.parse(reader, options);
        m_xc = m_xo.newCursor();
        XmlCursor xc1 = xo.newCursor();
        try {
            compareDocTokens(m_xc, xc1);
        } finally {
            xc1.dispose();
        }
    }

    private void _xmlTextRoundTrip(XmlOptions map) throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_NESTED_SIBLINGS);
        String sXml = m_xo.xmlText(map);
        assertNotNull(sXml);
        XmlOptions options = new XmlOptions(map);
        XmlObject xo = XmlObject.Factory.parse(sXml, options);
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }

    @Test
    public void testXmlTextRoundTrip() throws Exception {
        _xmlTextRoundTrip(null);
    }

    public void testXmlTextWithOptionsRoundTrip() throws Exception {
        _xmlTextRoundTrip(m_map);
    }
}
