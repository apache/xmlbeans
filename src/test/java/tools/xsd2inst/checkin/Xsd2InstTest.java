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
package tools.xsd2inst.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.DocumentHelper;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.apache.xmlbeans.impl.xsd2inst.SchemaInstanceGenerator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Xsd2InstTest {
    private static final String PRICEQ = "/xbean/compile/scomp/pricequote/PriceQuote.xsd";
    private static final String BASE64BIN = "/xbean/compile/scomp/base64Binary/Base64BinaryElement.xsd";

    @Test
    void testPriceQuote() throws Exception {
        XmlObject xobj;
        try (InputStream xsdStream = Xsd2InstTest.class.getResourceAsStream(PRICEQ)) {
            xobj = XmlObject.Factory.parse(xsdStream, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
        }
        SchemaInstanceGenerator.Xsd2InstOptions options = new SchemaInstanceGenerator.Xsd2InstOptions();
        String result = SchemaInstanceGenerator.xsd2inst(new XmlObject[]{xobj}, "price-quote", options);
        assertTrue(result.contains("<price-quote>"), "price-quote element found?");
        assertTrue(result.contains("<stock-symbol>string</stock-symbol>"), "stock-symbol element found?");
        assertTrue(result.contains("<stock-price>string</stock-price>"), "stock-price element found?");
        try (InputStream docStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            assertNotNull(DocumentHelper.readDocument(new XmlOptions(), docStream));
        }
    }

    @Test
    void testBase64Binary() throws Exception {
        XmlObject xobj;
        try (InputStream xsdStream = Xsd2InstTest.class.getResourceAsStream(BASE64BIN)) {
            xobj = XmlObject.Factory.parse(xsdStream, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
        }
        SchemaInstanceGenerator.Xsd2InstOptions options = new SchemaInstanceGenerator.Xsd2InstOptions();
        String result = SchemaInstanceGenerator.xsd2inst(new XmlObject[]{xobj}, "echoBase64BinaryElement", options);
        assertTrue(result.contains("<ns:echoBase64BinaryElement"), "echoBase64BinaryElement element found?");
        assertTrue(result.contains("<ns:base64BinaryElement>"), "base64BinaryElement element found?");
        try (InputStream docStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            assertNotNull(DocumentHelper.readDocument(new XmlOptions(), docStream));
        }
    }

    @Test
    void testSampleXmlUtil() throws Exception {
        XmlObject xobj;
        try (InputStream xsdStream = Xsd2InstTest.class.getResourceAsStream(PRICEQ)) {
            xobj = XmlObject.Factory.parse(xsdStream, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
        }
        SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{xobj}, XmlBeans.getBuiltinTypeSystem(), new XmlOptions());
        SchemaGlobalElement[] elements = sts.globalElements();
        SchemaGlobalElement element = elements[0];
        String result = SampleXmlUtil.createSampleForType(element);
        assertTrue(result.contains("<price-quote>"), "price-quote element found?");
        assertTrue(result.contains("<stock-symbol>string</stock-symbol>"), "stock-symbol element found?");
        assertTrue(result.contains("<stock-price>string</stock-price>"), "stock-price element found?");
        try (InputStream docStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            assertNotNull(DocumentHelper.readDocument(new XmlOptions(), docStream));
        }
    }
}
