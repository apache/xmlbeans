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

package xmlcursor.xpath.complex.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import xmlcursor.xpath.common.XPathCommon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class XPathTests {
    static final String XML =
        "<?xml version=\"1.0\"?>" +
        "<doc xmlns:ext=\"http://somebody.elses.extension\">" +
        "<a test=\"test\" />" +
        "<b attr1=\"a1\" attr2=\"a2\"   " +
        "xmlns:java=\"http://xml.apache.org/xslt/java\">" +
        "    <a>" +
        "    </a>" +
        "</b>" +
        "</doc><!-- -->         ";

    private static final String[] STEPS = {
        /*  0 */ "<xml-fragment xmlns:ext=\"http://somebody.elses.extension\"/>",
        /*  1 */ "<doc xmlns:ext=\"http://somebody.elses.extension\"><a test=\"test\" /><b attr1=\"a1\" attr2=\"a2\" xmlns:java=\"http://xml.apache.org/xslt/java\"> <a /> </b></doc>",
        /*  2 */ "<a test=\"test\" xmlns:ext=\"http://somebody.elses.extension\"/>",
        /*  3 */ "<xml-fragment test=\"test\" xmlns:ext=\"http://somebody.elses.extension\" /> ",
        /*  4 */ "<a xmlns:java=\"http://xml.apache.org/xslt/java\" xmlns:ext=\"http://somebody.elses.extension\" />",
        /*  5 */ "<b attr1=\"a1\" attr2=\"a2\" xmlns:java=\"http://xml.apache.org/xslt/java\"> <a /> </b>",
        /*  6 */ "<xml-fragment attr1=\"a1\" xmlns:java=\"http://xml.apache.org/xslt/java\" xmlns:ext=\"http://somebody.elses.extension\" />",
        /*  7 */ "<xml-fragment attr2=\"a2\" xmlns:java=\"http://xml.apache.org/xslt/java\" xmlns:ext=\"http://somebody.elses.extension\" />",
        /*  8 */ "<xml-fragment><!-- --></xml-fragment>",
        /*  9 */ " <xml-fragment xmlns:java=\"http://xml.apache.org/xslt/java\" xmlns:ext=\"http://somebody.elses.extension\" />",
        /* 10 */ "<a>    </a>",
        /* 11 */ "<xml-fragment>    </xml-fragment>"
    };

    private static final String XMLFRAG_EMPTY = "<xml-fragment/>";
    private static XmlObject doc;

    @Parameter
    public String xpath = null;
    @Parameter(value = 1)
    public String[] expected = null;

    @BeforeClass
    public static void init() throws XmlException {
        doc = XmlObject.Factory.parse(XML);
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();

        add(data, "/doc/a/@test", STEPS[2]);
        add(data, "//.", XML, STEPS[1], STEPS[2], STEPS[5], XMLFRAG_EMPTY, STEPS[10], XMLFRAG_EMPTY, STEPS[8]);
        add(data, "/doc", STEPS[1]);
        add(data, "/doc/a", STEPS[2]);
        add(data, "//@*", STEPS[3], STEPS[6], STEPS[7]);
        add(data, ".", XML);
        add(data, "//ancestor-or-self::*", XML, STEPS[2], STEPS[5], STEPS[10]);
        add(data, "./child::*[1]", STEPS[1]);
        add(data, "//descendant-or-self::*/@*[1]", STEPS[2], STEPS[6]);

        // This is tricky:
        // The expression "*" is true for the principal axis: since the axis is self, so we're looking for elements: doc
        // elt node() also returns the doc elt, but also the comment nodes in the union set are returned in doc order
        add(data, "//@* | * | node()", STEPS[1], STEPS[3], STEPS[6], STEPS[7], STEPS[8]);

        add(data, "//*", STEPS[1], STEPS[2], STEPS[5], STEPS[4]);
        add(data, "/doc/n", (String) null);
        add(data, "//descendant::comment()", STEPS[8]);
        add(data, "//*[local-name()='a']", STEPS[2], STEPS[4]);
        add(data, "//*/@*", STEPS[3], STEPS[6], STEPS[7]);
        add(data, "//*[last()]", STEPS[1], STEPS[5], STEPS[4]);
        add(data, "doc/*[last()]", STEPS[5]);

        // TODO: BUGBUG: fix this
        add(data, "/doc/a/*/@*", (String) null);

        add(data, "doc/descendant::node()", STEPS[2], STEPS[5], STEPS[11], STEPS[10], STEPS[11]);
        add(data, "doc/a/@*", STEPS[2]);
        add(data, "doc/b/a/ancestor-or-self::*", STEPS[1], STEPS[5], STEPS[4]);
        add(data, "doc/b/a/preceding::*", STEPS[2]);
        add(data, "doc/a/following::*", STEPS[5], STEPS[4]);
        add(data, "/doc/b/preceding-sibling::*", STEPS[2]);
        add(data, "/doc/a/following-sibling::*", STEPS[5]);

        // "/doc/namespace::*", STEPS[0],DEFAULT_NS};

        return data;
    }

    private static void add(List<Object[]> data, String xpath, String... expected) {
        data.add(new Object[]{xpath, expected});
    }


    @Test
    public void testConformance() {
        XmlCursor actual = doc.newCursor();
        try {
            actual.selectPath(xpath);

            if (actual.getSelectionCount() == 0) {
                assertNull(expected[0]);
                return;
            }

            XmlObject[] expXO = Stream.of(expected).map(XPathTests::parse).toArray(XmlObject[]::new);
            XPathCommon.compare(actual, expXO);
        } finally {
            actual.dispose();
        }
    }

    private static XmlObject parse(String str) {
        try {
            return XmlObject.Factory.parse(str);
        } catch (XmlException e) {
            fail(e.getMessage());
            return null;
        }
    }
}
