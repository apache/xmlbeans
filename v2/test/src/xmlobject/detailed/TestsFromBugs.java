
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
import com.mytest.Bar;
import com.mytest.Foo;
import com.mytest.Info;
import com.mytest.TestDocument;
import test.xmlobject.test36510.Test36510AppDocument;
import junit.framework.TestCase;

import java.io.File;

/**
 *  Test file that implements test cases that come from closing bugs.
 *
 *
 */
public class TestsFromBugs extends TestCase {
    File instance;

    public TestsFromBugs(String name) {
        super(name);
    }

    /**
     *  Radar Bug: 36156
     *  Problem with Namespace leaking into siblings
     */
    public void test36156()
            throws Exception {
        String str = "<x><y xmlns=\"bar\"><z xmlns=\"foo\"/></y><a/></x>";
        XmlObject x = XmlObject.Factory.parse(str);

        assertTrue("Test 36156 failed: ", x.xmlText().equals(str));
    }

    /*
     * Radar Bug: 36510
     */
    public void test36510()
            throws Exception {
        String str = "<test36510-app version='1.0' " +
                "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
                " xsi:schemaLocation='http://test/xmlobject/test36510' " +
                "xmlns='http://test/xmlobject/test36510'>" +
                "<testConstraint>" +
                "<customConstraint>" +
                "<description>These portlets don't" +
                " require any guarantee</description>" +
                "<options>BEST</options>" +
                "</customConstraint></testConstraint>" +
                "</test36510-app>";

        Test36510AppDocument doc = Test36510AppDocument.Factory.parse(str);
        str = doc.getTest36510App().getTestConstraintArray()[0].
                getCustomConstraint().getOptions().toString();
        assertTrue("Test 36510 failed: ", str.equals("BEST"));
    }


    /*
     * Radar Bug: 40907
     */
    public void test40907()
            throws Exception {
        String str = "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
                "<myt:foo>" +
                "<myt:fooMember>this is foo member</myt:fooMember>" +
                "</myt:foo>" +
                "</myt:Test>";
        TestDocument doc = TestDocument.Factory.parse(str);

        assertTrue("XML Instance did not validate.", doc.validate());

        Bar bar = Bar.Factory.newInstance();
        bar.setFooMember("new foo member");
        bar.setBarMember("new bar member");

        Info info = doc.getTest();

        Foo foo = info.addNewFoo();
        foo.set(bar);

        assertTrue("Modified XML instance did not validate.", doc.validate());
        str = "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
                "<myt:foo>" +
                "<myt:fooMember>this is foo member</myt:fooMember>" +
                "</myt:foo>" +
                "<myt:foo xsi:type=\"myt:bar\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<myt:fooMember>new foo member</myt:fooMember>" +
                "<myt:barMember>new bar member</myt:barMember>" +
                "</myt:foo>" +
                "</myt:Test>";
        assertEquals("XML instance is not as expected", doc.xmlText(), str);

    }
}
