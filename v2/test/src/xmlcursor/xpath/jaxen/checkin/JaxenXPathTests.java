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
package xmlcursor.xpath.jaxen.checkin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import xmlcursor.common.Common;
import xmlcursor.xpath.common.XPathCommon;

import java.io.IOException;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class JaxenXPathTests extends TestCase {
    String sXml =
            "<?xml version=\"1.0\"?>\n" +
            "<doc xmlns:ext=\"http://somebody.elses.extension\">\n" +
            "  <a test=\"test\" />\n" +
            "  <b attr1=\"a1\" attr2=\"a2\"   \n" +
            "  xmlns:java=\"http://xml.apache.org/xslt/java\">\n" +
            "    <a>\n" +
            "    </a> \n" +
            "  </b>\n" +
            "</doc><!-- -->         ";


    public JaxenXPathTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JaxenXPathTests.class);
    }

    public void testConformance() throws Exception {
// EricVas: I'm not implementing Jaxen in V2's newstore ... need to find a replacement for Jaxen
    }

    private void runAll(XmlObject doc, String[] xpathes)
            throws Exception {

        for (int i = 0; i < xpathes.length; i++) {
            try {
                runXpath2(doc, xpathes[i], i);

            }
            catch (Exception e) {
                System.err.println("**********************Failed at test " + i);
                throw new RuntimeException(e);
            }
        }
    }

//    private static void runXpath(XmlObject doc, String xpathStr, int i)
//    {
//        try
//        {
//            XmlCursor xc = doc.newCursor();
//            XPath xpath = new XBeansXPath(xpathStr);
//            List results = xpath.selectNodes( xc );
//
//            Iterator resultIter = results.iterator();
//
//            int j = 0;
//            while ( resultIter.hasNext() )
//            {
//                xc = (XmlCursor)resultIter.next();  //it's the same object as previous xc
//                // generateExpected(i, j, xc.toString());
//                check(i, j, xc);
//                j++;
//            }
//
//            xc.dispose();
//        }
//        catch (XPathSyntaxException e)
//        {
//            System.err.println( e.getMultilineMessage() );
//            throw new RuntimeException(e);
//        }
//        catch (JaxenException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }

    private void runXpath2(XmlObject doc, String xpathStr, int i)
            throws Exception {
        XmlCursor xc = doc.newCursor();
        xc.selectPath(xpathStr);


        check(i, xc);

        xc.dispose();
    }

    private void check(int expresionNumber,
                       XmlCursor actual) throws Exception {

       if (actual.getSelectionCount() == 0){
               assertEquals(null, expected[expresionNumber] );
               return;
       }

        int numCases = expected[expresionNumber].length;
        XmlObject[] expected_val = new XmlObject[numCases];


        for (int i = 0; i < numCases; i++)
            expected_val[i] = XmlObject.Factory.parse(
                    expected[expresionNumber][i]);

       try{
        XPathCommon.compare(actual, expected_val);
       }catch (Throwable e){
           throw new Exception("\n****Failed at expression "+expresionNumber+
                   e.getMessage()
                   );
       }

    }

    /**
     * This is only used to regen the expected files.
     */
    private void generateExpected(int expresionNumber, int resultNumber,
                                  String content) {
        expected[expresionNumber][resultNumber] = content;
    }

    public void setUp() {
        expected = new String[numExpressions][];


        xpath = new String[numExpressions];
        xpath[0] = "/doc/a/@test";
        xpath[1] = "//.";
        xpath[2] = "/doc";
        xpath[3] = "/doc/a";
        xpath[4] = "//@*";
        xpath[5] = ".";
        xpath[6] = "//ancestor-or-self::*";
        xpath[7] = "./child::*[1]";
        xpath[8] = "//descendant-or-self::*/@*[1]";
        xpath[9] = "//@* | * | node()";
        xpath[10] = "//*";
        xpath[11] = "/doc/namespace::*";
        xpath[12] = "//descendant::comment()";
        xpath[13] = "//*[local-name()='a']";
        xpath[14] = "//*/@*";
        xpath[15] = "//*[last()]";
        xpath[16] = "doc/*[last()]";
        xpath[17] = "/doc/a/*/@*";
        xpath[18] = "doc/descendant::node()";
        xpath[19] = "doc/a/@*";
        xpath[20] = "doc/b/a/ancestor-or-self::*";
        xpath[21] = "doc/b/a/preceding::*";
        xpath[22] = "doc/a/following::*";
        xpath[23] = "/doc/b/preceding-sibling::*";
        xpath[24] = "/doc/a/following-sibling::*";


        String[] steps = new String[10];
        steps[0] =
                "<xml-fragment xmlns:ext=\"http://somebody.elses.extension\"/>";
        steps[1] = "<doc xmlns:ext=\"http://somebody.elses.extension\">" +
                "<a test=\"test\" />" +
                "<b attr1=\"a1\" attr2=\"a2\" " +
                "xmlns:java=\"http://xml.apache.org/xslt/java\">" +
                " <a /> </b></doc>";
        steps[2] =
                "<a test=\"test\" xmlns:ext=\"http://somebody.elses.extension\"/>";
        steps[3] =
                "<xml-fragment test=\"test\" " +
                "xmlns:ext=\"http://somebody.elses.extension\" /> ";
        steps[4] =
                "<a xmlns:java=\"http://xml.apache.org/xslt/java\" " +
                "xmlns:ext=\"http://somebody.elses.extension\" />";
        steps[5] =
                "<b attr1=\"a1\" attr2=\"a2\" " +
                "xmlns:java=\"http://xml.apache.org/xslt/java\">" +
                " <a /> </b>";
        steps[6] =
                "<xml-fragment attr1=\"a1\" " +
                "xmlns:java=\"http://xml.apache.org/xslt/java\" " +
                "xmlns:ext=\"http://somebody.elses.extension\" />";
        steps[7] =
                "<xml-fragment attr2=\"a2\" " +
                "xmlns:java=\"http://xml.apache.org/xslt/java\" " +
                "xmlns:ext=\"http://somebody.elses.extension\" />";
        steps[8] = "<xml-fragment><!-- --></xml-fragment>";
        steps[9] = " <xml-fragment xmlns:java=\"http://xml.apache.org/xslt/java\"" +
                " xmlns:ext=\"http://somebody.elses.extension\" />";


        expected[0] = new String[]{steps[2]};
        expected[1] = new String[]{sXml,
                                   steps[1],
                                   steps[8],
                                   XMLFRAG_EMPTY,
                                   steps[2],
                                   XMLFRAG_EMPTY,
                                   steps[5],
                                   XMLFRAG_EMPTY,
                                   steps[0],
                                   steps[4],
                                   steps[0],
                                   steps[9]
        };

        expected[2] = new String[]{steps[1]};
        expected[3] = new String[]{steps[2]};

        expected[4] = new String[]{
            steps[3],
            steps[6],
            steps[7]};

        expected[5] = new String[]{sXml};
        expected[6] = new String[]{sXml,
                                   steps[1],
                                   steps[2],
                                   steps[5],
                                   steps[4]};
        expected[7] = new String[]{steps[1]};
        expected[8] =
                new String[]{
                    steps[2],
                    steps[6]};
        expected[9] = new String[]{
            steps[3],
            steps[6],
            steps[7],
            steps[1],
            steps[8]};

        expected[10] = new String[]{
            steps[1],
            steps[2],
            steps[5],
            steps[4]
        };
        expected[11] = new String[]{steps[0]};
        expected[12] = new String[]{steps[8]};
        expected[13] = new String[]{steps[2],
                                    steps[4]
        };
        expected[14] = new String[]{steps[3],
                                    steps[6],
                                    steps[7]};

        expected[15] = new String[]{steps[1],
                                    steps[5],
                                    steps[4]};
        expected[16] = new String[]{steps[5]};
        expected[18] = new String[]{
            XMLFRAG_EMPTY,
            steps[2],
            XMLFRAG_EMPTY,
            steps[5],
            XMLFRAG_EMPTY,
            steps[0],
            steps[4],
            steps[0],
            steps[9]
        };
        expected[19] = new String[]{steps[2]};
        expected[20] = new String[]{
            steps[4],
            steps[5],
            steps[1],
            sXml
        };
        expected[21] = new String[]{
            steps[4],
            steps[5],
            steps[2]

        };
        expected[22] = new String[]{
            steps[5],
            steps[4]};

        expected[23] = new String[]{
            steps[2]};

        expected[24] = new String[]{
            steps[5]};

    }

    private String[][] expected = null;
    private String[] xpath = null;
    private int numExpressions = 25;
    private String XMLFRAG_EMPTY = "<xml-fragment/>";
}
