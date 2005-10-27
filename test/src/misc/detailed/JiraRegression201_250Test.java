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

import misc.common.JiraTestBase;
import misc.detailed.jira208.FrogBreathDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;

/**
 *
 */
public class JiraRegression201_250Test extends JiraTestBase
{
    public JiraRegression201_250Test(String name)
    {
        super(name);
    }

    /*
    * [XMLBEANS-206]: Wrong method finding in getMethod() of InterfaceExtensionImpl
    *
    */
    // Refer test case xmlobject.extensions.interfaceFeature.averageCase.checkin.testJiraXMLBEANS_206

    /*
    * [XMLBEANS-208]: validation of decimal with fractionDigits -- special case, additional zero digits
    *
    */
    public void test_jira_xmlbeans208() throws Exception {

        XmlOptions options = new XmlOptions();
        List err = new ArrayList();
        options.setErrorListener(err);

        // decimal value invalid
        FrogBreathDocument invalidDoc = FrogBreathDocument.Factory.parse("<dec:frog_breath xmlns:dec=\"http://misc/detailed/jira208\">1000.000001</dec:frog_breath>");
        boolean valid = invalidDoc.validate(options);
        if(!valid)
        {
            for (Iterator iterator = err.iterator(); iterator.hasNext();) {
                System.out.println("Validation Error (invalid doc):" + iterator.next());
            }
        }
        // expected to fail
        assertFalse(valid);

        // decimal value with trailing zeros tagged as invalid
        FrogBreathDocument validDoc = FrogBreathDocument.Factory.parse("<dec:frog_breath xmlns:dec=\"http://misc/detailed/jira208\">1000.000000</dec:frog_breath>");

        err.clear();
        boolean valid2 = validDoc.validate(options);
        if(!valid2)
        {
            for (Iterator iterator = err.iterator(); iterator.hasNext();) {
                System.out.println("Validation Error (valid doc):" + iterator.next());
            }
        }

        assertTrue(valid2);
    }

}
