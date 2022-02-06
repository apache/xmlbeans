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

package xmlobject.xmlloader.detailed;

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParseTest {

    @Test
    void testLoadStripWhitespace() throws Exception {
        String xml = "<foo>01234   <bar>text</bar>   chars \r\n</foo>  ";
        XmlOptions m_map = new XmlOptions();
        m_map.setLoadStripWhitespace();
        XmlObject m_xo = XmlObject.Factory.parse(xml, m_map);
        try (XmlCursor m_xc = m_xo.newCursor()) {
            assertEquals("<foo>01234<bar>text</bar>chars</foo>", m_xc.xmlText());
        }
    }


    @Test
    void testLoadDiscardDocumentElement() throws Exception {
        XmlOptions m_map = new XmlOptions();
        m_map.setLoadReplaceDocumentElement(new QName(""));
        XmlObject.Factory.parse("<foo>01234   <bar>text</bar>   chars </foo>  ", m_map);
    }

    @Test
    void testPrefixNotDefined() throws Exception {
        String sXml = "<Person xmlns=\"person\"><pre1:Name>steve</pre1:Name></Person>";
        assertThrows(XmlException.class, () -> XmlObject.Factory.parse(sXml));
    }

    @Test
    void testErrorListener() throws Exception {
        XmlOptions m_map = new XmlOptions();
        List<XmlError> vErrors = new ArrayList<>();
        m_map.setErrorListener(vErrors);
        // improper end tag
        assertThrows(XmlException.class, () -> XmlObject.Factory.parse("<foo>text<foo>", m_map));
    }

    @Test
    void testParsingDOMWithDTD() throws Exception {
        final String svgDocumentString =
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n" +
            "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
            "<svg />";
        assertNotNull(XmlObject.Factory.parse(svgDocumentString));
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document parse = documentBuilder.parse(new InputSource(new StringReader(svgDocumentString)));
        assertNotNull(XmlObject.Factory.parse(parse));
    }
}

