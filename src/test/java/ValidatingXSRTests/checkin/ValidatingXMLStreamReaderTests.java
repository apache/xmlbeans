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
package ValidatingXSRTests.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openuri.testNumerals.DocDocument;
import tools.util.JarUtil;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatingXMLStreamReaderTests {
    private final static String URI_NUMERALS = "http://openuri.org/testNumerals";

    @ParameterizedTest
    @ValueSource(strings = {
        "xbean/xmlobject/enumtest.xml",
        "xbean/xmlobject/nameworld.xml",
        "xbean/misc/xmldsig-core-schema.xsd"
        //"xbean/misc/signature-example.xml"
    })
    void testValidateDoc(String doc) throws FileNotFoundException, XMLStreamException, Exception {
        File file = JarUtil.getResourceFromJarasFile(doc);
        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        Collection<XmlError> errors = new ArrayList<>();
        XMLStreamReader xsr = XmlObject.Factory.parse(new FileInputStream(file)).newXMLStreamReader();

        valXsr.init(xsr, true, null /* validate an entire document */,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null,
            errors);

        while (valXsr.hasNext()) {
            valXsr.next();
        }

        assertTrue(valXsr.isValid(), "File '" + file.getName() + "' is invalid.");
    }

    @Test
    void testValidateGlobalAtt1() throws XMLStreamException {
        XmlObject xo = XmlObject.Factory.newInstance();
        try (XmlCursor xc = xo.newCursor()) {
            xc.toNextToken();
            xc.insertAttributeWithValue("price", URI_NUMERALS, "23.5");
        }

        XMLStreamReader xsr = xo.newXMLStreamReader(new XmlOptions().setSaveOuter());
        Collection<XmlError> errors = new ArrayList<>();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, null,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        while (valXsr.hasNext()) {
            valXsr.next();
        }

        assertTrue(valXsr.isValid(), "Global attribute validation is broken.");

    }

    @Test
    void testValidateGlobalAtt2() throws XMLStreamException {

        XMLStreamReader xsr = new TestXSR();
        Collection<XmlError> errors = new ArrayList<>();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, null,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        while (valXsr.hasNext()) {
            valXsr.next();
        }

        assertTrue(valXsr.isValid(), "Global attribute validation 2 is broken.");
    }

    private static class TestXSR implements XMLStreamReader {
        private int _state = 0;

        public Object getProperty(String name) throws IllegalArgumentException {
            return null;
        }

        public int next() throws XMLStreamException {
            int state = _state;
            _state++;

            switch (state) {
                case 0:
                    return XMLEvent.START_DOCUMENT;

                case 1:
                    return XMLEvent.ATTRIBUTE;
            }
            return XMLEvent.END_DOCUMENT;
        }

        public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        }

        public String getElementText() throws XMLStreamException {
            return null;
        }

        public int nextTag() throws XMLStreamException {
            return next();
        }

        public boolean hasNext() throws XMLStreamException {
            return _state < 3;
        }

        public void close() throws XMLStreamException {
        }

        public String getNamespaceURI(String prefix) {
            if ("".equals(prefix)) {
                return URI_NUMERALS;
            }
            return null;
        }

        public boolean isStartElement() {
            return false;
        }

        public boolean isEndElement() {
            return false;
        }

        public boolean isCharacters() {
            return false;
        }

        public boolean isWhiteSpace() {
            return false;
        }

        public String getAttributeValue(String namespaceURI, String localName) {
            if (URI_NUMERALS.equals(namespaceURI) && "price".equals(localName)) {
                return "5";
            }
            throw new IllegalStateException();
        }

        public int getAttributeCount() {
            return 1;
        }

        public QName getAttributeName(int index) {
            if (index == 0) {
                return new QName(URI_NUMERALS, "price");
            }
            throw new IllegalStateException();
        }

        public String getAttributeNamespace(int index) {
            if (index == 0) {
                return URI_NUMERALS;
            }
            throw new IllegalStateException();
        }

        public String getAttributeLocalName(int index) {
            if (index == 0) {
                return "price";
            }
            throw new IllegalStateException();
        }

        public String getAttributePrefix(int index) {
            if (index == 0) {
                return "";
            }
            throw new IllegalStateException();
        }

        public String getAttributeType(int index) {
            if (index == 0) {
                return "CDATA";
            }
            throw new IllegalStateException();
        }

        public String getAttributeValue(int index) {
            if (index == 0) {
                return "8.7654321";
            }
            throw new IllegalStateException();
        }

        public boolean isAttributeSpecified(int index) {
            if (index == 0) {
                return true;
            }
            throw new IllegalStateException();
        }

        public int getNamespaceCount() {
            return 0;
        }

        public String getNamespacePrefix(int index) {
            return null;
        }

        public String getNamespaceURI(int index) {
            return null;
        }

        public NamespaceContext getNamespaceContext() {
            return null;
        }

        public int getEventType() {
            return XMLEvent.ATTRIBUTE;
        }

        public String getText() {
            return null;
        }

        public char[] getTextCharacters() {
            return new char[0];
        }

        public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
            return 0;
        }

        public int getTextStart() {
            return 0;
        }

        public int getTextLength() {
            return 0;
        }

        public String getEncoding() {
            return null;
        }

        public boolean hasText() {
            return false;
        }

        public Location getLocation() {
            return null;
        }

        public QName getName() {
            return null;
        }

        public String getLocalName() {
            return null;
        }

        public boolean hasName() {
            return false;
        }

        public String getNamespaceURI() {
            return null;
        }

        public String getPrefix() {
            return null;
        }

        public String getVersion() {
            return null;
        }

        public boolean isStandalone() {
            return false;
        }

        public boolean standaloneSet() {
            return false;
        }

        public String getCharacterEncodingScheme() {
            return null;
        }

        public String getPITarget() {
            return null;
        }

        public String getPIData() {
            return null;
        }

    }

    @Test
    void testValidateContent1() throws XMLStreamException {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc = xo.newCursor();
        xc.toNextToken();

        xc.insertElementWithText("int", URI_NUMERALS, "5");
        xc.insertElementWithText("float", URI_NUMERALS, "7.654321");

        XMLStreamReader xsr = xo.newXMLStreamReader();
        Collection<XmlError> errors = new ArrayList<>();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, true, DocDocument.Doc.type,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        int depth = 0;

        while (valXsr.hasNext()) {
            if (valXsr.getEventType() == XMLEvent.END_ELEMENT && ++depth >= 2) {
                break;
            }
            valXsr.next();
        }
        assertTrue(valXsr.isValid(), "Content1 validation is broken.");
    }

    @Test
    void testValidateContent2() throws XMLStreamException {
        String doc = "<doc xmlns='" + URI_NUMERALS + "'><int>5</int><float>7.654321</float></doc>";

        XMLStreamReader xsr = null;
        try {
            xsr = XmlObject.Factory.parse(doc).newXMLStreamReader();
        } catch (XmlException e) {
            throw new XMLStreamException(e);
        }
        xsr.nextTag();

        Collection<XmlError> errors = new ArrayList<>();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr, false, DocDocument.Doc.type,
            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
            null, errors);

        int depth = 0;

        while (valXsr.hasNext()) {
            if (valXsr.next() == XMLEvent.END_ELEMENT && ++depth >= 2) {
                break;
            }
        }

        assertTrue(valXsr.isValid(), "Content2 validation is broken.");
    }
}
