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

//            test3t(xpathStr, file);
//            test3(xpathStr, file);
//            test4(file);

            test5();
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

    private static void test5()
    {
        System.out.println("\n ----- test5:   XBeansXPath.selectNodes(xpathStr) Navigator: XmlBookmarks + 1 XmlCursor embeded in Navigator -----");

        //String xmlDocStr = "<AAA>  <BBB id = \"b1\"/> <BBB name = \" bbb \"/> <BBB name = \"bbb\"/>  </AAA>";
        //String xpathStr = "//BBB[normalize-space(@name)='bbb']";

        String xmlDocStr =
          "<AAA>\n" +
          "  <BCC> \n" +
          "     <BBB/>\n" +
          "     <BBB/> \n" +
          "     <BBB/> \n" +
          "  </BCC> \n" +
          "  <DDB> \n" +
          "     <BBB/>\n" +
          "     <BBB/> \n" +
          "  </DDB> \n" +
          "  <BEC> \n" +
          "     <CCC/>\n" +
          "     <DBD/> \n" +
          "  </BEC> \n" +
          "</AAA> ";
        String xpathStr = "//*[contains(name(),'C')]";

        try
        {
            XmlObject doc = XmlObject.Factory.parse(xmlDocStr);
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
        catch (JaxenException e)
        {
            e.printStackTrace();
        }
    }
}
