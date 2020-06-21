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

package xmlcursor.xpath.complex.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.xpath.common.XPathCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies XPath impl using examples from
 * http://www.zvon.org/xxl/XPathTutorial/Output/example1.html
 * includes expanded notations as well
 *
 */
@RunWith(Parameterized.class)
public class XPathTest extends BasicCursorTestCase {

    @Parameter
    public int dataset;
    @Parameter(value = 1)
    public String xpath;
    @Parameter(value = 2)
    public String[] expected = null;

    @Parameterized.Parameters(name = "{index}: zvon{0}.xml {1}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        add(data, 1, "/AAA", "<AAA><BBB/><CCC/><BBB/><BBB/><DDD><BBB/></DDD><CCC/></AAA>");
        add(data, 1, "/AAA/CCC", "<CCC/>", "<CCC/>");
        add(data, 1, "/AAA/DDD/BBB", "<BBB/>");

        add(data, 2, "//BBB", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>");
        add(data, 2, "//DDD/BBB", "<BBB/>", "<BBB/>", "<BBB/>");

        add(data, 3, "/AAA/CCC/DDD/*", "<BBB/>", "<BBB/>", "<EEE/>", "<FFF/>");
        add(data, 3, "/*/*/*/BBB", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB><BBB/></BBB>");

        //according to Galax the document order is :
        add(data, 3, "//*",
            "<AAA><XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX><CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC><CCC><BBB><BBB><BBB/></BBB></BBB></CCC></AAA>",
            "<XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX>", "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>", "<BBB/>", "<BBB/>",
            "<EEE/>", "<FFF/>", "<CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC>", "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>",
            "<BBB/>", "<BBB/>", "<EEE/>", "<FFF/>", "<CCC><BBB><BBB><BBB/></BBB></BBB></CCC>",
            "<BBB><BBB><BBB/></BBB></BBB>", "<BBB><BBB/></BBB>", "<BBB/>");

        add(data, 4, "/AAA/BBB[1]", "<BBB/>");
        add(data, 4, "/AAA/BBB[last()]", "<BBB/>");

        add(data, 5, "//@id", "<xml-fragment id=\"b1\"/>", "<xml-fragment id=\"b2\"/>");
        add(data, 5, "//BBB[@id]", "<BBB id = \"b1\"/>", "<BBB id = \"b2\"/>");
        add(data, 5, "//BBB[@name]", "<BBB name=\"bbb\"/>");
        add(data, 5, "//BBB[@*]", "<BBB id = \"b1\"/>", "<BBB id = \"b2\"/>", "<BBB name=\"bbb\"/>");
        add(data, 5, "//BBB[not(@*)]", "<BBB/>");

        add(data, 6, "//BBB[@id='b1']", "<BBB id = \"b1\"/>");
        add(data, 6, "//BBB[@name='bbb']", "<BBB name=\"bbb\"/>");
        add(data, 6, "//BBB[normalize-space(@name)='bbb']", "<BBB name=\" bbb \"/>", "<BBB name=\"bbb\"/>");

        add(data, 7, "//*[count(BBB)=2]", "<DDD><BBB/><BBB/></DDD>");
        add(data, 7, "//*[count(*)=2]", "<DDD><BBB/><BBB/></DDD>", "<EEE><CCC/><DDD/></EEE>");
        add(data, 7, "//*[count(*)=3]",
            "<AAA><CCC><BBB/><BBB/><BBB/></CCC><DDD><BBB/><BBB/></DDD><EEE><CCC/><DDD/></EEE></AAA>", "<CCC><BBB/><BBB/><BBB/></CCC>");

        add(data, 8, "//*[name()='BBB']", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>");
        add(data, 8, "//*[starts-with(name(),'B')]", "<BCC><BBB/><BBB/><BBB/></BCC>",
            "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>", "<BEC><CCC/><DBD/></BEC>");

        // ykadiysk: Jaxen prints in BF left-to-right order but XPath wants doc order
        // add(data, "zvon8.xml", "//*[starts-with(name(),'B')]", "<BCC><BBB/><BBB/><BBB/></BCC>",
        //    "<BBB/>", "<BBB/>", "<BEC><CCC/><DBD/></BEC>", "<BBB/>", "<BBB/>", "<BBB/>");

        add(data, 8, "//*[contains(name(),'C')]", "<BCC><BBB/><BBB/><BBB/></BCC>", "<BEC><CCC/><DBD/></BEC>", "<CCC/>");

        add(data, 9, "//*[string-length(name()) = 3]", "<AAA><Q/><SSSS/><BB/><CCC/><DDDDDDDD/><EEEE/></AAA>", "<CCC/>");
        add(data, 9, "//*[string-length(name()) < 3]", "<Q/>", "<BB/>");
        add(data, 9, "//*[string-length(name()) > 3]", "<SSSS/>", "<DDDDDDDD/>", "<EEEE/>");

        add(data, 10, "$this//CCC | $this//BBB", "<BBB/>", "<CCC/>", "<CCC/>");
        // Nodes are returned in document order
        add(data, 10, "$this/AAA/EEE | $this//BBB", "<BBB/>", "<EEE/>");
        add(data, 10, "./AAA/EEE |.//DDD/CCC | ./AAA | .//BBB", "<AAA><BBB/><CCC/><DDD><CCC/></DDD><EEE/></AAA>",
            "<BBB/>", "<CCC/>", "<EEE/>");

        add(data, 11, "/AAA", "<AAA><BBB/><CCC/></AAA>");
        add(data, 11, "/child::AAA", "<AAA><BBB/><CCC/></AAA>");
        add(data, 11, "/AAA/BBB", "<BBB/>");
        add(data, 11, "/child::AAA/child::BBB", "<BBB/>");
        add(data, 11, "/child::AAA/BBB", "<BBB/>");

        add(data, 12, "/descendant::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>", "<DDD><CCC><DDD/><EEE/></CCC></DDD>",
            "<CCC><DDD/><EEE/></CCC>", "<DDD/>", "<EEE/>", "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>",
            "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>", "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>", "<FFF/>");
        add(data, 12, "/AAA/BBB/descendant::*", "<DDD><CCC><DDD/><EEE/></CCC></DDD>",
            "<CCC><DDD/><EEE/></CCC>", "<DDD/>", "<EEE/>");
        add(data, 12, "//CCC/descendant::*", "<DDD/>", "<EEE/>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>",
            "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>", "<FFF/>");
        add(data, 12, "//CCC/descendant::DDD", "<DDD/>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>", "<DDD><FFF/></DDD>");

        add(data, 13, "//DDD/parent::*", "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>",
            "<CCC><DDD/><EEE/></CCC>", "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>", "<EEE><DDD><FFF/></DDD></EEE>");

        add(data, 14, "/AAA/BBB/DDD/CCC/EEE/ancestor::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>", "<DDD><CCC><DDD/><EEE/></CCC></DDD>", "<CCC><DDD/><EEE/></CCC>");
        add(data, 14, "//FFF/ancestor::*",
            "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>",
            "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>", "<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>",
            "<EEE><DDD><FFF/></DDD></EEE>", "<DDD><FFF/></DDD>");

        add(data, 15, "/AAA/BBB/following-sibling::*",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>", "<CCC><DDD/></CCC>");
        add(data, 15, "//CCC/following-sibling::*", "<DDD/>", "<FFF/>", "<FFF><GGG/></FFF>");

        add(data, 16, "/AAA/XXX/preceding-sibling::*", "<BBB><CCC/><DDD/></BBB>");
        add(data, 16, "//CCC/preceding-sibling::*", "<BBB><CCC/><DDD/></BBB>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>", "<EEE/>", "<DDD/>");

        add(data, 17, "/AAA/XXX/following::*", "<CCC><DDD/></CCC>", "<DDD/>");
        add(data, 17, "//ZZZ/following::*", "<FFF><GGG/></FFF>", "<GGG/>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>",
            "<FFF><GGG/></FFF>", "<GGG/>", "<CCC><DDD/></CCC>", "<DDD/>");


        // the preceding axis contains all nodes that are descendants of the root of the tree in which the context
        // node is found, are not ancestors of the context node, and occur before the context node in document order
        add(data, 18, "/AAA/XXX/preceding::*", "<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>", "<CCC/>",
            "<ZZZ><DDD/></ZZZ>", "<DDD/>");
        add(data, 18, "//GGG/preceding::*", "<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>", "<CCC/>",
            "<ZZZ><DDD/></ZZZ>", "<DDD/>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>");


        add(data, 19, "/AAA/XXX/descendant-or-self::*", "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>", "<DDD/>", "<CCC/>", "<FFF/>", "<FFF><GGG/></FFF>", "<GGG/>");
        add(data, 19, "//CCC/descendant-or-self::*", "<CCC/>", "<CCC/>", "<CCC><DDD/></CCC>", "<DDD/>");


        add(data, 20, "/AAA/XXX/DDD/EEE/ancestor-or-self::*",
            "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<EEE/>");
        add(data, 20, "//GGG/ancestor-or-self::*",
            "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>",
            "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>", "<FFF><GGG/></FFF>", "<GGG/>");

        add(data, 21, "//GGG/ancestor::*",
            "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>",
            "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>",
            "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>");
        add(data, 21, "//GGG/descendant::*", "<JJJ><QQQ/></JJJ>", "<QQQ/>", "<JJJ/>");
        add(data, 21, "//GGG/following::*", "<HHH/>", "<CCC><DDD/></CCC>", "<DDD/>");
        add(data, 21, "//GGG/preceding::*", "<BBB><CCC/><ZZZ/></BBB>", "<CCC/>", "<ZZZ/>", "<EEE/>", "<HHH/>");
        add(data, 21, "//GGG/self::*", "<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>");
        add(data, 21, "//GGG/ancestor::* | //GGG/descendant::* | //GGG/following::* | //GGG/preceding::* | //GGG/self::*",
            "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>",
            "<BBB><CCC/><ZZZ/></BBB>", "<CCC/>", "<ZZZ/>", "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>",
            "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>", "<EEE/>",
            "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>", "<HHH/>", "<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>",
            "<JJJ><QQQ/></JJJ>", "<QQQ/>", "<JJJ/>", "<HHH/>", "<CCC><DDD/></CCC>", "<DDD/>");

        add(data, 22, "//BBB[position() mod 2 = 0 ]", "<BBB/>", "<BBB/>", "<BBB/>", "<BBB/>");
        add(data, 22, "//BBB[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]",
            "<BBB/>", "<BBB/>");
        add(data, 22, "//CCC[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]",
            "<CCC/>");

        return data;
    }

    private static void add(List<Object[]> data, int dataset, String xpath, String... expected) {
        data.add(new Object[]{dataset, xpath, expected});
    }


    @Test
    public void zvonExample() throws IOException, XmlException {
        XmlObject xDoc = XmlObject.Factory.parse(JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon"+dataset+".xml"));
        XmlCursor x1 = xDoc.newCursor();
        try {
            x1.selectPath(xpath);

            XmlObject[] exp = new XmlObject[expected.length];
            for (int i = 0; i < expected.length; i++) {
                exp[i] = XmlObject.Factory.parse(expected[i]);
            }

            XPathCommon.compare(x1, exp);
        } finally {
            x1.dispose();
        }
    }
}
