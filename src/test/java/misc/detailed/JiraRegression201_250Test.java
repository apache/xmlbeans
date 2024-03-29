/*
 *   Copyright 2004 The Apache Software Foundation
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
package misc.detailed;

import jira.xmlbeans228.substitution.CommentType;
import jira.xmlbeans228.substitution.FirstCommentType;
import jira.xmlbeans228.substitution.PersonDocument;
import misc.common.JiraTestBase;
import misc.detailed.jira208.FrogBreathDocument;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JiraRegression201_250Test extends JiraTestBase {

    /*
     * [XMLBEANS-206]: Wrong method finding in getMethod() of InterfaceExtensionImpl
     *
     */
    // Refer test case xmlobject.extensions.interfaceFeature.averageCase.checkin.testJiraXMLBEANS_206

    /*
     * [XMLBEANS-208]: validation of decimal with fractionDigits -- special case, additional zero digits
     *
     */
    @Test
    void test_jira_xmlbeans208() throws Exception {

        XmlOptions options = new XmlOptions();
        List<XmlError> err = new ArrayList<>();
        options.setErrorListener(err);

        // decimal value invalid
        FrogBreathDocument invalidDoc = FrogBreathDocument.Factory.parse("<dec:frog_breath xmlns:dec=\"http://misc/detailed/jira208\">1000.000001</dec:frog_breath>");
        boolean valid = invalidDoc.validate(options);
        if (!valid) {
            for (XmlError xmlError : err) {
                System.out.println("Validation Error (invalid doc):" + xmlError);
            }
        }
        // expected to fail
        assertFalse(valid);

        // decimal value with trailing zeros tagged as invalid
        FrogBreathDocument validDoc = FrogBreathDocument.Factory.parse("<dec:frog_breath xmlns:dec=\"http://misc/detailed/jira208\">1000.000000</dec:frog_breath>");

        err.clear();
        boolean valid2 = validDoc.validate(options);
        if (!valid2) {
            for (XmlError xmlError : err) {
                System.out.println("Validation Error (valid doc):" + xmlError);
            }
        }

        assertTrue(valid2);
    }

    /*
     * [XMLBEANS-228]:
     * element order in sequence incorrect after calling substitute()
     */
    @Test
    void test_jira_xmlbeans228() throws Exception {
        PersonDocument personDocument = PersonDocument.Factory.newInstance();
        PersonDocument.Person person = personDocument.addNewPerson();
        CommentType commentType = person.addNewComment();
        String ns = "http://jira/xmlbeans_228/substitution";
        QName qName = new QName(ns, "FirstCommentElement");
        Object resultObject = commentType.substitute(qName, FirstCommentType.type);
        FirstCommentType firstCommentType = (FirstCommentType) resultObject;
        firstCommentType.setStringValue("ThirdElement");
        person.setComment(firstCommentType);

        person.setFirstName("FirstElement");
        person.setLastName("SecondElement");

        XmlOptions opts = new XmlOptions().setSavePrettyPrint().setUseDefaultNamespace();
        StringWriter out = new StringWriter();
        personDocument.save(out, opts);

        String exp =
            "<Person xmlns=\"http://jira/xmlbeans_228/substitution\">" + NEWLINE +
            "  <FirstName>FirstElement</FirstName>" + NEWLINE +
            "  <LastName>SecondElement</LastName>" + NEWLINE +
            "  <FirstCommentElement>ThirdElement</FirstCommentElement>" + NEWLINE +
            "</Person>";

        assertEquals(exp, out.toString());
        assertTrue(personDocument.validate(), "Wrong element order!");
    }
}
