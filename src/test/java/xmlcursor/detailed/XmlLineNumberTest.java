/*   Copyright 2005 The Apache Software Foundation
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
 *   limitations under the License.
 */
package xmlcursor.detailed;

import common.Common;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class XmlLineNumberTest extends Common
{
    public static final String xml = 
        "<people><person born=\"1912\" died=\"1954\" id=\"p342\">\n" + 
        "    <name>\n" + 
        "\t\t<first_name>Alan</first_name>\n" + 
        "\t\t<last_name>Turing</last_name>\n" + 
        "</name>\n" + 
        "</person></people>";
 
    public static final String xmlFile = 
        XBEAN_CASE_ROOT + P + "xmlcursor" + P + "Employees.xml";

    /** test obtaining XmlLineNumber bookmark with option
        XmlOptions.setLoadLineNumbers() */
    @Test
    public void testGetBookmark1() throws Exception
    {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers();
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstChild();
        assertEquals(XmlCursor.TokenType.START, c.currentTokenType());
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertNotNull(ln);
        assertEquals(1, ln.getLine());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        c.toEndToken();
        assertEquals(XmlCursor.TokenType.END, c.currentTokenType());
        ln =(XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        // no bookmark at END
        assertNull(ln);
    }

    /** test obtaining XmlLineNumber bookmark with option
        XmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT)
    */
    @Test
    public void testGetBookmark2() throws Exception
    {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstChild();
        assertEquals(XmlCursor.TokenType.START, c.currentTokenType());
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertNotNull(ln);
        assertEquals(1, ln.getLine());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        c.toEndToken();
        assertEquals(XmlCursor.TokenType.END, c.currentTokenType());
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        // there is a bookmark at END
        assertNotNull(ln);
        assertEquals(19, ln.getLine());
    }

    /** test using XmlLineNumber to get line number, column, and offset
        - parsing xml from string */
    @Test
    public void testLineNumber1() throws Exception
    {
        XmlOptions opt = new XmlOptions().setLoadLineNumbers();
        XmlObject xo = XmlObject.Factory.parse(xml, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(1, ln.getLine());
        assertEquals(50, ln.getColumn());
        // offset is not implemented
        assertEquals(-1, ln.getOffset());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        assertEquals(10, ln.getColumn());
        c.toFirstChild();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        // finishes after reading after <first_name> + 2xtabs
        assertEquals(14, ln.getColumn());
    }

    /** test using XmlLineNumber to get line number, column, and offset
        - parsing xml from file */
    @Test
    public void testLineNumber2() throws Exception {
        File f = new File(xmlFile);
        XmlOptions opt = new XmlOptions();
        opt.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
        XmlObject xo = XmlObject.Factory.parse(f, opt);
        XmlCursor c = xo.newCursor();
        c.toFirstContentToken();
        c.toFirstChild();
        XmlLineNumber ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(2, ln.getLine());
        assertEquals(15, ln.getColumn());
        assertEquals(-1, ln.getOffset());
        c.toFirstChild();
        c.push();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        assertEquals(13, ln.getColumn());
        c.toEndToken();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(3, ln.getLine());
        assertEquals(33, ln.getColumn());
        c.pop();
        c.toNextSibling(); //address
        c.toEndToken();
        ln = (XmlLineNumber) c.getBookmark(XmlLineNumber.class);
        assertEquals(9, ln.getLine());
        assertEquals(17, ln.getColumn());
        assertEquals(-1, ln.getOffset());
    }
}
