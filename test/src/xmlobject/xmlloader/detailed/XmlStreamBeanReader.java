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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;
import org.openuri.bea.samples.workshop.CreditCardDataDocument;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class XmlStreamBeanReader {


    @Test
    public void testXMLStreamReaderLoader() throws XMLStreamException, XmlException {
        String creditCardXmlwPrefix = " <cc:credit-card-data xmlns:cc=\"http://openuri.org/bea/samples/workshop\">\n" +
            " <cc:customer id=\"1\">\n" +
            " <cc:card name=\"MasterCard\" number=\"15385\">\n" +
            " <cc:available-credit>0</cc:available-credit>\n" +
            " <cc:credit-used>0</cc:credit-used>\n" +
            " </cc:card>\n" +
            " <cc:card name=\"Visa\" number=\"12346\">\n" +
            " <cc:available-credit>0</cc:available-credit>\n" +
            " <cc:credit-used>0</cc:credit-used>\n" +
            " </cc:card>\n" +
            " </cc:customer>\n" +
            " <cc:customer id=\"2\">\n" +
            " <cc:card name=\"MasterCard\" number=\"String\">\n" +
            " <cc:available-credit>0</cc:available-credit>\n" +
            " <cc:credit-used>0</cc:credit-used>\n" +
            " </cc:card>\n" +
            " <cc:card name=\"MasterCard\" number=\"String\">\n" +
            " <cc:available-credit>0</cc:available-credit>\n" +
            " <cc:credit-used>0</cc:credit-used>\n" +
            " </cc:card>\n" +
            " </cc:customer>\n" +
            " </cc:credit-card-data>";

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(creditCardXmlwPrefix.getBytes()));
        CreditCardDataDocument ccdoc = (CreditCardDataDocument) XmlObject.Factory.parse(reader, new XmlOptions().setDocumentType(CreditCardDataDocument.type));
        assertEquals(1, ccdoc.getCreditCardData().getCustomerArray(0).getId());
    }

    // test for IllegalStateException thrown on using XmlStreamReader
    @Test
    public void testXmlStreamReaderException() throws XMLStreamException {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc = xo.newCursor();
        xc.toNextToken();

        xc.insertElementWithText("int", "http://openuri.org/testNumerals", "5");
        xc.insertElementWithText("float", "http://openuri.org/testNumerals", "7.654321");

        XMLStreamReader xsr = xo.newXMLStreamReader();

        while (xsr.hasNext()) {
            xsr.next();
        }
    }
}
