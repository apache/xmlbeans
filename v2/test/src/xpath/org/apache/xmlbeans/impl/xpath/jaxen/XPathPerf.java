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
import org.jaxen.JaxenException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.xpath.jaxen.XBeansXPath;

import java.util.List;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class XPathPerf
{
    public static void main(String[] args)
    {
        try
        {
            String xpathStr;
            File file;

            if (args.length!=2)
            {
                System.out.println("Usage: XBeansDemo file.xml xpath");
                return;
            }
            else
            {
                file = new File(args[0]);
                xpathStr = args[1];
            }

            //test1(xpathStr, file);
            //test2(xpathStr, file);

            test3t(xpathStr, file);
            test3(xpathStr, file);
            test4(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void test1(String xpathStr, File file)
    {
        System.out.println("\n ----- test1:   XBeansXPath.selectNodes(xpathStr) Navigator: XmlBookmarks + 1 XmlCursor embeded in Navigator -----");

        try
        {
            XmlObject doc = XmlObject.Factory.parse(file);
            XPath xpath = new XBeansXPath(xpathStr);
            XmlCursor xc = doc.newCursor();
            List results = xpath.selectNodes(xc);

            Iterator resultIter = results.iterator();

//            System.out.println("Document :: " + doc );
            System.out.println("   XPath :: " + xpath );
            System.out.println("");
            System.out.println("Results" );
            System.out.println("----------------------------------");

            while ( resultIter.hasNext() )
            {
                xc = (XmlCursor)resultIter.next();
                System.out.println( xc );
            }
            System.out.println("----------------------------------");
            System.out.println(results.size() );
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JaxenException e)
        {
            e.printStackTrace();
        }
    }

    private static void test2(String xpathStr, File file)
    {
        System.out.println("\n ----- test2:   XBeansXPath.selectNodes(xpathStr) Navigator: XmlBookmarks + 1 XmlCursor embeded in Navigator -----");

        try
        {
            XmlObject doc = XmlObject.Factory.parse(file);
            XPath xpath = new XBeansXPath(xpathStr);
            XmlCursor docXC = doc.newCursor();

            long start = System.currentTimeMillis();
            int count = 0;

            for (int j = 0; j < 10; j++)
            {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    XmlCursor speaker = (XmlCursor)xpath.selectSingleNode(docXC);
                    count += (speaker == null ? 0 : 1);
                }
                //System.out.println((j*100) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JaxenException e)
        {
            e.printStackTrace();
        }
    }

	private static void test3(String xpathStr, File file)
    {
        System.out.println("\n ----- test3:   XmlCursor.selectPath(cpath)  -----");

        try
        {
            String cpath = XmlBeans.compilePath( xpathStr );
            XmlObject doc = XmlObject.Factory.parse(file);
            XmlCursor speaker = doc.newCursor();

            long start = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 10; j++) {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    speaker.toStartDoc();
                    speaker.selectPath(cpath);
                    //speaker.getSelectionCount();
                    while ( speaker.toNextSelection() ) ;

                    count += (speaker == null ? 0 : 1);
                }
                System.out.println((j*100) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void test3t(String xpathStr, File file)
    {
        System.out.println("\n ----- test3:   XmlCursor.selectPath(cpath)  -----");

        try
        {
            String cpath = XmlBeans.compilePath( xpathStr );
            XmlObject doc = XmlObject.Factory.parse(file);
            XmlCursor speaker = doc.newCursor();

            int count = 0;
            speaker.toStartDoc();
            speaker.selectPath(cpath);
            //speaker.getSelectionCount();
            while ( speaker.toNextSelection() )
            {
                System.out.println(speaker);
                count += (speaker == null ? 0 : 1);
            }

            System.out.println(">>> " + count + " selections");
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void test4(File file)
    {
        System.out.println("\n ----- test4:   1 XmlCursor handCoded  -----");

        try
        {
            XmlObject doc = XmlObject.Factory.parse(file);
            XmlCursor xc = doc.newCursor();

            long start = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 10; j++) {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    xc.toStartDoc();

                    rec(new String[] {"PLAY","ACT","SCENE","SPEECH","SPEAKER"}, 0, xc);

                    count += (xc == null ? 0 : 1);
                }
                //System.out.println((j*100) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
        }
        catch (XmlException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void rec(String[] xp, int i, XmlCursor xc )
    {
        if (i>=xp.length)
        {
            //System.out.println(xc);
            return;
        }

        if (xc.toChild(xp[i])) do
        {
            rec(xp, i+1, xc);
        }
        while(xc.toNextSibling(xp[i]));
        xc.toParent();
    }
}
