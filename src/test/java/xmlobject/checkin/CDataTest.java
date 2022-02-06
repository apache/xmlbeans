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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for finner CData control feature.
 */
public class CDataTest {

    @ParameterizedTest
    @CsvSource(value = {
        "'<a><![CDATA[cdata text]]></a>'," +
        "'<a><![CDATA[cdata text]]></a>'," +
        "'<a><![CDATA[cdata text]]></a>'",
        // Bookmark doesn't seem to keep the length of the CDATA
        // "<a>NL<b><![CDATA[cdata text]]> regular text</b>NL</a>," +
        // "<a>\n<b>cdata text regular text</b>\n</a>," +
        // "<a>NL  <b>cdata text regular text</b>NL</a>"
        "'<a>\n<c>text <![CDATA[cdata text]]></c>\n</a>'," +
        "'<a>\n<c>text cdata text</c>\n</a>'," +
        "'<a>NL  <c>text cdata text</c>NL</a>'",
        // https://issues.apache.org/jira/browse/XMLBEANS-404
        "'<a>\n<c>text <![CDATA[cdata text]]]]></c>\n</a>'," +
        "'<a>\n<c>text cdata text]]</c>\n</a>'," +
        "'<a>NL  <c>text cdata text]]</c>NL</a>'"
    })
    void checkCData(String xmlText, String expected1, String expected2) throws XmlException {
        String NL = Stream.of(SystemProperties.getProperty("line.separator"),System.getProperty("line.separator"),"\n")
            .filter(Objects::nonNull).findFirst().get();

        XmlOptions opts = new XmlOptions();
        opts.setUseCDataBookmarks();

        XmlObject xo = XmlObject.Factory.parse(xmlText.replace("NL", NL), opts);

        String result1 = xo.xmlText(opts);
        assertEquals(expected1.replace("NL", NL), result1, "xmlText");

        opts.setSavePrettyPrint();
        String result2 = xo.xmlText(opts);
        assertEquals(expected2.replace("NL", NL), result2, "prettyPrint");
    }
}
