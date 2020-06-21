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

import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test for finner CData control feature.
 */
public class CDataTest {
    private static final String NL = SystemProperties.getProperty("line.separator") != null ?
        SystemProperties.getProperty("line.separator") :
        (System.getProperty("line.separator") != null ? System.getProperty("line.separator") : "\n");

    @Test
    public void testCData1() throws Exception {
        String xmlText = "<a><![CDATA[cdata text]]></a>";
        checkCData(xmlText, xmlText, xmlText);
    }

    @Test
    @Ignore("Bookmark doesn't seem to keep the length of the CDATA")
    public void testCData2() throws Exception {
        String xmlText =
            "<a>" + NL +
            "<b><![CDATA[cdata text]]> regular text</b>" + NL +
            "</a>";
        String expected1 =
            "<a>\n" +
            "<b>cdata text regular text</b>\n" +
            "</a>";
        String expected2 =
            "<a>" + NL +
            "  <b>cdata text regular text</b>" + NL +
            "</a>";

        checkCData(xmlText, expected1, expected2);
    }

    @Test
    public void testCData3() throws Exception {
        String xmlText =
            "<a>\n" +
            "<c>text <![CDATA[cdata text]]></c>\n" +
            "</a>";
        String expected1 =
            "<a>\n" +
            "<c>text cdata text</c>\n" +
            "</a>";
        String expected2 =
            "<a>" + NL +
            "  <c>text cdata text</c>" + NL +
            "</a>";

        checkCData(xmlText, expected1, expected2);
    }

    // https://issues.apache.org/jira/browse/XMLBEANS-404
    @Test
    public void testXmlBeans404()
        throws Exception {
        String xmlText =
            "<a>\n" +
            "<c>text <![CDATA[cdata text]]]]></c>\n" +
            "</a>";
        String expected1 =
            "<a>\n" +
            "<c>text cdata text]]</c>\n" +
            "</a>";
        String expected2 =
            "<a>" + NL +
            "  <c>text cdata text]]</c>" + NL +
            "</a>";

        checkCData(xmlText, expected1, expected2);
    }

    private void checkCData(String xmlText, String expected1, String expected2)
        throws XmlException {
        XmlOptions opts = new XmlOptions();
        opts.setUseCDataBookmarks();

        XmlObject xo = XmlObject.Factory.parse(xmlText, opts);

        String result1 = xo.xmlText(opts);
        assertEquals("xmlText", expected1, result1);

        opts.setSavePrettyPrint();
        String result2 = xo.xmlText(opts);
        assertEquals("prettyPrint", expected2, result2);
    }
}
