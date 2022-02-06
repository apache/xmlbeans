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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xmlcursor.xpath.common.XPathCommon;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static xmlcursor.common.BasicCursorTestCase.jcur;

public class XPathTest {
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

    @BeforeAll
    public static void init() throws XmlException {
        doc = XmlObject.Factory.parse(XML);
    }

    public static Stream<Arguments> dataCheckin() {
        return Stream.of(
            addConf("/doc/a/@test", STEPS[2]),
            addConf("//.", XML, STEPS[1], STEPS[2], STEPS[5], XMLFRAG_EMPTY, STEPS[10], XMLFRAG_EMPTY, STEPS[8]),
            addConf("/doc", STEPS[1]),
            addConf("/doc/a", STEPS[2]),
            addConf("//@*", STEPS[3], STEPS[6], STEPS[7]),
            addConf(".", XML),
            addConf("//ancestor-or-self::*", XML, STEPS[2], STEPS[5], STEPS[10]),
            addConf("./child::*[1]", STEPS[1]),
            addConf("//descendant-or-self::*/@*[1]", STEPS[2], STEPS[6]),

            // This is tricky:
            // The expression "*" is true for the principal axis: since the axis is self, so we're looking for elements: doc
            // elt node() also returns the doc elt, but also the comment nodes in the union set are returned in doc order
            addConf("//@* | * | node()", STEPS[1], STEPS[3], STEPS[6], STEPS[7], STEPS[8]),

            addConf("//*", STEPS[1], STEPS[2], STEPS[5], STEPS[4]),
            addConf("/doc/n", (String) null),
            addConf("//descendant::comment()", STEPS[8]),
            addConf("//*[local-name()='a']", STEPS[2], STEPS[4]),
            addConf("//*/@*", STEPS[3], STEPS[6], STEPS[7]),
            addConf("//*[last()]", STEPS[1], STEPS[5], STEPS[4]),
            addConf("doc/*[last()]", STEPS[5]),

            // TODO: BUGBUG: fix this
            addConf("/doc/a/*/@*", (String) null),

            addConf("doc/descendant::node()", STEPS[2], STEPS[5], STEPS[11], STEPS[10], STEPS[11]),
            addConf("doc/a/@*", STEPS[2]),
            addConf("doc/b/a/ancestor-or-self::*", STEPS[1], STEPS[5], STEPS[4]),
            addConf("doc/b/a/preceding::*", STEPS[2]),
            addConf("doc/a/following::*", STEPS[5], STEPS[4]),
            addConf("/doc/b/preceding-sibling::*", STEPS[2]),
            addConf("/doc/a/following-sibling::*", STEPS[5])

            // "/doc/namespace::*", STEPS[0],DEFAULT_NS};
        );
    }

    private static Arguments addConf(String xpath, String... expected) {
        return Arguments.of(xpath, expected);
    }

    @ParameterizedTest
    @MethodSource("dataCheckin")
    void testConformance(String xpath, String[] expected) {
        try (XmlCursor actual = doc.newCursor()) {
            actual.selectPath(xpath);

            if (actual.getSelectionCount() == 0) {
                assertNull(expected[0]);
                return;
            }

            XmlObject[] expXO = Stream.of(expected).map(XPathTest::parse).toArray(XmlObject[]::new);
            XPathCommon.compare(actual, expXO);
        }
    }


    public static Stream<Arguments> dataZvon() {
        return Stream.of(
        addZvon(1, "/AAA", "<AAA><BBB/><CCC/><BBB/><BBB/><DDD><BBB/></DDD><CCC/></AAA>"),
        addZvon(1, "/AAA/CCC", "<CCC/>", "<CCC/>"),
        addZvon(1, "/AAA/DDD/BBB", "<BBB/>"),

        addZvon(2, "//BBB", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>"),
        addZvon(2, "//DDD/BBB", "<BBB/>", "<BBB/>", "<BBB/>"),

        addZvon(3, "/AAA/CCC/DDD/*", "<BBB/>", "<BBB/>", "<EEE/>", "<FFF/>"),
        addZvon(3, "/*/*/*/BBB", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB><BBB/></BBB>"),

        //according to Galax the document order is :
        addZvon(3, "//*",
            "<AAA><XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX><CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC><CCC><BBB><BBB><BBB/></BBB></BBB></CCC></AAA>",
            "<XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX>", "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>", "<BBB/>", "<BBB/>",
            "<EEE/>", "<FFF/>", "<CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC>", "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>",
            "<BBB/>", "<BBB/>", "<EEE/>", "<FFF/>", "<CCC><BBB><BBB><BBB/></BBB></BBB></CCC>",
            "<BBB><BBB><BBB/></BBB></BBB>", "<BBB><BBB/></BBB>", "<BBB/>"),

        addZvon(4, "/AAA/BBB[1]", "<BBB/>"),
        addZvon(4, "/AAA/BBB[last()]", "<BBB/>"),

        addZvon(5, "//@id", "<xml-fragment id=\"b1\"/>", "<xml-fragment id=\"b2\"/>"),
        addZvon(5, "//BBB[@id]", "<BBB id = \"b1\"/>", "<BBB id = \"b2\"/>"),
        addZvon(5, "//BBB[@name]", "<BBB name=\"bbb\"/>"),
        addZvon(5, "//BBB[@*]", "<BBB id = \"b1\"/>", "<BBB id = \"b2\"/>", "<BBB name=\"bbb\"/>"),
        addZvon(5, "//BBB[not(@*)]", "<BBB/>"),

        addZvon(6, "//BBB[@id='b1']", "<BBB id = \"b1\"/>"),
        addZvon(6, "//BBB[@name='bbb']", "<BBB name=\"bbb\"/>"),
        addZvon(6, "//BBB[normalize-space(@name)='bbb']", "<BBB name=\" bbb \"/>", "<BBB name=\"bbb\"/>"),

        addZvon(7, "//*[count(BBB)=2]", "<DDD><BBB/><BBB/></DDD>"),
        addZvon(7, "//*[count(*)=2]", "<DDD><BBB/><BBB/></DDD>", "<EEE><CCC/><DDD/></EEE>"),
        addZvon(7, "//*[count(*)=3]",
            "<AAA><CCC><BBB/><BBB/><BBB/></CCC><DDD><BBB/><BBB/></DDD><EEE><CCC/><DDD/></EEE></AAA>", "<CCC><BBB/><BBB/><BBB/></CCC>"),

        addZvon(8, "//*[name()='BBB']", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>"),
        addZvon(8, "//*[starts-with(name(),'B')]", "<BCC><BBB/><BBB/><BBB/></BCC>",
            "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BEC><CCC/><DBD/></BEC>"),

        // ykadiysk: Jaxen prints in BF left-to-right order but XPath wants doc order
        // addZvon("zvon8.xml", "//*[starts-with(name(),'B')]", "<BCC><BBB/><BBB/><BBB/></BCC>",
        //    "<BBB/>", "<BBB/>", "<BEC><CCC/><DBD/></BEC>", "<BBB/>", "<BBB/>", "<BBB/>"),

        addZvon(8, "//*[contains(name(),'C')]", "<BCC><BBB/><BBB/><BBB/></BCC>", "<BEC><CCC/><DBD/></BEC>", "<CCC/>"),

        addZvon(9, "//*[string-length(name()) = 3]", "<AAA><Q/><SSSS/><BB/><CCC/><DDDDDDDD/><EEEE/></AAA>", "<CCC/>"),
        addZvon(9, "//*[string-length(name()) < 3]", "<Q/>", "<BB/>"),
        addZvon(9, "//*[string-length(name()) > 3]", "<SSSS/>", "<DDDDDDDD/>", "<EEEE/>"),

        addZvon(10, "$this//CCC | $this//BBB", "<BBB/>", "<CCC/>", "<CCC/>"),
        // Nodes are returned in document order
        addZvon(10, "$this/AAA/EEE | $this//BBB", "<BBB/>", "<EEE/>"),
        addZvon(10, "./AAA/EEE |.//DDD/CCC | ./AAA | .//BBB", "<AAA><BBB/><CCC/><DDD><CCC/></DDD><EEE/></AAA>",
            "<BBB/>", "<CCC/>", "<EEE/>"),

        addZvon(11, "/AAA", "<AAA><BBB/><CCC/></AAA>"),
        addZvon(11, "/child::AAA", "<AAA><BBB/><CCC/></AAA>"),
        addZvon(11, "/AAA/BBB", "<BBB/>"),
        addZvon(11, "/child::AAA/child::BBB", "<BBB/>"),
        addZvon(11, "/child::AAA/BBB", "<BBB/>"),

        addZvon(12, "/descendant::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>", "<DDD><CCC><DDD/><EEE/></CCC></DDD>",
            "<CCC><DDD/><EEE/></CCC>", "<DDD/>", "<EEE/>", "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>",
            "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>", "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>", "<FFF/>"),
        addZvon(12, "/AAA/BBB/descendant::*", "<DDD><CCC><DDD/><EEE/></CCC></DDD>",
            "<CCC><DDD/><EEE/></CCC>", "<DDD/>", "<EEE/>"),
        addZvon(12, "//CCC/descendant::*", "<DDD/>", "<EEE/>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>",
            "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>", "<FFF/>"),
        addZvon(12, "//CCC/descendant::DDD", "<DDD/>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>", "<DDD><FFF/></DDD>"),

        addZvon(13, "//DDD/parent::*", "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>",
            "<CCC><DDD/><EEE/></CCC>", "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>", "<EEE><DDD><FFF/></DDD></EEE>"),

        addZvon(14, "/AAA/BBB/DDD/CCC/EEE/ancestor::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>", "<DDD><CCC><DDD/><EEE/></CCC></DDD>", "<CCC><DDD/><EEE/></CCC>"),
        addZvon(14, "//FFF/ancestor::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>",
            "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>"),

        addZvon(15, "/AAA/BBB/following-sibling::*",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>", "<CCC><DDD/></CCC>"),
        addZvon(15, "//CCC/following-sibling::*", "<DDD/>", "<FFF/>", "<FFF><GGG/></FFF>"),

        addZvon(16, "/AAA/XXX/preceding-sibling::*", "<BBB><CCC/><DDD/></BBB>"),
        addZvon(16, "//CCC/preceding-sibling::*", "<BBB><CCC/><DDD/></BBB>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>", "<EEE/>", "<DDD/>"),

        addZvon(17, "/AAA/XXX/following::*", "<CCC><DDD/></CCC>", "<DDD/>"),
        addZvon(17, "//ZZZ/following::*", "<FFF><GGG/></FFF>", "<GGG/>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>",
            "<FFF><GGG/></FFF>", "<GGG/>", "<CCC><DDD/></CCC>", "<DDD/>"),


        // the preceding axis contains all nodes that are descendants of the root of the tree in which the context
        // node is found, are not ancestors of the context node, and occur before the context node in document order
        addZvon(18, "/AAA/XXX/preceding::*", "<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>", "<CCC/>",
            "<ZZZ><DDD/></ZZZ>", "<DDD/>"),
        addZvon(18, "//GGG/preceding::*", "<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>", "<CCC/>",
            "<ZZZ><DDD/></ZZZ>", "<DDD/>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>"),


        addZvon(19, "/AAA/XXX/descendant-or-self::*", "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>", "<FFF><GGG/></FFF>", "<GGG/>"),
        addZvon(19, "//CCC/descendant-or-self::*", "<CCC/>", "<CCC/>", "<CCC><DDD/></CCC>", "<DDD/>"),


        addZvon(20, "/AAA/XXX/DDD/EEE/ancestor-or-self::*",
            "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>"),
        addZvon(20, "//GGG/ancestor-or-self::*",
            "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<FFF><GGG/></FFF>", "<GGG/>"),

        addZvon(21, "//GGG/ancestor::*",
            "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>",
            "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>",
            "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>"),
        addZvon(21, "//GGG/descendant::*", "<JJJ><QQQ/></JJJ>", "<QQQ/>", "<JJJ/>"),
        addZvon(21, "//GGG/following::*", "<HHH/>", "<CCC><DDD/></CCC>", "<DDD/>"),
        addZvon(21, "//GGG/preceding::*", "<BBB><CCC/><ZZZ/></BBB>", "<CCC/>", "<ZZZ/>", "<EEE/>", "<HHH/>"),
        addZvon(21, "//GGG/self::*", "<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>"),
        addZvon(21, "//GGG/ancestor::* | //GGG/descendant::* | //GGG/following::* | //GGG/preceding::* | //GGG/self::*",
            "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<BBB><CCC/><ZZZ/></BBB>", "<CCC/>", "<ZZZ/>", "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>",
            "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>", "<EEE/>",
            "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>", "<HHH/>", "<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>",
            "<JJJ><QQQ/></JJJ>", "<QQQ/>", "<JJJ/>", "<HHH/>", "<CCC><DDD/></CCC>", "<DDD/>"),

        addZvon(22, "//BBB[position() mod 2 = 0 ]", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>"),
        addZvon(22, "//BBB[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]",
            "<BBB/>", "<BBB/>"),
        addZvon(22, "//CCC[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]",
            "<CCC/>")
        );
    }

    private static Arguments addZvon(int dataset, String xpath, String... expected) {
        return Arguments.of(dataset, xpath, expected);
    }

    /**
     * Verifies XPath impl using examples from
     * http://www.zvon.org/xxl/XPathTutorial/Output/example1.html
     * includes expanded notations as well
     */
    @ParameterizedTest(name = "{index}: zvon{0}.xml {1}")
    @MethodSource("dataZvon")
    void zvonExample(int dataset, String xpath, String[] expected) throws IOException, XmlException {
        try (XmlCursor x1 = jcur("xbean/xmlcursor/xpath/zvon"+dataset+".xml")) {
            x1.selectPath(xpath);

            XmlObject[] exp = new XmlObject[expected.length];
            for (int i = 0; i < expected.length; i++) {
                exp[i] = XmlObject.Factory.parse(expected[i]);
            }

            XPathCommon.compare(x1, exp);
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
