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

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.openuri.test.selectAttribute.DocDocument;
import xmlobject.common.SelectChildrenAttribCommon;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SelectAttributeTests extends SelectChildrenAttribCommon {

    private static String saUri = "http://openuri.org/test/selectAttribute";
    private static String saStartFrag = "<xm xmlns:sa=\"" + saUri + "\">";

    private static String abcUri = "http://abc";
    private static String defUri = "http://def";

    static String anyStartFrag = "<xm xmlns:sa=\"" + saUri + "\"" +
        " xmlns:abc=\"" + abcUri + "\"" +
        " xmlns:def=\"" + defUri + "\"" + ">";

    private static String endFrag = "</xm>";
    // To speed up tests when running multiple test methods in the same run
    private DocDocument.Doc doc = null;

    ///////////////////////////////////////////////////////////////////
    // Tests for non-wildcard attributes
    @Test
    public void testSelectWithQName()
        throws Exception {
        if (doc == null)
            doc = (DocDocument.Doc) getTestObject();
        QName qn = new QName("", "att1");
        XmlObject x = doc.getNormal().selectAttribute(qn);
        String exp = saStartFrag + "Attribute 1" + endFrag;

        validateTest("testSelectWithQName", exp, x);
        // Check Select with QName that is not present.. should get null back.
        x = doc.getWithOther().selectAttribute(qn);
        assertNull(x);
    }


    @Test
    public void testSelectWithURI()
        throws Exception {
        if (doc == null)
            doc = (DocDocument.Doc) getTestObject();

        XmlObject x = doc.getNormal().selectAttribute("", "att2");
        String exp = saStartFrag + "Attribute 2" + endFrag;

        validateTest("testSelectWithURI", exp, x);
        // Check Select with QName that is not present.. should get null back.
        x = doc.getWithAny().selectAttribute("", "att2");
        assertNull(x);

    }

    ////////////////////////////////////////////////////////////////////
    // Test for wild-card attributes
    @Test
    public void testSelectWithQNameForAny()
        throws Exception {
        if (doc == null)
            doc = (DocDocument.Doc) getTestObject();

        QName qn = new QName(abcUri, "att3");
        String exp = saStartFrag + "Attribute 3" + endFrag;
        XmlObject x = doc.getWithOther().selectAttribute(qn);

        validateTest("testSelectWithQNameForAny", exp, x);


        x = doc.getWithAny();
        System.out.println(x.xmlText());

    }

    ////////////////////////////////////////////////////////////////////
    // Helper
    private XmlObject getTestObject()
        throws Exception {
        String xml = getXml("xbean/xmlobject/SelectAttribute-Doc.xml");
        DocDocument xmlObj = DocDocument.Factory.parse(xml);
        DocDocument.Doc doc = xmlObj.getDoc();

        Collection errors = new ArrayList();
        opts.setErrorListener(errors);
        boolean valid = doc.validate(opts);
        tools.xml.Utils.printXMLErrors(errors);

        assertTrue("Test Xml is not valid!!", valid);
        return doc;
    }
}
