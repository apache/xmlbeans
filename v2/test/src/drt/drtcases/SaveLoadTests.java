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

package drtcases;

import org.w3c.dom.Document;
import org.apache.xmlbeans.impl.common.LoadSaveUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;

public class SaveLoadTests extends TestCase
{
    public SaveLoadTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(SaveLoadTests.class); }

    public void testLoadSave()
            throws IOException, SAXException, ParserConfigurationException, XmlException, XMLStreamException
    {
        File file = TestEnv.xbeanCase("xpath/testXPath.xml");

        Document doc = LoadSaveUtils.xmlText2GenericDom(new FileInputStream(file),
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        XmlObject xo = XmlObject.Factory.parse(doc);

        XMLStreamReader xsr = xo.newXMLStreamReader();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LoadSaveUtils.xmlStreamReader2XmlText(xsr, bos);

        check( XmlObject.Factory.parse(TestEnv.xbeanCase("xpath/testXPath.xml")).toString(), bos.toString() );
    }

    private static void check(String expected, String actual) throws XmlException
    {
        XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
        boolean match = XmlComparator.lenientlyCompareTwoXmlStrings(actual, expected, diag);
        Assert.assertTrue("------------  Found difference:" +
                " actual=\n'" + actual + "'\nexpected=\n'" + expected + "'\ndiagnostic=" + diag , match);
    }
}
