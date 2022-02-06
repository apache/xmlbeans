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

package xmlobject.checkin;

import org.apache.xml.test.selectChldAtt.DocDocument;
import org.apache.xml.test.selectChldAtt.TypeExtendedC;
import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SelectChldAttTests {
    private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String URI = "http://xml.apache.org/test/selectChldAtt";

    private static final String XML =
        "<doc xmlns='" + URI + "'>\n" +
        "  <int>7</int>\n" +
        "  <string> ... some text ... </string>\n" +

        "  <elemA price='4.321'>\n" +
        "    <topLevelElement> this is wildcard bucket </topLevelElement>\n" +
        "  </elemA>\n" +

        "  <elemB xmlns:p='uri:other_namespace' \n" +
        "       p:att='attribute in #other namespace'>\n" +
        "    <someElement>2</someElement>\n" +
        "    <p:otherElement> element in #other namespace </p:otherElement>\n" +
        "  </elemB>\n" +

        "  <elemC xmlns:xsi='" + XSI_URI + "' \n" +
        "         xmlns:p='uri_other_namespace' \n" +
        "         xsi:type='typeExtendedC' \n" +
        "         att1='attribute from typeC' \n" +
        "         aditionalAtt='attribute added in type extension' \n" +
        "         p:validAtt='attribute in any bucket' >\n" +
        "    <someElement> element from typeC </someElement>\n" +
        "    <p:validElement> element in the 'any' bucket for typeExtendedC </p:validElement>\n" +
        "    <aditionalElement> element from typeExtendedC </aditionalElement>\n" +
        "  </elemC>\n" +
        "</doc>";


    @Test
    void testSelect() throws XmlException {
        DocDocument document = DocDocument.Factory.parse(XML);
        DocDocument.Doc doc = document.getDoc();
        Collection<XmlError> errors = new ArrayList<>();
        assertTrue(doc.validate(new XmlOptions().setErrorListener(errors)), "Valid instance");

        // select a known element
        String[] act1 = toArray(doc.selectChildren(new QName(URI, "int")));
        String[] exp1 = {"<xml-fragment>7</xml-fragment>"};
        assertArrayEquals(exp1, act1, "1 selectChildren 'int'");

        String[] act2 = toArray(doc.selectChildren(URI, "string"));
        String[] exp2 = {"<xml-fragment> ... some text ... </xml-fragment>"};
        assertArrayEquals(exp2, act2, "2 selectChildren 'string'");

        // elemA
        String[] act3 = toArray(doc.selectChildren(new QName(URI, "elemA")));
        String[] exp3 = {
            "<xml-fragment price=\"4.321\" xmlns:sel=\"" + URI + "\">" +
            "  <sel:topLevelElement> this is wildcard bucket </sel:topLevelElement>" +
            "</xml-fragment>"
        };
        assertArrayEquals(exp3, act3, "3 selectChildren 'elemA'");

        // select a known attribute
        final XmlObject xo1 = doc.selectChildren(new QName(URI, "elemA"))[0];
        String[] act4 = toArray(xo1.selectAttribute(new QName("", "price")));
        String[] exp4 = {"<xml-fragment>4.321</xml-fragment>"};
        assertArrayEquals(exp4, act4, "4     selectAttribute 'price'");

        // select all attributes
        String[] act5 = toArray(xo1.selectAttributes(QNameSet.forWildcardNamespaceString("##any", URI)));
        String[] exp5 = {"<xml-fragment>4.321</xml-fragment>"};
        assertArrayEquals(exp5, act5, "5     selectAttributes set'##any'");

        // elemB
        final XmlObject xo6 = doc.selectChildren(new QName(URI, "elemB"))[0];
        assertNotNull(xo6, "6 selectChildren 'elemB'");

        String[] act7 = toArray(xo6.selectChildren(QNameSet.forWildcardNamespaceString("##other", URI)));
        String[] exp7 = {"<xml-fragment xmlns:p=\"uri:other_namespace\"> element in #other namespace </xml-fragment>"};
        assertArrayEquals(exp7, act7, "7     selectChildren set'##other'");

        String[] act8 = toArray(xo6.selectAttributes(QNameSet.forWildcardNamespaceString("##other", URI)));
        String[] exp8 = {"<xml-fragment xmlns:p=\"uri:other_namespace\">attribute in #other namespace</xml-fragment>"};
        assertArrayEquals(exp8, act8, "8     selectAttributes set'##other'");

        // elemC
        XmlObject xo9 = doc.selectChildren(new QName(URI, "elemC"))[0];
        assertNotNull(xo9, "9 selectChildren 'elemC'");

        String[] act10 = toArray(xo9.selectChildren(QNameSet.forWildcardNamespaceString("##any", URI)));
        String[] exp10 = {
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\"> element from typeC </xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\"> element in the 'any' bucket for typeExtendedC </xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\"> element from typeExtendedC </xml-fragment>"
        };
        assertArrayEquals(exp10, act10, "10    selectChildren set'##any'");

        // select elements in the any bucket by excluding the the known elements
        QNameSetBuilder qnsb = new QNameSetBuilder();
        qnsb.add(new QName(URI, "someElement"));
        qnsb.add(new QName(URI, "aditionalElement"));
        qnsb.invert();

        String[] act11a = toArray(xo9.selectChildren(qnsb.toQNameSet()));
        String[] exp11a = {"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\"> element in the 'any' bucket for typeExtendedC </xml-fragment>"};
        assertArrayEquals(exp11a, act11a, "11a    selectChildren in the any bucket for typeExtendedC");

        String[] act11b = toArray(xo9.selectChildren(TypeExtendedC.type.qnameSetForWildcardElements()));
        String[] exp11b = {"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\"> element in the 'any' bucket for typeExtendedC </xml-fragment>"};
        assertArrayEquals(exp11b, act11b, "11b    selectChildren in the any bucket for typeExtendedC");

        // select attributes in the any bucket by excluding the the known attributes
        qnsb = new QNameSetBuilder();
        qnsb.add(new QName("", "att1"));
        qnsb.add(new QName("", "aditionalAtt"));
        qnsb.add(new QName(XSI_URI, "type"));
        qnsb.invert();

        String[] act12a = toArray(xo9.selectAttributes(qnsb.toQNameSet()));
        String[] exp12a = {"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">attribute in any bucket</xml-fragment>"};
        assertArrayEquals(exp12a, act12a, "12a    selectChildren in the any bucket for typeExtendedC");

        String[] act12b = toArray(xo9.selectAttributes(TypeExtendedC.type.qnameSetForWildcardAttributes()));
        String[] exp12b = {
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">typeExtendedC</xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">attribute in any bucket</xml-fragment>"
        };
        assertArrayEquals(exp12b, act12b, "12b    selectChildren in the any bucket for typeExtendedC");
    }

    private static String[] toArray(XmlObject... xos) {
        return Stream.of(xos).map(XmlObject::toString).map(s -> s.replaceAll("[\\r\\n]", "")).toArray(String[]::new);
    }
}
