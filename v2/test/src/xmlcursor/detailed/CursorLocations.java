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


package xmlcursor.detailed;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderType;
import test.xbean.xmlcursor.purchaseOrder.USAddress;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import java.math.BigDecimal;

/**
 *
 *
 */
public class CursorLocations extends BasicCursorTestCase {

    private Bookmark0 _theBookmark0 = new Bookmark0("value0");

    public CursorLocations(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CursorLocations.class);
    }

    public void testLocation() throws Exception {
        XmlCursor xc1, xc2, xc3, xc4;
        XmlObject m_xo1;


        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xo1 = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);


        xc1 = m_xo.newCursor();
        xc2 = m_xo.newCursor();
        xc3 = m_xo1.newCursor();


        toNextTokenOfType(xc2, TokenType.END);
        toNextTokenOfType(xc3, TokenType.START);


        while (xc1.isLeftOf(xc2)) {
            assertEquals(false, xc1.isRightOf(xc2));
            assertEquals(true, xc2.isRightOf(xc1));
            assertEquals(true, xc1.isInSameDocument(xc2));
            assertEquals(false, xc2.isAtSamePositionAs(xc1));
            assertEquals(false, xc1.isAtSamePositionAs(xc2));
            assertEquals(1, xc2.comparePosition(xc1));
            assertEquals(-1, xc1.comparePosition(xc2));
            //	System.out.println(xc1.currentTokenType() + "       " +  xc2.currentTokenType());
            xc1.toNextToken();
            xc2.toPrevToken();
        }

        System.out.println("Current Token Type "   +  xc1.currentTokenType() + "       " +  xc2.currentTokenType());
        assertEquals(xc1.getChars(), xc2.getTextValue());
        //System.out.println(xc2.getTextValue());
        xc2.toNextToken();
        assertEquals(true, xc1.isAtSamePositionAs(xc2));
        xc2.toPrevToken();
        xc2.toEndToken();

//comparing two cursors in the middle of text

        assertEquals(xc2.toPrevChar(4), xc1.toNextChar(4));
        assertEquals(true, xc2.isRightOf(xc1));
        assertEquals(false, xc1.isRightOf(xc2));
        assertEquals(false, xc2.isLeftOf(xc1));
        assertEquals(false, xc1.isAtSamePositionAs(xc2));
        assertEquals(1, xc2.comparePosition(xc1));
        assertEquals(true, xc1.isInSameDocument(xc2));
        xc1.toNextChar(3);
        assertEquals(0, xc2.comparePosition(xc1));
        assertEquals(xc1.currentTokenType(), xc2.currentTokenType());

//Comparing the same cursor to itself
        xc1.toNextChar(1);
        assertEquals(false, xc1.isRightOf(xc1));
        assertEquals(0, xc2.comparePosition(xc2));
        assertEquals(true, xc2.isInSameDocument(xc2));
        assertEquals(true, xc2.isAtSamePositionAs(xc2));

        assertEquals(TokenType.START, xc2.toPrevToken());
        xc1.setBookmark(_theBookmark0);

//moving xml and bookmark to a different location
        assertEquals(true, xc2.moveXml(xc3));
        xc4 = _theBookmark0.createCursor();

        assertEquals(true, xc4.isInSameDocument(xc3));
        assertEquals(-1, xc4.comparePosition(xc3));
        assertEquals(TokenType.END, xc3.toPrevToken());
        xc3.toPrevChar(3);
        assertEquals(0, xc4.comparePosition(xc3));

//comparing in  two different documents
        assertEquals(false, xc2.isInSameDocument(xc3));


        try {
            xc4.isLeftOf(xc2);
            fail("Expecting Illegal Argument Exception");
        }
        catch (IllegalArgumentException ie) {
        }

        xc1.dispose();
        xc2.dispose();
        xc3.dispose();
        xc4.dispose();

    }

    public void testLocationATTR() throws Exception {
        XmlCursor xc1, xc2;
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_5ATTR_TEXT);

        xc1 = m_xo.newCursor();
        xc2 = m_xo.newCursor();

        toNextTokenOfType(xc1, TokenType.ATTR);
        toNextTokenOfType(xc2, TokenType.ATTR);

        int i = 0;
        while (xc2.currentTokenType() == TokenType.ATTR) {
            xc2.toNextToken();
            ++i;
        }

        assertEquals(5, i);
        xc2.toPrevToken();

//moving betweenAttributes. one cursor is at the last ATTR and other is at first ATTR.
        while (xc1.isLeftOf(xc2)) {
            assertEquals(false, xc1.isRightOf(xc2));
            assertEquals(true, xc2.isRightOf(xc1));
            assertEquals(true, xc1.isInSameDocument(xc2));
            assertEquals(false, xc2.isAtSamePositionAs(xc1));
            assertEquals(false, xc1.isAtSamePositionAs(xc2));
            assertEquals(1, xc2.comparePosition(xc1));
            assertEquals(-1, xc1.comparePosition(xc2));
            //	System.out.println(xc1.currentTokenType() + "       " +  xc2.currentTokenType());
            xc1.toNextToken();
            xc2.toPrevToken();
        }
        assertEquals(true, xc1.isAtSamePositionAs(xc2));

//inserting and then comparing to make sure cursors move properly.
        xc2.insertAttributeWithValue("attr5", "val5");
        assertEquals(0, xc1.comparePosition(xc2));

        xc2.toPrevToken();
        assertEquals("val5", xc2.getTextValue());

        xc1.dispose();
        xc2.dispose();

    }

    public void testLocationTEXTMiddle() throws Exception {
        XmlCursor xc1, xc2, xc3;
        m_xo = XmlObject.Factory.parse(Common.XML_TEXT_MIDDLE);

        xc1 = m_xo.newCursor();
        xc2 = m_xo.newCursor();
        xc3 = m_xo.newCursor();


        //	 while(xc2.currentTokenType() != TokenType.ENDDOC)
        //	{
        //    System.out.println(xc2.currentTokenType());
        //    xc2.toNextToken();
        // }

//moving cursor to right locations. one is in middle of mixed content. the others is in middle of text of first node and last node

        toNextTokenOfType(xc1, TokenType.TEXT);
        toNextTokenOfType(xc2, TokenType.TEXT);
        toNextTokenOfType(xc3, TokenType.START);
        toNextTokenOfType(xc2, TokenType.TEXT);
        xc1.toNextChar(4);
        xc2.toNextChar(5);
        xc3.toEndToken();
        xc3.toPrevToken();
        xc3.toPrevChar(3);
//comparing positions
        assertEquals(-1, xc2.comparePosition(xc3));
        assertEquals(true, xc2.isRightOf(xc1));
        assertEquals(true, xc1.isInSameDocument(xc2));
        assertEquals(false, xc2.isAtSamePositionAs(xc3));

//moving cursors
        xc3.toPrevChar(2);
        xc2.toNextChar(1);
//comparing position once again
        assertEquals(-1, xc2.comparePosition(xc3));
        assertEquals(true, xc2.isRightOf(xc1));
        assertEquals(true, xc1.isInSameDocument(xc2));
        assertEquals(false, xc2.isAtSamePositionAs(xc3));

//moving and bringing them to identical positions
        xc3.toPrevToken();
        xc2.toNextChar(2);
        assertEquals(true, xc2.isAtSamePositionAs(xc3));

        xc1.dispose();
        xc2.dispose();
        xc3.dispose();


    }


    public void testXmlObjectUsingCursor() throws Exception {
        XmlCursor xc1, xc2, xc3;

        //PurchaseOrderDocument pod = (PurchaseOrderDocument)XmlObject.Factory.parse(Common.XML_PURCHASEORDER);
        PurchaseOrderDocument pod = PurchaseOrderDocument.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        xc1 = pod.newCursor();
        xc2 = pod.newCursor();
        xc3 = pod.newCursor();

        //while(xc1.currentTokenType() != TokenType.ENDDOC)
        // {
        //System.out.println(xc1.currentTokenType());

        //if (xc1.currentTokenType() == TokenType.TEXT)
        //System.out.println( xc1.getChars());

        //xc1.toNextToken();
        //}

        //moving cursor location so that it comes to zip under shipto

        toNextTokenOfType(xc1, TokenType.START);
        toNextTokenOfType(xc1, TokenType.START);
        toNextTokenOfType(xc2, TokenType.START);
        toNextTokenOfType(xc2, TokenType.START);
        toNextTokenOfType(xc3, TokenType.START);
        toNextTokenOfType(xc3, TokenType.START);

        xc1.toEndToken();
        xc2.toEndToken();
        xc3.toEndToken();

        toPrevTokenOfType(xc1, TokenType.TEXT);
        toPrevTokenOfType(xc1, TokenType.TEXT);
        toPrevTokenOfType(xc2, TokenType.TEXT);
        toPrevTokenOfType(xc2, TokenType.TEXT);
        toPrevTokenOfType(xc3, TokenType.TEXT);
        toPrevTokenOfType(xc3, TokenType.TEXT);

        assertEquals(xc1.getChars(), xc2.getChars(), xc3.getChars());

        xc2.toNextChar(3);
        xc3.toNextChar(5);
        assertEquals(false, xc2.isAtSamePositionAs(xc3));
        assertEquals(false, xc3.isAtSamePositionAs(xc1));


//setting zip value through the object . once the set occurs comparing postions of cursors.
        PurchaseOrderType pt = pod.getPurchaseOrder();
        USAddress usa = pt.getShipTo();
        usa.setZip(new BigDecimal(500));

        //System.out.println(usa.getZip());

        assertEquals(true, xc2.isAtSamePositionAs(xc3));
        assertEquals(true, xc3.isAtSamePositionAs(xc1));

        xc1.toPrevChar(3);
        xc2.toPrevChar(3);

        //System.out.println(xc1.getChars());
        assertEquals(xc1.getChars(), xc2.getChars());
        assertEquals(1, xc3.comparePosition(xc1));

//inserting an element through the cursor under zip and then doing a set of a valid value through object..

        xc1.insertElementWithText("foo", "text");
        toPrevTokenOfType(xc1, TokenType.START);
        toPrevTokenOfType(xc1, TokenType.START);
        //System.out.println("here" + xc1.getTextValue());

        toNextTokenOfType(xc1, TokenType.START);

        xc1.toNextChar(2);
        usa.setZip(new BigDecimal(90852));

        assertEquals(true, xc2.isAtSamePositionAs(xc3));
        assertEquals(true, xc3.isAtSamePositionAs(xc1));
        xc1.toPrevChar(5);
        assertEquals("90852", xc1.getChars());


        xc1.dispose();
        xc2.dispose();
        xc3.dispose();


    }


    public class Bookmark0 extends XmlCursor.XmlBookmark {
        public String text;

        public Bookmark0(String text) {
            this.text = text;
        }
    }


}

