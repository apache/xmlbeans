/*   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DOM Level 1 factory methods produce nodes with no namespace information, which
 * the store tracks per node. These pin that behaviour across the three places the
 * state is written.
 */
public class NodePrefixUriFlagTest {

    private static final String URI = "http://example.org/ns";

    private static XmlObject parse() throws XmlException {
        return XmlObject.Factory.parse("<root/>");
    }

    private static Document doc() throws XmlException {
        return (Document) parse().getDomNode();
    }

    @Test
    void level1ElementHasNoNamespaceInfo() throws XmlException {
        Element e = doc().createElement("foo");

        assertNull(e.getLocalName());
        assertNull(e.getNamespaceURI());
        assertNull(e.getPrefix());
    }

    @Test
    void level2ElementKeepsNamespaceInfo() throws XmlException {
        Element e = doc().createElementNS(URI, "p:foo");

        assertEquals("foo", e.getLocalName());
        assertEquals(URI, e.getNamespaceURI());
        assertEquals("p", e.getPrefix());
    }

    @Test
    void level1AttributeHasNoNamespaceInfo() throws XmlException {
        Attr a = doc().createAttribute("bar");

        assertNull(a.getLocalName());
        assertNull(a.getNamespaceURI());
        assertNull(a.getPrefix());
    }

    @Test
    void level2AttributeKeepsNamespaceInfo() throws XmlException {
        Attr a = doc().createAttributeNS(URI, "p:bar");

        assertEquals("bar", a.getLocalName());
        assertEquals(URI, a.getNamespaceURI());
        assertEquals("p", a.getPrefix());
    }

    @Test
    void renamingALevel1ElementRestoresNamespaceInfo() throws XmlException {
        XmlObject xo = parse();
        Document doc = (Document) xo.getDomNode();

        Element child = doc.createElement("child");
        doc.getDocumentElement().appendChild(child);
        assertNull(child.getLocalName());

        try (XmlCursor c = xo.newCursor()) {
            c.toFirstChild(); // root
            c.toFirstChild(); // child
            c.setName(new QName(URI, "child", "p"));
        }

        assertEquals("child", child.getLocalName());
        assertEquals(URI, child.getNamespaceURI());
        assertEquals("p", child.getPrefix());
    }
}
