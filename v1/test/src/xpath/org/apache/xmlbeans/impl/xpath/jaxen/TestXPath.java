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

package org.apache.xmlbeans.impl.xpath.jaxen;

import org.jaxen.XPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

import java.util.List;
import java.util.Iterator;
import java.io.File;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class TestXPath
{
    public static void main(String[] args)
    {
        try
        {

            XmlObject doc;

            if (args.length!=1)
            {
                System.out.println("TestXPath test/cases/xpath/testXPath.xml");
                return;
            }
            else
            {
                doc = XmlObject.Factory.parse(new File(args[0]));
            }

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
            xpath[14] = "//*/@*";         //  "//*[current()]/@*";
            xpath[15] = "//*[last()]";
            xpath[16] = "doc/*[last()]";
            xpath[17] = "/doc/a/*/@*"; // "/doc/a/*[current()]/@*";
            xpath[18] = "doc/descendant::node()";
            xpath[19] = "doc/a/@*";
            xpath[20] = "doc/b/a/ancestor-or-self::*";
            xpath[21] = "doc/b/a/preceding::*";
            xpath[22] = "doc/a/following::*";
            xpath[23] = "/doc/b/preceding-sibling::*";
            xpath[24] = "/doc/a/following-sibling::*";

            test1(doc, xpath);
        }
        catch (XPathSyntaxException e)
        {
            System.err.println( e.getMultilineMessage() );
        }
        catch (JaxenException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void test1(XmlObject doc, String[] xpathes) throws JaxenException
    {
        for (int i=0; i<xpathes.length; i++)
        {
            runXpath2(doc, xpathes[i], i);
        }
    }

    private static void runXpath1(XmlObject doc, String xpathStr, int i)
        throws JaxenException
    {
        System.out.println("\n>>>================= " + i + " " + xpathStr + " ========================<<<");

        XPath xpath = new XBeansXPath(xpathStr);
        XmlCursor xc = doc.newCursor();
		List results = xpath.selectNodes( xc );

        Iterator resultIter = results.iterator();

        //System.out.println("Document :: \n" + doc );
        int j = 0;
        while ( resultIter.hasNext() )
        {
            xc = (XmlCursor)resultIter.next();
            System.out.println("> " + (j++) + " >--------------------------------------< " + xc.currentTokenType());
            System.out.println( xc );
        }
        System.out.println("\n>>>Results: " + j + "    " + xpathStr + " ==========================<<<");
		xc.dispose();
    }

    private static void runXpath2(XmlObject doc, String xpathStr, int i)
    {
        System.out.println("\n>>>================= " + i + " " + xpathStr + " ========================<<<");

        XmlCursor xc = doc.newCursor();
		xc.selectPath(xpathStr);

        //System.out.println("Document :: \n" + doc );
        int j = 0;
        while ( xc.toNextSelection() )
        {
            System.out.println("> " + (j++) + " >--------------------------------------< " + xc.currentTokenType());
            System.out.println( xc );
        }
        System.out.println("\n>>>Results: " + j + "    " + xpathStr + " ==========================<<<");
		xc.dispose();
    }
}
