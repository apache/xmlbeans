package org.apache.xmlbeans.impl.xpath.jaxen;

import org.jaxen.XPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.xpath.jaxen.XBeansXPath;

import java.util.List;
import java.util.Iterator;
import java.io.File;

/**
 * Author: Cezar Andrei (cezar.andrei@bea.com)
 * Date: Oct 10, 2003
 */
public class XBeansDemo
{
    public static void main(String[] args)
    {
        try
        {

            XmlObject doc;
            String xpathStr;

            if (args.length!=2)
            {
                doc = XmlObject.Factory.parse("<a><b>lala</b><b>second 'b'</b>some text<c/></a>");

                xpathStr = "/a/b";
            }
            else
            {
                doc = XmlObject.Factory.parse(new File(args[0]));
                xpathStr = args[1];
            }
            XPath xpath= new XBeansXPath(xpathStr);

            test1(xpath, doc);
            test2(xpath, doc);
            test3(xpathStr, doc);
            test4(doc);
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

    private static void test1(XPath xpath, XmlObject doc)
        throws JaxenException
    {
            System.out.println("\n ----- test1:   XBeansXPath.selectNodes(xpathStr)  -----");

            List results = xpath.selectNodes( doc.newCursor() );

            Iterator resultIter = results.iterator();

//            System.out.println("Document :: " + doc );
            System.out.println("   XPath :: " + xpath );
            System.out.println("");
            System.out.println("Results" );
            System.out.println("----------------------------------");

            while ( resultIter.hasNext() )
            {
                Object object = resultIter.next();
                System.out.println( object );
            }
            System.out.println("----------------------------------");
            System.out.println(results.size() );
    }

    private static void test2(XPath xpath, XmlObject doc)
            throws JaxenException
    {
            System.out.println("\n ----- test2:   XBeansXPath.selectNodes(xpathStr)  -----");

            XmlCursor docXC = doc.newCursor();

            long start = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 10; j++) {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 10; i++)
                {
                    docXC.toStartDoc();
                    XmlCursor speaker = (XmlCursor) xpath.selectSingleNode(docXC);
                    count += (speaker == null ? 0 : 1);
                    //System.out.println(((Cursor)speaker).crs );
                    //((Cursor)speaker).crs = 0;
                }
                System.out.println((j*10) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
    }

    private static void test3(String xpathStr, XmlObject doc)
            throws XmlException
    {
            System.out.println("\n ----- test3:   XmlCursor.selectPath(cpath)  -----");

            String cpath = XmlBeans.compilePath( xpathStr );
            XmlCursor speaker = doc.newCursor();

            long start = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 10; j++) {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 10; i++)
                {
                    speaker.toStartDoc();
                    speaker.selectPath(cpath);
                    while ( speaker.toNextSelection() ) ;

                    count += (speaker == null ? 0 : 1);
                    //System.out.println(((Cursor)speaker).crs );
                    //((Cursor)speaker).crs = 0;
                }
                System.out.println((j*10) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
    }

    private static void test4(XmlObject doc)
    {
            System.out.println("\n ----- test4:   XmlCursor.selectPath(cpath)  -----");

            XmlCursor xc = doc.newCursor();

            long start = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 10; j++) {
                long start2 = System.currentTimeMillis();
                for (int i = 0; i < 10; i++)
                {
                    xc.toStartDoc();

                    rec(new String[] {"PLAY","ACT","SCENE","SPEECH","SPEAKER"}, 0, xc);

                    count += (xc == null ? 0 : 1);
                    //System.out.println(((Cursor)speaker).crs );
                    //((Cursor)speaker).crs = 0;
                }
                System.out.println((j*10) + "                \t" + (System.currentTimeMillis()-start2));
            }

            long end = System.currentTimeMillis();
            System.out.println(">>> " + count + " selections in " + (end - start) + " ms");
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
