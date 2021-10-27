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

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.DocumentHelper;
import org.apache.xmlbeans.impl.xsd2inst.SchemaInstanceGenerator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Xsd2InstTest extends TestCase {

    public void testPriceQuote() throws Exception {
        XmlObject xobj;
        try (InputStream xsdStream = Xsd2InstTest.class.getResourceAsStream(
                "/xbean/compile/scomp/pricequote/PriceQuote.xsd")) {
            xobj = XmlObject.Factory.parse(xsdStream,
                    (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
        }
        SchemaInstanceGenerator.Xsd2InstOptions options = new SchemaInstanceGenerator.Xsd2InstOptions();
        String result = SchemaInstanceGenerator.xsd2inst(new XmlObject[] {xobj}, "price-quote", options);
        assertTrue("price-quote element found?", result.contains("<price-quote>"));
        assertTrue("stock-symbol element found?", result.contains("<stock-symbol>string</stock-symbol>"));
        assertTrue("stock-price element found?", result.contains("<stock-price>"));
        try (InputStream docStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            assertNotNull(DocumentHelper.readDocument(new XmlOptions(), docStream));
        }
    }
}
