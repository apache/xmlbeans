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

package streamtest;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Date;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.mxp1.MXParserCachingStrings;
import weblogic.xml.stream.XMLInputStream;
import weblogic.xml.stream.XMLInputStreamFactory;
import weblogic.xml.stream.XMLEvent;
import xmlext.xml.XppAdaptor;
import org.xmlpull.stream.XppXmlStreamImpl;
import org.xmlpull.stream.XmlStreamFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 *   Tests the conformance of the XMLInputStream produced by XppXmlStreamImpl.
 *   @author Cezar Andrei
 */
public class XmlStreamConformance
{
    final static int TIMES = 30;

    public static void main(String[] args)
        throws Exception
    {
        if( args.length>0 )
        {
            File f = new File(args[0]);
            if( f.isDirectory() )
                compareDir(f);
            else
                //compareFile(f);
                printFile(f);
        }
        else
        {
            printUsage();
        }
    }

    static void printUsage()
    {
        System.out.println("\tThis app tests the conformance of the two " +
                           "XMLInputStream \n\tproduced by Babel parser and " +
                           "Xpp3 parser.");
        System.out.println("\n\n\tUSAGE: java ... test.XmlStreamConformance " +
                           "dir|file");
        System.out.println("\n\t\tdir\t\tMakes the test for all the files in " +
                           "the dir.");
        System.out.println("\t\tfile\t\tMakes the test only for that file.");
    }

    static void compareFile(File file)
        throws Exception
    {
        System.out.println(file.getPath() + " size: " + file.length());

        if(!file.isFile())
            //throw new Exception("compareFile: file not a File");
            return;

        Reader r1 = new BufferedReader( new FileReader(file));
        Reader r2 = new BufferedReader( new FileReader(file));

        XMLInputStream xpp = XmlStreamFactory.
                createXmlInputStream(new XppXmlStreamImpl(r2));
        XMLInputStream babel = XMLInputStreamFactory.newInstance().
            newInputStream(r1);

        while( xpp.hasNext() || babel.hasNext() )
        {
            XMLEvent xppevent = xpp.peek();
            XMLEvent babelevent = babel.peek();

            if( !(""+xppevent).equals((""+babelevent)) )
                printError(xppevent, babelevent);
            else
            {
                //System.out.println("   `" + xppevent + "'\n   `" + babelevent +
                //                   "'"); System.out.flush();
            }

            xppevent = xpp.next();
            babelevent = babel.next();
        }

        //System.out.println("   xpp   overlimit: " + xpp.next() );
        //System.out.println("   babel overlimit: " + babel.next() );

        r1.close();
        r2.close();

        //comparePerf(file);
    }

    static void printFile(File file)
        throws Exception
    {
        System.out.println(file.getPath() + " size: " + file.length());

        if(!file.isFile())
            //throw new Exception("compareFile: file not a File");
            return;

        Reader r2 = new BufferedReader( new FileReader(file));

        XMLInputStream xpp = XmlStreamFactory.
                createXmlInputStream(new XppXmlStreamImpl(r2));
        XMLInputStream morphedStream;
        try
        {
            System.out.println("Starting xquery");
            XmlObject xmlObj = XmlObject.Factory.parse(xpp);
            XmlCursor crs = xmlObj.newCursor();
            XmlCursor resCrs = crs.execQuery("$this");
            // not yet implemented
            //morphedStream = resCrs.newXMLInputStream();
            Reader reader = resCrs.newReader();
            reader = resCrs.newReader();
            morphedStream = XMLInputStreamFactory.newInstance().
                newInputStream(reader);
            System.out.print("...");

        }
        catch( Exception e )
        {
            e.printStackTrace();
            morphedStream = null;
        }

        xpp = morphedStream;

        while( xpp.hasNext() )
        {
            XMLEvent xppevent = xpp.peek();

            System.out.println("   `" + xppevent + "'"); System.out.flush();
            xppevent = xpp.next();
        }

        r2.close();
    }

    static void printError(XMLEvent xppevent, XMLEvent babelevent)
    {
        String sxpp = ""+xppevent;
        String sbab = ""+babelevent;

        System.out.println("ERROR-----");
        if( !sxpp.trim().equals(sbab.trim()) )
        {
            System.out.println("xpp   :`" + sxpp + "'");
            System.out.print("babel :`" + sbab + "'");
        }
        else
        {
            System.out.print("xpp   :");
            for(int i=0; i<sxpp.length(); i++)
            {
                byte b = (byte)sxpp.charAt(i);
                System.out.print(" #" + b);
            }
            System.out.print("\nbabel :");
            for(int i=0; i<sbab.length(); i++)
            {
                byte b = (byte)sbab.charAt(i);
                System.out.print(" #" + b);
            }
        }
        System.out.println("\n----------");
    }

    static void compareDir(File dir)
        throws Exception
    {
        if(!dir.isDirectory())
            throw new Exception("compareDir: dir not a Directory");

        File[] files = dir.listFiles();
        for( int i=0; i<files.length; i++)
            compareFile(files[i]);
    }

    static void comparePerf(File file)
        throws Exception
    {
        long start;
        double xstime = 0;
        double xrtime = 0;
        double babeltime = 0;

        //babel
        for( int i=0; i<TIMES; i++ )
        {
            Reader r = new BufferedReader( new FileReader(file));
            start = new Date().getTime();
            XMLInputStream babel = XMLInputStreamFactory.newInstance().
                newInputStream(r);
            while( babel.hasNext() )
            {
                XMLEvent babelevent = babel.peek();
                babelevent = babel.next();
            }
            babeltime += new Date().getTime() - start;
            r.close();
            //System.out.print(".");
        }

        //xpp with direct acces to the xpp parser
        for( int i=0; i<TIMES; i++ )
        {
            Reader r = new BufferedReader( new FileReader(file));
            start = new Date().getTime();
            MXParser xpp = (MXParser) new MXParserCachingStrings();
            xpp.setFeature( MXParser.FEATURE_PROCESS_NAMESPACES, true);
            xpp.setInput( r );
            int eventType = xpp.getEventType();
            while ( eventType != xpp.END_DOCUMENT )
            {
                eventType = xpp.nextToken();
            }
            xrtime += new Date().getTime() - start;
            r.close();
            //System.out.print("'");
        }

        //xpp with XppXmlStreamImpl
        for( int i=0; i<TIMES; i++ )
        {
            Reader r = new BufferedReader( new FileReader(file));
            //XMLInputStream xpp = new XppAdaptor().getStream(r2);
            start = new Date().getTime();
            XMLInputStream xpp = (XMLInputStream) new XppXmlStreamImpl(r);

            while( xpp.hasNext() )
            {
                XMLEvent xppevent = xpp.peek();
                xppevent = xpp.next();
            }
            xstime += new Date().getTime() - start;
            r.close();
            //System.out.print("`");
        }

        System.out.println("PERF:  babel/xppIS = " + dec(babeltime/xstime) +
                           " \tbabel/xppRaw = " + dec(babeltime/xrtime) );
    }

    static String dec( double f)
    {
        return "" + ((double)Math.round(f*100))/100;
    }
}
