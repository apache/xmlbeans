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


package xmlobject.detailed;

import knextest.bug38361.TestDocument;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CarLocationMessageDocument.CarLocationMessage;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class NilTest extends BasicCursorTestCase {
    @Test
    public void testIsNilFalse() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        CarLocationMessage clm = clmDoc.getCarLocationMessage();
        assertFalse(clm.isNil());
    }

    @Test
    public void testSetNilNillable() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        m_xo = pod.getPurchaseOrder().getShipTo().xgetName();
        m_xo.setNil();
        assertTrue(m_xo.isNil());
    }

    @Test(expected = XmlValueNotNillableException.class)
    public void testSetNilNotNillable() throws Exception {
        XmlOptions xo = new XmlOptions();
        xo.setValidateOnSet();
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM), xo);
        clmDoc.setNil();
    }

    /**
     * Test case for Radar bug: #38361
     */
    @Test
    public void nillableTest() throws Exception {
        //Generates a xml document programatically
        TestDocument generated = TestDocument.Factory.newInstance();
        generated.addNewTest();
        generated.getTest().setNilSimple();
        generated.getTest().setNilDate();

        // Generate a xml document by parsing a string
        TestDocument parsed = TestDocument.Factory.parse("<tns:Test xmlns:tns='http://bug38361.knextest'>" +
                "<tns:Simple xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'/>" +
                "<tns:Date xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'/>" +
                "</tns:Test>");

        // Test generated xml doc properties
        assertTrue("Generated XML document is not valid", generated.validate());
        assertTrue("Generated: isNilSimple() failed",
                generated.getTest().isNilSimple());
        assertTrue("Generated: isNilDate() failed",
                generated.getTest().isNilDate());

        // Test parsed xml doc properties
        assertTrue("Parsed XML document is not valid", parsed.validate());
        assertTrue("Parsed: isNilSimple() failed",
                parsed.getTest().isNilSimple());
        assertTrue("Parsed: isNilDate() failed", parsed.getTest().isNilDate());
    }

}

