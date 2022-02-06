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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.openuri.test.selectAttribute.DocDocument;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;
import static xmlobject.detailed.SelectChildrenTests.validateTest;

public class SelectAttributeTests {

    private static final String saUri = "http://openuri.org/test/selectAttribute";
    private static final String saStartFrag = "<xm xmlns:sa=\"" + saUri + "\">";
    private static final String abcUri = "http://abc";
    private static final String endFrag = "</xm>";

    ///////////////////////////////////////////////////////////////////
    // Tests for non-wildcard attributes
    @Test
    void testSelectWithQName() throws Exception {
        DocDocument.Doc doc = getTestObject();
        QName qn = new QName("", "att1");
        XmlObject x = doc.getNormal().selectAttribute(qn);
        String exp = saStartFrag + "Attribute 1" + endFrag;

        validateTest("testSelectWithQName", new String[]{exp}, new XmlObject[]{x});
        // Check Select with QName that is not present.. should get null back.
        x = doc.getWithOther().selectAttribute(qn);
        assertNull(x);
    }


    @Test
    void testSelectWithURI() throws Exception {
        DocDocument.Doc doc = getTestObject();

        XmlObject x = doc.getNormal().selectAttribute("", "att2");
        String exp = saStartFrag + "Attribute 2" + endFrag;

        validateTest("testSelectWithURI", new String[]{exp}, new XmlObject[]{x});
        // Check Select with QName that is not present.. should get null back.
        x = doc.getWithAny().selectAttribute("", "att2");
        assertNull(x);
    }

    ////////////////////////////////////////////////////////////////////
    // Test for wild-card attributes
    @Test
    void testSelectWithQNameForAny() throws Exception {
        DocDocument.Doc doc = getTestObject();

        QName qn = new QName(abcUri, "att3");
        String exp = saStartFrag + "Attribute 3" + endFrag;
        XmlObject x = doc.getWithOther().selectAttribute(qn);

        validateTest("testSelectWithQNameForAny", new String[]{exp}, new XmlObject[]{x});
        x = doc.getWithAny();
        assertNotNull(x.xmlText());
    }

    ////////////////////////////////////////////////////////////////////
    // Helper
    private DocDocument.Doc getTestObject() throws Exception {
        DocDocument xmlObj = (DocDocument) jobj("xbean/xmlobject/SelectAttribute-Doc.xml");
        DocDocument.Doc doc = xmlObj.getDoc();

        XmlOptions opts = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(2);

        Collection<XmlError> errors = new ArrayList<>();
        opts.setErrorListener(errors);
        boolean valid = doc.validate(opts);

        assertTrue(valid, "Test Xml is not valid!!");
        return doc;
    }
}
