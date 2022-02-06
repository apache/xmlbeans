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


import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import org.openuri.test.selectChildren.ElemWithAnyDocument;
import org.openuri.test.selectChildren.NormalDocument;
import org.openuri.test.selectChildren.NormalType;
import org.openuri.test.selectChildren.WithAnyType;
import tools.xml.XmlComparator;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;

public class SelectChildrenTests {
    private static final String scUri = "http://openuri.org/test/selectChildren";
    private static final String scStartFrag = "<xm xmlns:sc=\"" + scUri + "\">";

    private static final String abcUri = "http://abc";
    private static final String defUri = "http://def";
    private static final String xyzUri = "http://xyz";

    private static final String anyStartFrag =
        "<xm xmlns:sc=\"" + scUri + "\"" +
        " xmlns:abc=\"" + abcUri + "\"" +
        " xmlns:def=\"" + defUri + "\"" +
        " xmlns:xyz=\"" + xyzUri + "\"" + ">";

    private static final String endFrag = "</xm>";

    //////////////////////////////////////////////////////////////////
    // Tests
    @Test
    void testSelectWithQName() throws Exception {
        NormalDocument doc = (NormalDocument) jobj("xbean/xmlobject/SelectChildren-NormalDoc.xml");
        assertTrue(doc.validate());
        NormalType norm = doc.getNormal();

        // For the expected xml strings
        String[] exps = new String[]{scStartFrag + "first element" + endFrag};
        // For the return from selectChildren
        XmlObject[] xos = norm.selectChildren(new QName(scUri, "first"));

        this.validateTest("testSelectWithQName", exps, xos);
    }

    @Test
    void testSelectWithURI() throws Exception {
        NormalDocument doc = (NormalDocument) jobj("xbean/xmlobject/SelectChildren-NormalDoc.xml");
        assertTrue(doc.validate());
        NormalType norm = doc.getNormal();

        // For the expected xml strings
        String[] exps = new String[]{scStartFrag + "second element" + endFrag};
        // For the return from selectChildren
        XmlObject[] xos = norm.selectChildren(scUri, "second");

        this.validateTest("testSelectWithURI", exps, xos);
    }

    @Test
    void testSelectWithQNameSet() throws Exception {
        NormalDocument doc = (NormalDocument) jobj("xbean/xmlobject/SelectChildren-NormalDoc.xml");
        assertTrue(doc.validate());
        NormalType norm = doc.getNormal();

        QName[] qArr = new QName[]{
            new QName(scUri, "first"),
            new QName(scUri, "numbers"),
            new QName(scUri, "second")};

        QNameSet qSet = QNameSet.forArray(qArr);

        // For the expected xml strings
        String[] exps = new String[]{
            scStartFrag + "first element" + endFrag,
            scStartFrag + "second element" + endFrag,
            scStartFrag + "10" + endFrag,
            scStartFrag + "11" + endFrag,
            scStartFrag + "12" + endFrag};

        // For the return from selectChildren
        XmlObject[] xos = norm.selectChildren(qSet);

        this.validateTest("testSelectWithQNameSet", exps, xos);
    }

    //////////////////////////////////////////////////////////////////////
    // Tests with 'any' Element
    @Test
    void testSelectWithQNameForAny() throws Exception {
        ElemWithAnyDocument doc = (ElemWithAnyDocument) jobj("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();
        // Select children from a known namespace
        // For the return from selectChildren
        XmlObject[] xos = any.selectChildren(new QName(defUri, "someElem2"));
        // For the expected xml strings
        String[] exps = new String[]{anyStartFrag + "DEF Namespace" + endFrag};

        validateTest("testSelectWithQNameForAny", exps, xos);
    }

    @Test
    void testSelectWithURIForAny() throws Exception {

        ElemWithAnyDocument doc = (ElemWithAnyDocument) jobj("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();
        // Select children from a known namespace
        // For the return from selectChildren
        XmlObject[] xos = any.selectChildren(scUri, "simple");
        // For the expected xml strings
        String[] exps = new String[]{anyStartFrag + "Simple String" + endFrag};

        validateTest("testSelectWithURIForAny", exps, xos);
    }

    @Test
    void testSelectWithWildcard() throws Exception {
        ElemWithAnyDocument doc = (ElemWithAnyDocument) jobj("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();

        // For the return from selectChildren
        XmlObject[] xos = any.selectChildren(QNameSet.forWildcardNamespaceString("##other", scUri));
        // For the expected xml strings
        String[] exps = new String[]{
            anyStartFrag + "ABC Namespace" + endFrag,
            anyStartFrag + "DEF Namespace" + endFrag,
            anyStartFrag + "XYX Namespace" + endFrag,
            anyStartFrag + "ABC-SameElem" + endFrag,
            anyStartFrag + "DEF-SameElem" + endFrag,
            anyStartFrag + "XYZ-SameElem" + endFrag};

        validateTest("testSelectWithWildcard", exps, xos);
    }

    @Test
    void testSelectWithQNameBuilder() throws Exception {
        // For the expected xml strings
        ElemWithAnyDocument doc = (ElemWithAnyDocument) jobj("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();

        Set<QName> excFromIncSet = new HashSet<QName>();
        excFromIncSet.add(new QName(scUri, "simple"));

        Set<String> excSet = new HashSet<String>();
        excSet.add(xyzUri);

        Set<QName> incFromExcSet = new HashSet<QName>();
        incFromExcSet.add(new QName(xyzUri, "sameElem"));

        QNameSet qset = new QNameSetBuilder(excSet, null, excFromIncSet, incFromExcSet).toQNameSet();
        // For the return from selectChildren
        XmlObject[] xos = any.selectChildren(qset);

        for (XmlObject xo : xos) {
            assertNotNull(xo.xmlText());
        }
    }

    //////////////////////////////////////////////////////////////////
    // Helper methods
    protected static void validateTest(String testName, String[] exps, XmlObject[] act) throws Exception {
        assertEquals(act.length, exps.length,
            testName + ": Return array has more/less elements than expected: " + act.length);

        for (int i = 0; i < act.length; i++) {
            XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
            String actual = convertFragToDoc(act[i].xmlText());
            boolean same = XmlComparator.lenientlyCompareTwoXmlStrings(actual, exps[i], diag);
            assertTrue(same);
        }
    }


    /**
     * This is a workaround for using XmlComparator to compare XML that are just
     * a single value like '7' wrapped in <xml-fragemnt> tags. Inside
     * XmlComparator creates XmlObjects and <xml-fragment> tags are ignored. So
     * this method will replace that with something like <xm> so that they look
     * like Xml Docs...
     */
    private static String convertFragToDoc(String xmlFragment) {
        String startFragStr = "<xml-fragment";
        String endFragStr = "</xml-fragment>";
        String startReplacementStr = "<xm";
        String endReplacementStr = "</xm>";

        Pattern pattern = Pattern.compile(startFragStr);
        Matcher matcher = pattern.matcher(xmlFragment);

        String xmlDoc = matcher.replaceAll(startReplacementStr);

        pattern = Pattern.compile(endFragStr);
        matcher = pattern.matcher(xmlDoc);

        xmlDoc = matcher.replaceAll(endReplacementStr);

        return xmlDoc;
    }
}
