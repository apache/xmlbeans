/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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
