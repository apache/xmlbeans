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

/*
* Created by cezar@bea.com
*/

import weblogic.xml.stream.XMLStreamException;
import weblogic.xml.stream.XMLInputStream;
import weblogic.xml.stream.XMLInputStreamFactory;

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

