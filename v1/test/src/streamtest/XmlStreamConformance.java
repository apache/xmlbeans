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

package streamtest;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Date;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.mxp1.MXParserCachingStrings;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLInputStreamFactory;
import org.apache.xmlbeans.xml.stream.XMLEvent;
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
