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

/*
* Created by cezar@bea.com
*/

import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLInputStreamFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileReader;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.stream.XmlStreamSource;
import org.xmlpull.stream.XppXmlStreamImpl;
import org.xmlpull.stream.XmlStreamFactory;
import junit.framework.TestCase;

public class SubStreamsTest
{
    public SubStreamsTest(String name)
    {
        //super(name);
    }

    public void testSubStream()
    {
        System.out.println("It works.");

        //throw new RuntimeException("Kevin's exception");

        //assertTrue( true );
    }

    public static void main(String[] args)
        throws FileNotFoundException, XMLStreamException, IOException,
            XmlPullParserException
    {
        Reader reader1;
        Reader reader2;
        if( args.length==0 )
        {
            String s = "<a><b><c a1='a1v' a2='a2v' /></b><x/>" +
                "<y>Some text</y></a>";
            reader1 = new StringReader(s);
            reader2 = new StringReader(s);
        }
        else
        {
            reader1 = new FileReader(args[0]);
            reader2 = new FileReader(args[0]);
        }

        System.out.println("reader ...");
        XmlStreamSource source = new XppXmlStreamImpl(reader1);
        XMLInputStream myStream = XmlStreamFactory.
                createXmlInputStream( source );

        XMLInputStream babelStream = XMLInputStreamFactory.newInstance().
            newInputStream(reader2);

        System.out.println("myStream ...");
        for(int i=0; myStream.hasNext() && i<3; i++)
        {
            System.out.println( "my: " + myStream.hasNext() + " '" +
                myStream.next() + "'");
            System.out.println( "bb: " + babelStream.hasNext() + " '" +
                    babelStream.next() + "'");
        }

        XMLInputStream myss = myStream.getSubStream();
        XMLInputStream bbss = babelStream.getSubStream();

        for(int i=0; (myss.hasNext() || bbss.hasNext()) && i<3; i++)
        {
            System.out.println( "my SUBStream: " + myss.hasNext() + " '" +
                    myss.next() + "'");
            System.out.println( "bb SUBStream: " + bbss.hasNext() + " '" +
                    bbss.next() + "'");
        }

        XMLInputStream myss2 = myss.getSubStream();
        XMLInputStream bbss2 = myss.getSubStream();
        for(; myss2.hasNext() || bbss2.hasNext() ;)
        {
            System.out.println( "my SubSUBStream 2: " + myss2.hasNext() +
                    " '" + myss2.next() + "'");
            System.out.println( "bb SubSUBStream 2: " + bbss2.hasNext() +
                    " '" + bbss2.next() + "'");
        }
        myss2.close();
        bbss2.close();

        for(; myss.hasNext() || bbss.hasNext() ;)
        {
            System.out.println( "my SUBStream: " + myss.hasNext() + " '" +
                    myss.next() + "'");
            System.out.println( "bb SUBStream: " + bbss.hasNext() + " '" +
                    bbss.next() + "'");
        }

        for(; myStream.hasNext() || babelStream.hasNext();)
        {
            System.out.println( "my: " + myStream.hasNext() + " '" +
                    myStream.next() + "'");
            System.out.println( "bb: " + babelStream.hasNext() + " '" +
                    babelStream.next() + "'");
        }

        main2();
    }

    public static void main2()
        throws FileNotFoundException, XMLStreamException, IOException,
            XmlPullParserException
    {
        Reader reader1;
        Reader reader2;
            String s = "<a><b><c a1='a1v' a2='a2v' /></b><x/>" +
                "<y>Some text</y></a>";
            reader1 = new StringReader(s);
            reader2 = new StringReader(s);

        System.out.println("reader ...");
        XmlStreamSource source = new XppXmlStreamImpl(reader1);
        XMLInputStream myStream = XmlStreamFactory.
                createXmlInputStream( source );

        XMLInputStream babelStream = XMLInputStreamFactory.newInstance().
            newInputStream(reader2);

        System.out.println("myStream ...");
        for(int i=0; myStream.hasNext() && i<3; i++)
        {
            System.out.println( "my: " + " '" +
                myStream.next() + "'");
            System.out.println( "bb: " + " '" +
                    babelStream.next() + "'");
        }

        XMLInputStream myss = myStream.getSubStream();
        XMLInputStream bbss = babelStream.getSubStream();

        for(int i=0; myss.hasNext() && i<3; i++)
        {
            System.out.println( "my SUBStream: " + " '" +
                    myss.next() + "'");
            System.out.println( "bb SUBStream: " + " '" +
                    bbss.next() + "'");
        }

        XMLInputStream myss2 = myss.getSubStream();
        XMLInputStream bbss2 = myss.getSubStream();
        for(; myss2.hasNext() ;)
        {
            System.out.println( "my SubSUBStream 2: " +
                    " '" + myss2.next() + "'");
            System.out.println( "bb SubSUBStream 2: " +
                    " '" + bbss2.next() + "'");
        }
        myss2.close();
        bbss2.close();

        for(; myss.hasNext() || bbss.hasNext() ;)
        {
            System.out.println( "my SUBStream: " + " '" +
                    myss.next() + "'");
            System.out.println( "bb SUBStream: " + " '" +
                    bbss.next() + "'");
        }

        for(; myStream.hasNext() ;)
        {
            System.out.println( "my: " + " '" +
                    myStream.next() + "'");
            System.out.println( "bb: " +
                    //babelStream.hasNext() +  //BABEL bugbug: un/comment this
                                //line you'll get different behavior for next()
                    " '" +
                    babelStream.next() + "'");
        }

        main3();
    }

    public static void main3()
        throws FileNotFoundException, XMLStreamException, IOException,
            XmlPullParserException
    {
        Reader reader1;
        Reader reader2;
        String s = "<?xml version='1.1.1'?><a><b xmlns='' xmlns:a='sdfsdf'><c a1='a&lt;1v' a2='a2v' /></b><x/>" +
            "<y>Some&lt; and &gt;text</y></a>";
        reader1 = new StringReader(s);
        reader2 = new StringReader(s);

        System.out.println("reader ... in main3");
        XmlStreamSource source = new XppXmlStreamImpl(reader1);
        XMLInputStream myStream = XmlStreamFactory.
                createXmlInputStream( source );

        //XMLInputStream babelStream = XMLInputStreamFactory.newInstance().
        //    newInputStream(reader2);

        System.out.println("myStream ...");
        for(int i=0; myStream.hasNext() && i<0; i++)
        {
            System.out.println( "my: " + " '" +
                myStream.next() + "'");
            //System.out.println( "bb: " + " '" +
            //        babelStream.next() + "'");
        }

        XMLInputStream myss = myStream.getSubStream();
        //XMLInputStream bbss = babelStream.getSubStream();

        for(int i=0; myss.hasNext() && i<3; i++)
        {
            System.out.println( "my SUBStream: " + " '" +
                    myss.next() + "'");
            //System.out.println( "bb SUBStream: " + " '" +
            //        bbss.next() + "'");
        }

        XMLInputStream myss2 = myss.getSubStream();
        //XMLInputStream bbss2 = myss.getSubStream();
        for(; myss2.hasNext() ;)
        {
            System.out.println( "my SubSUBStream 2: " +
                    " '" + myss2.next() + "'");
            //System.out.println( "bb SubSUBStream 2: " +
            //        " '" + bbss2.next() + "'");
        }
        myss2.close();
        //bbss2.close();

        for(; myss.hasNext() /*|| bbss.hasNext()*/ ;)
        {
            System.out.println( "my SUBStream: " + " '" +
                    myss.next() + "'");
            //System.out.println( "bb SUBStream: " + " '" +
            //        bbss.next() + "'");
        }

        for(; myStream.hasNext() ;)
        {
            System.out.println( "my: " + " '" +
                    myStream.next() + "'");
            //System.out.println( "bb: " +
                    //babelStream.hasNext() +  //BABEL bugbug: un/comment this
                                //line you'll get different behavior for next()
            //        " '" +
            //        babelStream.next() + "'");
        }
    }
}

