/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package drtcases;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
   
public class StoreTestsXqrl extends TestCase
{
    public StoreTestsXqrl(String name) { super(name); }
    public static Test suite() { return new TestSuite(StoreTestsXqrl.class); }

    private void doTokenTest ( String xml )
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse( xml ).newCursor();
        String s = c.execQuery( "$this" ).xmlText();
        Assert.assertTrue( s.equals( xml ) );
    }
    
    private void doSaveTest ( String xml )
        throws Exception
    {
        doTokenTest( xml );
    }

    public void testSaving ( )
        throws Exception
    {
        doSaveTest( "<foo xmlns=\"foo.com\"><bar>1</bar></foo>" );
        doSaveTest( "<foo><!--comment--><?target foo?></foo>" );
        doSaveTest( "<foo>a<bar>b</bar>c<bar>d</bar>e</foo>" );
        doSaveTest( "<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>" );
        doSaveTest( "<foo x=\"y\" p=\"r\"/>" );

        String s = "<foo>aaa</foo>bbb";
        s = s + s + s + s + s + s + s + s + s + s + s + s + s + s + s;
        s = "<bar>xxxx" + s + "</bar>";
        
        doSaveTest( s );

        XmlObject x =
            XmlObject.Factory.parse( "<foo xmlns:a='a.com'><bar xmlns:a='b.com'/></foo>" );

        XmlCursor c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        Assert.assertTrue( c.xmlText().equals( "<bar xmlns:a=\"b.com\"/>" ) );
        
        x = XmlObject.Factory.parse( "<foo xmlns:a='a.com'><bar/></foo>" );

        c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        Assert.assertTrue( c.xmlText().equals( "<bar xmlns:a=\"a.com\"/>" ) );
    }
    
    
    private void testTextFrag ( String actual, String expected )
    {
        String pre = "<xml-fragment>";
        
        String post = "</xml-fragment>";
        
        Assert.assertTrue( actual.startsWith( pre ) );
        Assert.assertTrue( actual.endsWith( post ) );
        
        Assert.assertTrue(
            expected.equals(
                actual.substring(
                    pre.length(), actual.length() - post.length() ) ) );
    }

    //
    // Make sure XQuery works (tests the saver too)
    //

    public void testXQuery ( )
        throws Exception
    {
        XmlCursor c =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar></foo>" ).newCursor();

        String s =
            c.execQuery( "$this//bar sort by (. descending)" ).xmlText();

        testTextFrag( s, "<bar>2</bar><bar>1</bar>" );
        
        c = XmlObject.Factory.parse( "<foo></foo>" ).newCursor();
        c.toNextToken();
        c.toNextToken();
        c.insertElement( "boo", "boo.com" );
        c.toStartDoc();
        
        Assert.assertTrue(
            c.execQuery( "$this" ).
                xmlText().equals(
                    "<foo><boo:boo xmlns:boo=\"boo.com\"/></foo>" ) );
    }

    
    public void testPathing ( )
        throws Exception
    {
        XmlObject x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>" );

        XmlCursor c = x.newCursor();

        c.selectPath( "$this//bar" );
        
        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>1</bar>" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>2</bar>" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>3</bar>" ) );

        Assert.assertTrue( !c.toNextSelection() );

        //
        //
        //

        x =
            XmlObject.Factory.parse(
                "<foo><bar x='1'/><bar x='2'/><bar x='3'/></foo>" );

        c = x.newCursor();

        c.selectPath( "$this//@x" );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "1" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "2" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "3" ) );

        Assert.assertTrue( !c.toNextSelection() );

        //
        //
        //

        x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>" );

        c = x.newCursor();

        c.selectPath( "$this//text()" );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "1" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "2" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "3" ) );

        Assert.assertTrue( !c.toNextSelection() );
    }
}
