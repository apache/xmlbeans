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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class JaxenXPathTests extends TestCase
{
    public JaxenXPathTests(String name) { super(name); }
    public static Test suite()
    {
        try
        {
            Class.forName("org.jaxen.XPath");
            return new TestSuite(JaxenXPathTests.class);
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("\n\nWARNING!!!\n\nJaxen.jar not on classpath skipping JaxenXPathTests.\n\n");
            return new TestSuite();
        }
    }

    public void testConformance() throws Exception
    {
        try
        {
            XmlObject doc = XmlObject.Factory.parse(TestEnv.xbeanCase("xpath/testXPath.xml"));

            String[] xpath = new String[25];
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

            runAll(doc, xpath);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void runAll(XmlObject doc, String[] xpathes)
    {
        for (int i=0; i<xpathes.length; i++)
        {
            runXpath2(doc, xpathes[i], i);
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

    private static void runXpath2(XmlObject doc, String xpathStr, int i)
    {
        XmlCursor xc = doc.newCursor();
		xc.selectPath( xpathStr );

        int j = 0;
        while ( xc.toNextSelection() )
        {
            // generateExpected(i, j, xc.toString());
            check(i, j, xc);
            j++;
        }

		xc.dispose();
    }

    private static void check(int expresionNumber, int resultNumber, XmlCursor actual)
    {
        try
        {
            XmlCursor expected = XmlObject.Factory.parse(TestEnv.xbeanCase("xpath/expected/JaxenXPathTest_" + expresionNumber +
                "_" + resultNumber + ".xml")).newCursor();

            XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
            boolean match = XmlComparator.lenientlyCompareTwoXmlStrings(actual.toString(), expected.toString(), diag);

            Assert.assertTrue("------------  Found difference:" + expresionNumber + " " + resultNumber +
                " actual=\n'" + actual + "'\nexpected=\n'" + expected + "'\ndiagnostic=" + diag , match);
        }
        catch (XmlException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is only used to regen the expected files.
     */
    private static void generateExpected(int expresionNumber, int resultNumber, String content)
    {
        try
        {
            FileWriter fw = new FileWriter(TestEnv.xbeanCase("xpath/expected/JaxenXPathTest_" + expresionNumber +
                "_" + resultNumber + ".xml"));

            fw.write(content);
            fw.close();
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
