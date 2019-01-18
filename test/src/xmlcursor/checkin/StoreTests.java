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

package xmlcursor.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.*;

public class StoreTests {

    static String[] _args;
    static String _test;


    private void streamTest(String xml)
        throws Exception {
        XmlObject x1 = XmlObject.Factory.parse(xml);
        XmlObject x2 = XmlObject.Factory.parse(x1.newCursor().newXMLStreamReader());

        String x1Text = x1.xmlText();
        String x2Text = x2.xmlText();

        assertEquals(x1Text, x2Text);
    }

    @Test
    public void testXMLStreamReader() throws Exception {
        streamTest("<a/>");
        streamTest("<a x='y'/>");
        streamTest("<a><b>foo</b></a>");
        streamTest("<a><b>fo<!--moo-->o<?goof ball?>dsfdf</b></a>");
        streamTest("<a xmlns='nnn'></a>");
        streamTest("<a x='y'><!---->x<b/><c p='q'>z</c></a>");
        streamTest("<a x='y'><!----><b>moo</b><c p='q'></c></a>");
        streamTest("<a>asa<b/>sdsd<c>aaz</c>adsasd</a>");
        streamTest("<a><?target value?></a>");
        streamTest("<n:a xmlns:n='nnn'></n:a>");
        streamTest("<j:a x='y' p='q' xmlns:j='k'></j:a>");
        streamTest("<foo xmlns=\"foo.com\"><bar>1</bar></foo>");
        streamTest("<foo><!--comment--><?target foo?></foo>");
        streamTest("<foo>a<bar>b</bar>c<bar>d</bar>e</foo>");
        streamTest("<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>");
        streamTest("<foo x=\"y\" p=\"r\"/>");
    }

    @Test
    public void testReplaceContents() throws Exception {
        XmlObject xDst = XmlObject.Factory.newInstance();
        XmlObject xSrc = XmlObject.Factory.parse("<foo/>");
        XmlObject newDst = xDst.set(xSrc);
        assertEquals("<foo/>", newDst.xmlText());

        xDst = XmlObject.Factory.parse("<bar/>");
        xSrc = XmlObject.Factory.parse("<foo/>");
        XmlCursor c = xDst.newCursor();
        c.toNextToken();
        xDst = c.getObject();
        xDst.set(xSrc);
        c.toStartDoc();
        xDst = c.getObject();
        assertEquals("<bar><foo/></bar>", xDst.xmlText());

        xDst = XmlObject.Factory.parse("<bar x='y'/>");
        xSrc = XmlObject.Factory.parse("<foo>moo</foo>");
        c = xDst.newCursor();
        c.toNextToken();
        c.toNextToken();
        xDst = c.getObject();
        xDst.set(xSrc);
        c.toStartDoc();
        xDst = c.getObject();
        assertEquals("<bar x=\"moo\"/>", xDst.xmlText());
    }

    @Test(expected = Exception.class)
    public void testSniffing() throws Exception {
        XmlObject x;

        x = XmlObject.Factory.parse("<xoo/>");
        assertSame(x.schemaType(), XmlBeans.NO_TYPE);

        x = XmlObject.Factory.parse(
            "<schema xmlns='http://www.w3.org/2001/XMLSchema'/>");
        assertSame(x.schemaType(), SchemaDocument.type);

        x = XmlObject.Factory.parse(
            "<schema xmlns='http://www.w3.org/2001/XMLSchema/moo'/>");
        assertSame(x.schemaType(), XmlBeans.NO_TYPE);

        x = XmlObject.Factory.parse(
            "<schema xmlns='http://www.w3.org/2001/XMLSchema'/>");
        assertSame(x.schemaType(), SchemaDocument.type);

        x = org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Factory.parse(
            "<schema xmlns='http://www.w3.org/2001/XMLSchema'/>");
        assertSame(x.schemaType(), SchemaDocument.type);

        org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Factory.parse(
            "<schema xmlns='http://www.w3.org/2001/XMLSchema/moo'/>");
    }

    @Test
    public void testCursorStack() throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo x='y'/>");
        XmlCursor c = x.newCursor();
        c.push();
        c.toNextToken();
        c.push();
        c.toNextToken();
        assertTrue(c.isAttr());
        c.pop();
        assertTrue(c.isStart());
        c.pop();
        assertTrue(c.isStartdoc());
    }

    @Test
    public void testImplicitNamespaces() throws Exception {
        Map<String,String> namespaces = new HashMap<String,String>();
        namespaces.put("foo", "foo.com");
        namespaces.put("bar", "bar.com");
        namespaces.put("", "default.com");

        XmlOptions options = new XmlOptions();
        options.put(XmlOptions.SAVE_IMPLICIT_NAMESPACES, namespaces);

        XmlObject x = XmlObject.Factory.newInstance();
        XmlCursor c = x.newCursor();

        c.toNextToken();

        c.beginElement("a", "foo.com");
        c.beginElement("b", "default.com");
        c.beginElement("c", "bar.com");

        assertEquals("<foo:a><b><bar:c/></b></foo:a>", x.xmlText(options));
    }

    private static class Content implements ContentHandler, LexicalHandler {
        public void startDocument() {
            add("START_DOCUMENT");
        }

        public void endDocument() {
            add("END_DOCUMENT");
        }

        public void startElement(
            String namespaceURI, String localName,
            String qName, Attributes atts) {
            add("START_ELEMENT");
            add("  namespaceURI: " + namespaceURI);
            add("  localName: " + localName);
//            add( "  qName: " + qName ); 

            TreeSet sortedAttrs = new TreeSet();

            for (int i = 0; i < atts.getLength(); i++) {
                String ln = atts.getLocalName(i);
                String uri = atts.getURI(i);
                String qname = atts.getQName(i);

                if (ln.equals("xmlns"))
                    continue;
                if (qname.startsWith("xmlns"))
                    continue;

//                if (ln.equals( "xmlns" ))
//                    ln = "";
//
//                if (uri.equals( "xmlns" ))
//                    uri = "";

                sortedAttrs.add(
                    uri + "-" + ln + "-" +
                    atts.getQName(i) + "-" + atts.getType(i) + "-" +
                    atts.getValue(i));
            }

            for (Iterator i = sortedAttrs.iterator(); i.hasNext(); )
                add("  Attr: " + i.next());
        }

        public void endElement(
            String namespaceURI, String localName, String qName) {
            add("END_ELEMENT");
            add("  namespaceURI: " + namespaceURI);
            add("  localName: " + localName);
//            add( "  qName: " + qName );
        }

        public void characters(char[] ch, int start, int length) {
            if (length > 0) {
                add("CHARACTERS");
                add(ch, start, length);
            }
        }

        public void comment(char[] ch, int start, int length) {
            add("COMMENT");
            add("  Comment: ", ch, start, length);
        }

        public void processingInstruction(String target, String data) {
            add("PROCESSING_INSTRUCTION");
            add("target: " + target);
            add("data: " + data);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) {
            if (length > 0) {
                add("IGNORABLE_WHITESPACE");
                add("  whitespace: ", ch, start, length);
            }
        }

        public void startPrefixMapping(String prefix, String uri) {
            add("START_PREFIX_MAPPING");
//            add( "  prefix: " + prefix );
            add("  uri: " + uri);
        }

        public void endPrefixMapping(String prefix) {
            add("END_PREFIX_MAPPING");
//            add( "  prefix: " + prefix );
        }

        public void startCDATA() {
            add("START_CDATA");
        }

        public void endCDATA() {
            add("END_CDATA");
        }

        public void startDTD(String name, String publicId, String systemId) {
            add("START_DTD");
            add("  name: " + name);
            add("  publicId: " + publicId);
            add("  systemId: " + systemId);
        }

        public void endDTD() {
            add("END_DTD");
        }

        public void startEntity(String name) {
            add("START_ENTITY");
            add("  name: " + name);
        }

        public void endEntity(String name) {
            add("END_ENTITY");
            add("  name: " + name);
        }

        public void setDocumentLocator(Locator locator) {
            // add( "START_DOCUMENT_LOCATOR" );
        }

        public void skippedEntity(String name) {
            add("SKIPPED_ENTITY");
            add("  name: " + name);
        }

        private void add(String s) {
            _sb.append(s);
            _sb.append("\n");
        }

        private void add(String s, char[] buf, int off, int cch) {
            _sb.append(s);
            if (buf != null)
                _sb.append(buf, off, cch);
            _sb.append("\n");
        }

        private void add(char[] buf, int off, int cch) {
            _sb.append(buf, off, cch);
            _sb.append("\n");
        }

        public boolean equals(Object that) {
            return toString().equals(that.toString());
        }

        public String toString() {
            return _sb.toString();
        }

        private StringBuffer _sb = new StringBuffer();
    }

    private void doTestSaxSaver(String xml)
        throws Exception {
        // ME

        Content content2 = new Content();

        XmlObject x = XmlObject.Factory.parse(xml);

        x.save(content2, content2);

        // THEM

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();

        XMLReader xr = sp.getXMLReader();

        Content content1 = new Content();

        xr.setProperty("http://xml.org/sax/properties/lexical-handler", content1);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/validation", false);

        xr.setContentHandler(content1);

        InputSource is = new InputSource(new StringReader(xml));

        xr.parse(is);

        assertEquals(content1, content2);
    }

    @Test
    public void testSaxSaver()
        throws Exception {
        doTestSaxSaver("<a xmlns='nnn'></a>");
        doTestSaxSaver("<a x='y'><!---->x<b/><c p='q'>z</c></a>");
        doTestSaxSaver("<a x='y'><!----><b>moo</b><c p='q'></c></a>");
        doTestSaxSaver("<a>asa<b/>sdsd<c>aaz</c>adsasd</a>");
        doTestSaxSaver("<a><?target value?></a>");
        doTestSaxSaver("<n:a xmlns:n='nnn'></n:a>");
        doTestSaxSaver("<j:a x='y' p='q' xmlns:j='k'></j:a>");
    }

    @Test
    @Ignore
    public void testParsing() throws Exception {
        Random r = new Random(1);

        for (int i = 0; i < 100000; i++) {
            String xml = makeRandomDocument(r);
            XmlObject.Factory.parse(xml);
        }
    }

    private void doTestLineNumbers(String xml)
        throws Exception {
        int line = 1;
        int col = 1;

        XmlCursor c =
            XmlObject.Factory.parse(
                xml, new XmlOptions().setLoadLineNumbers()).
                newCursor();

        for (int i = 0; i < xml.length(); i++) {
            char ch = xml.charAt(i);

            if (ch == '<' && Character.isLetter(xml.charAt(i + 1))) {
                while (!c.currentTokenType().isStart())
                    c.toNextToken();

                assertTrue(c.currentTokenType().isStart());

                XmlLineNumber ln =
                    (XmlLineNumber)
                        c.getBookmark(XmlLineNumber.class);

                assertNotNull(ln);

                assertTrue(ln.getLine() == -1 || ln.getLine() == line);
                assertTrue(ln.getColumn() == -1 || ln.getColumn() == col);
                assertTrue(ln.getOffset() == -1 || ln.getOffset() == i);

                c.toNextToken();
            }

            if (ch == '\n') {
                line++;
                col = 1;
            } else
                col++;
        }
    }

    @Test
    @Ignore
    public void testLineNumbers() throws Exception {
        Random r = new Random(1);

        for (int i = 0; i < 1000; i++) {
            String xml = makeRandomDocument(r);
            doTestLineNumbers(xml);
        }
    }

    private static class DocBuilder {
        Random r;
        StringBuffer sb;

        DocBuilder(Random _r, StringBuffer _sb) {
            r = _r;
            sb = _sb;
        }

        void append(char ch) {
            sb.append(ch);
        }

        void append(String s) {
            sb.append(s);
        }

        public void whitespace() {
            int p = r.nextInt(100);

            if (p < 20)
                append('\t');
            else if (p < 40)
                append('\n');
            else
                append(' ');
        }

        void whitespaces() {
            for (int i = r.nextInt(8); i > 0; i--)
                whitespace();
        }

        char makeLetter() {
            return (char) (((int) 'a') + r.nextInt(26));
        }

        public void letter() {
            append(makeLetter());
        }

        void charEntity() {
            switch (r.nextInt(5)) {
                case 0:
                    append("&lt;");
                    break;
                case 1:
                    append("&gt;");
                    break;
                case 2:
                    append("&amp;");
                    break;
                case 3:
                    append("&apos;");
                    break;
                case 4:
                    append("&quot;");
                    break;
            }
        }

        public void text() {
            for (int i = r.nextInt(20); i > 0; i--) {
                int p = r.nextInt(100);

                if (p < 70)
                    letter();
                else if (p < 74)
                    charEntity();
                else
                    whitespace();
            }
        }

        String makeNcName() {
            StringBuilder name = new StringBuilder();

            for (; ; ) {
                char ch = makeLetter();

                if (ch == 'x' || ch == 'X')
                    continue;

                name.append(ch);

                break;
            }

            for (int i = r.nextInt(20); i > 0; i--)
                name.append(makeLetter());

            return name.toString();
        }

        void ncName() {
            append(makeNcName());
        }

        public void comment() {
            append("<!--");
            text();
            append("-->");
        }

        public void procinst() {
            append("<?");
            ncName();

            if (r.nextInt(100) < 90) {
                whitespace();
                text();
            }

            append("?>");
        }

        void whiteContent() {
            for (; ; ) {
                int p = r.nextInt(100);

                if (p < 20)
                    break;
                else if (p < 50)
                    whitespaces();
                else if (p < 70)
                    comment();
                else
                    procinst();
            }
        }

        void xmlDecl() {
            append("<?xml version=\"1.0\"?>");
        }

        public void content(int depth) {
            for (int i = r.nextInt(10); i > 0; i--) {
                switch (r.nextInt(4)) {
                    case 0:
                        elementContent(depth + 1);
                        break;

                    case 1:
                        text();
                        break;
                    case 2:
                        comment();
                        break;
                    case 3:
                        procinst();
                        break;
                }
            }
        }

        public void attribute() {
            ncName();

            if (r.nextInt(100) == 0)
                whitespaces();

            append('=');

            if (r.nextInt(100) == 0)
                whitespaces();

            char q = r.nextInt(2) == 0 ? '\'' : '"';

            append(q);

            text();

            append(q);
        }

        void elementContent(int depth) {
            // If depth == 0, guarantee an element, otherwise, as depth
            // increases, the probablility we'll spit out an element
            // gets smaller.

            if (r.nextInt(depth + 1) <= 1) {
                String name = makeNcName();

                append('<');
                append(name);

                if (r.nextInt(100) == 0)
                    whitespaces();

                HashMap attrs = new HashMap();

                for (int i = r.nextInt(3); i > 0; i--) {
                    append(' ');

                    String aname;

                    for (; ; ) {
                        aname = makeNcName();

                        if (!attrs.containsKey(aname))
                            break;
                    }

                    attrs.put(aname, null);

                    append(aname);

                    if (r.nextInt(100) == 0)
                        whitespaces();

                    append('=');

                    if (r.nextInt(100) == 0)
                        whitespaces();

                    char q = r.nextInt(2) == 0 ? '\'' : '"';

                    append(q);

                    text();

                    append(q);

                    if (r.nextInt(10) == 0)
                        whitespaces();
                }

                append('>');

                content(depth);

                append("</");
                append(name);

                if (r.nextInt(100) == 0)
                    whitespaces();

                append('>');
            }
        }

        public void document() {
            if (r.nextInt(2) == 0)
                xmlDecl();

            whiteContent();

            elementContent(0);

            whiteContent();
        }
    }

    private String makeRandomDocument(Random r) {
        StringBuffer sb = new StringBuffer();

        DocBuilder db = new DocBuilder(r, sb);

        db.document();

        return sb.toString();
    }

    private static class MyMark extends XmlBookmark {
    }

    @Test
    public void testBookmarks()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo x='y'>abcdefg<!---->xy</foo>");

        XmlCursor c = x.newCursor();
        MyMark m1 = new MyMark();
        c.setBookmark(m1);

        c.toNextToken();
        MyMark m2 = new MyMark();
        c.setBookmark(m2);

        c.toNextToken();
        MyMark m3 = new MyMark();
        c.setBookmark(m3);

        c.toNextToken();
        MyMark m4 = new MyMark();
        c.setBookmark(m4);

        c.toNextChar(1);
        MyMark m5 = new MyMark();
        c.setBookmark(m5);

        c.toNextChar(3);
        MyMark m6 = new MyMark();
        c.setBookmark(m6);

        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        MyMark m7 = new MyMark();
        c.setBookmark(m7);

        c.toNextToken();
        MyMark m8 = new MyMark();
        c.setBookmark(m8);

        c.toStartDoc();

        assertSame(c.getBookmark(MyMark.class), m1);
        assertSame(c.toNextBookmark(MyMark.class), m2);
        assertSame(c.toNextBookmark(MyMark.class), m3);
        assertSame(c.toNextBookmark(MyMark.class), m4);
        assertSame(c.toNextBookmark(MyMark.class), m5);
        assertSame(c.toNextBookmark(MyMark.class), m6);
        assertSame(c.toNextBookmark(MyMark.class), m7);
        assertSame(c.toNextBookmark(MyMark.class), m8);
        assertNull(c.toNextBookmark(MyMark.class));

        c.toEndDoc();

        assertSame(c.getBookmark(MyMark.class), m8);
        assertSame(c.toPrevBookmark(MyMark.class), m7);
        assertSame(c.toPrevBookmark(MyMark.class), m6);
        assertSame(c.toPrevBookmark(MyMark.class), m5);
        assertSame(c.toPrevBookmark(MyMark.class), m4);
        assertSame(c.toPrevBookmark(MyMark.class), m3);
        assertSame(c.toPrevBookmark(MyMark.class), m2);
        assertSame(c.toPrevBookmark(MyMark.class), m1);
        assertNull(c.toPrevBookmark(MyMark.class));
    }

    @Test
    public void testSetName()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo x='a'/>");
        XmlCursor c = x.newCursor();
        c.toNextToken();
        c.setName(new QName("bar"));
        c.toNextToken();
        c.setName(new QName("y"));

        assertEquals("<bar y=\"a\"/>", x.xmlText());
    }

    //
    // Basic load up a file and iterate through it
    //
    @Test
    public void testBasicXml()
        throws Exception {
        XmlCursor c = XmlObject.Factory.parse(Common.XML_ATTR_TEXT, null).newCursor();

        int n = 0;

        for (; ; ) {
            TokenType t = c.toNextToken();

            n++;

            if (t == TokenType.NONE)
                break;
        }

        assertEquals(6, n);
    }

    //
    // Make sure the tokens going forward the the reverse of the tokens
    // going backward
    //
    @Test
    public void testConsistentTokenOrder()
        throws Exception {
        ArrayList<TokenType> l = new ArrayList<TokenType>();

        XmlCursor c = XmlObject.Factory.parse(Common.XML_ATTR_TEXT, null).newCursor();


        for (; ; ) {
            // System.err.println(c.currentTokenType());
            l.add(c.currentTokenType());

            if (c.toNextToken() == TokenType.NONE)
                break;
        }

        c.toEndDoc();
        // System.err.println("Reversing");

        for (int i = l.size() - 1; ; i--) {
            // System.err.println(c.currentTokenType());
            assertEquals(l.get(i), c.currentTokenType());

            if (c.toPrevToken() == TokenType.NONE)
                break;
        }
    }

    // Make sure you can't insert text before the doc begin
    // going backward
    @Test(expected = IllegalStateException.class)
    public void testIllegalTextInsert()
        throws Exception {
        XmlCursor c = XmlObject.Factory.parse(Common.XML_ATTR_TEXT, null).newCursor();
        c.insertChars("Ho ho ho");
    }

    // Make sure getText works in a basic way
    @Test
    public void testgetText()
        throws Exception {
        XmlCursor c = XmlObject.Factory.parse(Common.XML_ATTR_TEXT, null).newCursor();
        assertEquals("ab", c.getTextValue()); // Doc node

        c.toNextToken();
        assertEquals("ab", c.getTextValue()); // Doc elem

        c.toNextToken();
        assertEquals("y", c.getTextValue()); // Attr x

        c.toNextToken();
        assertEquals("ab", c.getChars()); // Text

        c.toNextChar(1);
        assertEquals("b", c.getChars()); // Text

        c.toNextToken();
        assertEquals(0, c.getChars().length());       // End tag

        c.toNextToken();
        assertEquals(0, c.getChars().length());       // End doc
    }

    //
    // Text XMLInputStream support
    //

    private void doSaverTest(String xml) throws Exception {
        XmlCursor c = XmlObject.Factory.parse(xml).newCursor();
        assertEquals(xml, c.xmlText());
    }

    private void doSaveTest(String xml) throws Exception {
        doSaverTest(xml);
    }

    @Test
    public void testCDATA() throws Exception {
        // https://issues.apache.org/jira/browse/XMLBEANS-404
        String xml = "<foo>Unable to render embedded object: <![CDATA[>>>>>>>><<<<<<<<<<<]]></foo>";
        String expected = "<foo><![CDATA[Unable to render embedded object: >>>>>>>><<<<<<<<<<<]]></foo>";
        XmlOptions options = new XmlOptions().setSaveCDataLengthThreshold(0);
        XmlCursor c = XmlObject.Factory.parse(xml, options).newCursor();
        assertEquals(expected, c.xmlText(options));
    }

    @Test
    public void testSaving() throws Exception {
        doSaveTest("<foo xmlns=\"foo.com\"><bar>1</bar></foo>");
        doSaveTest("<foo><!--comment--><?target foo?></foo>");
        doSaveTest("<foo>a<bar>b</bar>c<bar>d</bar>e</foo>");
        doSaveTest("<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>");
        doSaveTest("<foo x=\"y\" p=\"r\"/>");

        String s = "<foo>aaa</foo>bbb";
        s = s + s + s + s + s + s + s + s + s + s + s + s + s + s + s;
        s = "<bar>xxxx" + s + "</bar>";

        doSaveTest(s);

        XmlObject x =
            XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar xmlns:a='b.com'/></foo>");

        XmlCursor c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        assertEquals("<bar xmlns:a=\"b.com\"/>", c.xmlText());

        x = XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar/></foo>");

        c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        assertEquals("<bar xmlns:a=\"a.com\"/>", c.xmlText());
    }


    private XmlCursor navDoc(XmlObject x, String dirs) {
        return navCursor(x.newCursor(), dirs);
    }

    private XmlCursor navNewCursor(XmlCursor c, String dirs) {
        return navCursor(c.newCursor(), dirs);
    }

    //
    // Format:
    //
    //    ( [-][N] type ) *
    //
    //    type:
    //
    //      c - character
    //      t - token
    //      p - parent element
    //      s - sibling element
    //      d - descendent (child) element
    //      r - end of the doc (root)
    //      b - begin of the doc
    //

    private XmlCursor navCursor(XmlCursor c, String dirs) {
        int n = 0;
        boolean prev = false;

        for (int i = 0; i < dirs.length(); i++) {
            char ch = dirs.charAt(i);

            if (ch == '-') {
                prev = !prev;
                continue;
            } else if (ch >= '0' && ch <= '9') {
                n = n * 10 + (ch - '0');
                continue;
            }

            if (n == 0)
                n = 1;

            if (ch == 'c') {
                if (prev)
                    assertEquals(c.toPrevChar(n), n);
                else
                    assertEquals(c.toNextChar(n), n);
            } else if (ch == 't') {
                while (n-- > 0) {
                    if (prev)
                        assertNotSame(c.toPrevToken(), TokenType.NONE);
                    else
                        assertNotSame(c.toNextToken(), TokenType.NONE);
                }
            } else if (ch == 'p') {
                assertTrue(!prev);

                while (n-- > 0)
                    assertTrue(c.toParent());
            } else if (ch == 'r') {
                assertTrue(!prev);
                assertEquals(1, n);

                c.toEndDoc();
            } else if (ch == 'b') {
                assertTrue(!prev);
                assertEquals(1, n);

                c.toStartDoc();
            } else if (ch == 's') {
                while (n-- > 0) {
                    if (prev)
                        assertTrue(c.toPrevSibling());
                    else
                        assertTrue(c.toNextSibling());
                }
            } else if (ch == 'd') {
                assertTrue(!prev);

                while (n-- > 0)
                    assertTrue(c.toFirstChild());
            } else {
                fail();
            }

            n = 0;
            prev = false;
        }

        return c;
    }

    @Test
    public void testOps()
        throws Exception {
        XmlObject x, x2, y;
        XmlCursor cFrom, cTo, cTemp, cTemp2, c, d;
        XmlBookmark anno;

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");
        cFrom = navDoc(x, "d");
        cTo = navNewCursor(cFrom, "");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        XmlOptions options = new XmlOptions();

        options.put(
            XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT,
            new QName(null, "bar"));

        assertTrue(
            x.xmlText(options).equals("<bar>[TO]<foo>abcdef</foo>[FROM]</bar>") ||
            x.xmlText(options).equals("<bar>[FROM]<foo>abcdef</foo>[TO]</bar>"));

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");

        cFrom = navDoc(x, "d");
        cTo = navNewCursor(cFrom, "ttt");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertTrue(
            x.xmlText(options).equals("<bar><foo>abcdef</foo>[FROM][TO]</bar>") ||
            x.xmlText(options).equals("<bar><foo>abcdef</foo>[TO][FROM]</bar>"));

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");

        cFrom = navDoc(x, "d");
        cTo = navNewCursor(cFrom, "t3c");
        assertTrue(!cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<bar>[FROM]<foo>abc[TO]def</foo></bar>", x.xmlText(options));

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a><b>pqr</b></r>");

        cFrom = navDoc(x, "dd");
        cTo = navNewCursor(cFrom, "r-1t");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<r>[FROM]<b>pqr</b><a>xyz</a>[TO]</r>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a><b>pqr</b>AB</r>");

        cFrom = navDoc(x, "dd");
        cTo = navNewCursor(cFrom, "r-1t-1c");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<r>[FROM]<b>pqr</b>A<a>xyz</a>[TO]B</r>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a><b>pqr</b>AB</r>");

        cFrom = navDoc(x, "dd");
        cTo = navNewCursor(cFrom, "stc");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<r>[FROM]<b>p<a>xyz</a>[TO]qr</b>AB</r>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a><b>pqr</b>AB</r>");

        cFrom = navDoc(x, "dd");
        cTo = navDoc(x, "d");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        testTextFrag(x.xmlText(), "<a>xyz</a>[TO]<r>[FROM]<b>pqr</b>AB</r>");

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a><b>pqr</b>AB</r>");

        cFrom = navDoc(x, "dd");
        cTo = navDoc(x, "r");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<bar><r>[FROM]<b>pqr</b>AB</r><a>xyz</a>[TO]</bar>", x.xmlText(options));

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a></r>");
        x2 = XmlObject.Factory.parse("<s></s>");

        cFrom = navDoc(x, "dd");
        cTo = navDoc(x2, "dt");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<r>[FROM]</r>", x.xmlText());
        assertEquals("<s><a>xyz</a>[TO]</s>", x2.xmlText());

        //

        x = XmlObject.Factory.parse("<r><a>pq</a><b></b></r>");

        cFrom = navDoc(x, "dd");
        cTo = navDoc(x, "ddst");
        cTemp = navDoc(x, "ddt1c");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");
        cTemp.insertChars("[TEMP]");

        assertEquals("<r>[FROM][TEMP]<b><a>pq</a>[TO]</b></r>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");

        cFrom = navDoc(x, "2t2c");
        cTo = navNewCursor(cFrom, "-1c");
        cFrom.moveChars(2, cTo);
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<foo>acd[TO]b[FROM]ef</foo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");

        cFrom = navDoc(x, "2t2c");
        cTo = navNewCursor(cFrom, "3c");
        cFrom.moveChars(2, cTo);
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<foo>ab[FROM]ecd[TO]f</foo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<bar><foo>abcdef</foo><foo>123456</foo></bar>");

        cFrom = navDoc(x, "3t2c");
        cTo = navNewCursor(cFrom, "3t3c");
        cFrom.moveChars(2, cTo);
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<bar><foo>ab[FROM]ef</foo><foo>123cd[TO]456</foo></bar>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<bar><foo>abcdef</foo><foo>123456</foo></bar>");

        cFrom = navDoc(x, "2d");
        cTo = navDoc(x, "2dst2c");
        assertTrue(cFrom.copyXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals(x.xmlText(),
            "<bar>[FROM]<foo>abcdef</foo><foo>12" +
            "<foo>abcdef</foo>[TO]3456</foo></bar>");

        //

        x = XmlObject.Factory.parse("<r><a>xyz</a></r>");
        x2 = XmlObject.Factory.parse("<s></s>");

        cFrom = navDoc(x, "dd");
        cTo = navDoc(x2, "dt");
        assertTrue(cFrom.copyXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<r>[FROM]<a>xyz</a></r>", x.xmlText());
        assertEquals("<s><a>xyz</a>[TO]</s>", x2.xmlText());

        //

        x = XmlObject.Factory.parse(
            "<bar><foo>abcdef</foo>blah<foo>123456</foo></bar>");

        cFrom = navDoc(x, "2d");
        cTo = navDoc(x, "2dst2c");
        assertTrue(cFrom.copyXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals(x.xmlText(), "<bar>[FROM]<foo>abcdef</foo>blah<foo>12" +
                                  "<foo>abcdef</foo>[TO]3456</foo></bar>");

        //

        x = XmlObject.Factory.parse(
            "<bar><foo x='y'>abcdef</foo><foo>123456</foo>7890</bar>");

        cFrom = navDoc(x, "2dt");
        cTo = navDoc(x, "2dst");
        cTemp = navDoc(x, "2dst3c");
        cTemp2 = navDoc(x, "2ds3t2c");
        assertTrue(cFrom.copyXml(cTo));
        cTemp.insertChars("[TEMP]");
        cTemp2.insertChars("[TEMP2]");

        assertEquals(x.xmlText(),
            "<bar><foo x=\"y\">abcdef</foo>" +
            "<foo x=\"y\">123[TEMP]456</foo>78[TEMP2]90</bar>");

        //

        x = XmlObject.Factory.parse(
            "<bar>xy<foo x='y'>abcdef</foo>pqr<foo>123456</foo></bar>");

        cFrom = navDoc(x, "2d");
        cTo = navDoc(x, "2ds-2c");

        assertTrue(cFrom.removeXml());

        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<bar>xy[FROM]p[TO]qr<foo>123456</foo></bar>", x.xmlText());

        //

        x = XmlObject.Factory.parse(
            "<bar>xy<foo x='y'>abcdef</foo>pqr<foo>123456</foo></bar>");

        cFrom = navDoc(x, "2d2t2c");
        cTo = navDoc(x, "2d2t5c");

        cFrom.removeChars(2);

        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals(x.xmlText(),
            "<bar>xy<foo x=\"y\">ab[FROM]e[TO]f" +
            "</foo>pqr<foo>123456</foo></bar>");

        //

        x = XmlObject.Factory.parse("<bar><!---->abc</bar>");

        cFrom = navDoc(x, "tt");
        cTo = navDoc(x, "tttc");

        assertTrue(cFrom.removeXml());

        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");

        assertEquals("<bar>[FROM]a[TO]bc</bar>", x.xmlText());

        //

        x = XmlObject.Factory.newInstance();

        cTo = navDoc(x, "t");
        cTo.insertElement("boo");
        cTo.toPrevToken();
        cTo.insertElement("moo");
        cTo.toPrevToken();
        cTo.insertElement("goo");

        assertEquals("<boo><moo><goo/></moo></boo>", x.xmlText());

        //

        x = XmlObject.Factory.newInstance();

        cTo = navDoc(x, "t");
        cTo.insertElement("boo");
        cTo.toPrevToken();
        cTo.insertElement("moo");
        cTo.toPrevToken();
        cTo.insertAttributeWithValue("x", "y");

        assertEquals("<boo><moo x=\"y\"/></boo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<bar x='y'>abc</bar>");
        cTo = navDoc(x, "tt");
        cTo.insertAttributeWithValue("p", "q");

        assertEquals("<bar p=\"q\" x=\"y\">abc</bar>", x.xmlText());

        // Text XmlBookmark

        x = XmlObject.Factory.parse("<r><foo>abc</foo><bar></bar></r>");
        cFrom = navDoc(x, "tt");
        anno = new Anno();
        cFrom.setBookmark(anno);
        cTo = navDoc(x, "6t");
        assertTrue(cFrom.moveXml(cTo));
        cFrom.insertChars("[FROM]");
        cTo.insertChars("[TO]");
        anno.createCursor().insertChars("[ANNO]");

        assertEquals("<r>[FROM]<bar>[ANNO]<foo>abc</foo>[TO]</bar></r>", x.xmlText());

        // Test content ops

        x = XmlObject.Factory.parse("<foo x='y'>abc</foo>");
        y = XmlObject.Factory.newInstance();
        d = y.newCursor();
        d.toNextToken();
        x.newCursor().moveXmlContents(d);
        assertEquals("<foo x=\"y\">abc</foo>", y.xmlText());

        x = XmlObject.Factory.parse("<bar><foo x='y'>abc</foo></bar>");
        y = XmlObject.Factory.newInstance();
        c = x.newCursor();
        c.toNextToken();
        d = y.newCursor();
        d.toNextToken();
        c.moveXmlContents(d);
        assertEquals("<foo x=\"y\">abc</foo>", y.xmlText());

        x = XmlObject.Factory.parse("<bar><foo x='y'>abc</foo></bar>");
        c = x.newCursor();
        c.toNextToken();
        c.removeXmlContents();
        assertEquals("<bar/>", x.xmlText());

        x = XmlObject.Factory.parse("<foo x='y'>abc</foo>");
        y = XmlObject.Factory.newInstance();
        d = y.newCursor();
        d.toNextToken();
        x.newCursor().copyXmlContents(d);
        assertEquals("<foo x=\"y\">abc</foo>", y.xmlText());
    }

    private static class Anno extends XmlBookmark {
    }

    @Test
    public void testSave() throws Exception {
        XmlObject x;
        XmlCursor cTo;

        //

        x = XmlObject.Factory.parse("<foo>abcdef</foo>");

        assertEquals("<foo>abcdef</foo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<foo>a&lt;b&amp;c</foo>");

        assertEquals("<foo>a&lt;b&amp;c</foo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<foo></foo>");

        cTo = navDoc(x, "dt");
        cTo.insertChars("&<");

        assertEquals("<foo>&amp;&lt;</foo>", x.xmlText());

        //

        x = XmlObject.Factory.parse("<foo><boo>bar</boo></foo>");

        cTo = navDoc(x, "dt");

        assertEquals("<boo>bar</boo>", cTo.xmlText());

        //

        x = XmlObject.Factory.parse("<foo><boo x=\"y\">bar</boo></foo>");

        cTo = navDoc(x, "dt");

        assertEquals("<boo x=\"y\">bar</boo>", cTo.xmlText());

        // Tests fragment saving and loading

        x = XmlObject.Factory.parse("<foo>Eric</foo>");

        cTo = navDoc(x, "dt");

        x = XmlObject.Factory.parse(cTo.xmlText());

        cTo = navDoc(x, "");

        assertEquals("Eric", cTo.getTextValue());

        // test save where I replace the name of an element

        x = XmlObject.Factory.parse("<foo>Eric</foo>");

        cTo = navDoc(x, "d");

        XmlOptions options = new XmlOptions();

        options.put(
            XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT,
            new QName(null, "bar"));

        x = XmlObject.Factory.parse(cTo.xmlText(options));

        cTo = navDoc(x, "");

        assertEquals("<bar>Eric</bar>", cTo.xmlText());

        // test save where I replace the name of the document

        x = XmlObject.Factory.parse("<foo>Eric</foo>");

        cTo = navDoc(x, "");

        options = new XmlOptions();

        options.put(
            XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT,
            new QName(null, "bar"));

        x = XmlObject.Factory.parse(cTo.xmlText(options));

        cTo = navDoc(x, "");

        assertEquals("<bar><foo>Eric</foo></bar>", cTo.xmlText());

        x = XmlObject.Factory.parse("<a xmlns='foo'/>");

        XmlCursor c = x.newCursor();

        c.toFirstContentToken();
        c.toFirstContentToken();

        c.insertElement("b");
        c.toPrevSibling();
        assertEquals("b", c.getName().getLocalPart());
        assertEquals(0, c.getName().getNamespaceURI().length());

        x = XmlObject.Factory.parse(x.xmlText());

        c = x.newCursor();

        c.toFirstContentToken();
        c.toFirstContentToken();

        assertEquals("b", c.getName().getLocalPart());
        assertEquals(0, c.getName().getNamespaceURI().length());
    }

    private void testTextFrag(String actual, String expected) {
        String pre = "<xml-fragment>";

        String post = "</xml-fragment>";

        assertTrue(actual.startsWith(pre));
        assertTrue(actual.endsWith(post));

        assertEquals(expected, actual.substring(
            pre.length(), actual.length() - post.length()));
    }

    @Test
    public void testSaveFrag() {
        XmlObject x;
        XmlCursor c;

        x = XmlObject.Factory.newInstance();

        c = x.newCursor();

        c.toNextToken();

        c.insertChars("Eric");

        testTextFrag(x.xmlText(), "Eric");

        //

        x = XmlObject.Factory.newInstance();

        c = x.newCursor();

        c.toNextToken();

        c.insertComment("");
        c.insertChars("x");

        testTextFrag(x.xmlText(), "<!---->x");

        //

        x = XmlObject.Factory.newInstance();

        c = x.newCursor();

        c.toNextToken();

        c.insertElement("foo");
        c.insertChars("x");

        testTextFrag(x.xmlText(), "<foo/>x");

        //

        x = XmlObject.Factory.newInstance();

        c = x.newCursor();

        c.toNextToken();

        c.insertElement("foo");
        c.insertElement("bar");

        testTextFrag(x.xmlText(), "<foo/><bar/>");
    }

    @Test
    public void testLoad() throws Exception {
        XmlObject x;

        XmlOptions options = new XmlOptions();

        options.put(XmlOptions.LOAD_REPLACE_DOCUMENT_ELEMENT, null);

        x =
            XmlObject.Factory.parse(
                "<bar p='q' x='y'>ab<foo>xy</foo>cd</bar>", options);

        XmlCursor c = navDoc(x, "t");

        assertSame(c.currentTokenType(), TokenType.ATTR);

        String open = "xmlns:open='http://www.openuri.org/fragment'";

        x =
            XmlObject.Factory.parse(
                "<open:fragment p='q' x='y' " + open +
                ">ab<foo>xy</foo>cd</open:fragment>");

        c = navDoc(x, "t");

        assertSame(c.currentTokenType(), TokenType.ATTR);
    }

    @Test
    public void testCompare() throws Exception {
        XmlObject x;
        XmlCursor cFrom, cTo;

        // Forward navigation 

        x = XmlObject.Factory.parse("<bar p='q' x='y'>ab<foo>xy</foo>cd</bar>");

        cFrom = navDoc(x, "");
        cTo = navDoc(x, "");

        for (; ; ) {
            assertEquals(0, cFrom.comparePosition(cTo));
            assertTrue(cFrom.isAtSamePositionAs(cTo));

            TokenType tt = cFrom.currentTokenType();

            if (tt == TokenType.ENDDOC) {
                break;
            } else if (tt == TokenType.TEXT) {
                cFrom.toNextChar(1);
                cTo.toNextChar(1);
            } else {
                cFrom.toNextToken();
                cTo.toNextToken();
            }
        }

        // Backward navigation 

        x = XmlObject.Factory.parse("<bar p='q' x='y'>ab<foo>xy</foo>cd</bar>");

        cFrom = navDoc(x, "r");
        cTo = navDoc(x, "r");

        for (; ; ) {
            assertEquals(0, cFrom.comparePosition(cTo));
            assertTrue(cFrom.isAtSamePositionAs(cTo));

            if (cFrom.toPrevChar(1) == 1)
                cTo.toPrevChar(1);
            else if (cFrom.toPrevToken() != TokenType.NONE)
                cTo.toPrevToken();
            else
                break;
        }

        //

        x = XmlObject.Factory.parse(
            "<bar p='q' x='y'>ab<foo>xy</foo>c<f y='x'>xy</f>d</bar>");

        cFrom = navDoc(x, "");

        for (; ; ) {
            boolean passed = false;

            cTo = navDoc(x, "");

            for (; ; ) {
                if (cTo.isAtSamePositionAs(cFrom)) {
                    assertTrue(!passed);
                    passed = true;
                } else if (cTo.isLeftOf(cFrom)) {
                    assertTrue(!passed);
                } else {
                    assertTrue(passed);
                    assertTrue(cTo.isRightOf(cFrom));
                }

                if (cTo.toNextChar(1) != 1)
                    if (cTo.toNextToken() == TokenType.ENDDOC)
                        break;
            }

            if (cFrom.toNextChar(1) != 1)
                if (cFrom.toNextToken() == TokenType.ENDDOC)
                    break;
        }
    }

    @Test
    public void testAttrSetter()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo/>");
        XmlCursor c = x.newCursor();
        c.toNextToken();
        c.setAttributeText(new QName(null, "x"), "hardehar");
        assertEquals("<foo x=\"hardehar\"/>", x.xmlText());
    }

    @Test
    public void testNavigation()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<a><x/><y/><z/></a>");
        XmlCursor c = x.newCursor();
        assertFalse(c.toNextSibling());
        assertFalse(c.toPrevSibling());
        assertFalse(c.toFirstAttribute());
        assertFalse(c.toLastAttribute());
        c.toNextToken();
        c.toNextToken();
        assertTrue(c.toNextSibling());
        assertEquals("y", c.getName().getLocalPart());
        assertTrue(c.toNextSibling());
        assertEquals("z", c.getName().getLocalPart());
        assertFalse(c.toNextSibling());

        x = XmlObject.Factory.parse("<a p='q' m='n'><x/><y/><z/></a>");
        c = x.newCursor();
        c.toNextToken();
        c.toNextToken();
        assertTrue(c.currentTokenType().isAttr());
        assertFalse(c.toPrevSibling());
        assertTrue(c.currentTokenType().isAttr());
        assertTrue(c.toNextSibling());
        assertEquals("x", c.getName().getLocalPart());

        c.toEndDoc();
        c.toPrevToken();
        assertTrue(c.toPrevSibling());
        assertEquals("z", c.getName().getLocalPart());
        assertTrue(c.toPrevSibling());
        assertEquals("y", c.getName().getLocalPart());
        assertTrue(c.toPrevSibling());
        assertEquals("x", c.getName().getLocalPart());
        assertFalse(c.toPrevSibling());

        c.toEndDoc();
        c.toPrevToken();
        assertTrue(c.toParent());
        assertEquals("a", c.getName().getLocalPart());

        c.toEndDoc();
        assertTrue(c.toParent());
        assertTrue(c.currentTokenType().isStartdoc());

        x = XmlObject.Factory.parse("<a>moo<!---->foo</a>");
        c = x.newCursor();
        c.toStartDoc();
        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        assertTrue(c.toParent());
        assertEquals("a", c.getName().getLocalPart());

        c.toStartDoc();
        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        assertTrue(c.toParent());
        assertEquals("a", c.getName().getLocalPart());

        c.toStartDoc();
        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        c.toNextToken();
        c.toNextChar(2);
        assertTrue(c.toParent());
        assertEquals("a", c.getName().getLocalPart());

        x = XmlObject.Factory.parse("<foo>early<bar>text<char>zap</char></bar></foo>");
        c = x.newCursor();
        c.toNextToken();
        c.toNextToken();
        assertTrue(c.toFirstChild());
        assertEquals("zap", c.getTextValue());
    }

    @Test
    public void testGetName()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<a x='y'>eric<!----><?moo?></a>");
        XmlCursor c = x.newCursor();
        assertNull(c.getName());
        assertTrue(!c.toNextToken().isNone());
        assertEquals("a", c.getName().getLocalPart());
        assertEquals(0, c.getName().getNamespaceURI().length());
        assertTrue(!c.toNextToken().isNone());
        assertEquals("x", c.getName().getLocalPart());
        assertEquals(0, c.getName().getNamespaceURI().length());
        assertTrue(!c.toNextToken().isNone());
        assertNull(c.getName());
        assertTrue(!c.toNextToken().isNone());
        assertNull(c.getName());
        assertTrue(!c.toNextToken().isNone());
        assertEquals("moo", c.getName().getLocalPart());
        assertEquals(0, c.getName().getNamespaceURI().length());
        assertTrue(!c.toNextToken().isNone());
        assertNull(c.getName());
        assertTrue(!c.toNextToken().isNone());
        assertNull(c.getName());
        assertTrue(c.toNextToken().isNone());
    }

    @Test
    public void testGetChars()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo>abcdefghijkl</foo>");
        XmlCursor c = x.newCursor();
        c.toNextToken();
        c.toNextToken();
        c.toNextChar(2);

        char[] buf = new char[3];
        int n = c.getChars(buf, 0, 400);

        assertEquals(3, n);
        assertEquals('c', buf[0]);
        assertEquals('d', buf[1]);
        assertEquals('e', buf[2]);
    }

    @Test
    public void testNamespaceSubstitution()
        throws Exception {
        HashMap<String, String> subs = new HashMap<String, String>();
        subs.put("foo", "moo");
        subs.put("a", "b");

        XmlOptions options = new XmlOptions();
        options.put(XmlOptions.LOAD_SUBSTITUTE_NAMESPACES, subs);

        XmlObject x =
            XmlObject.Factory.parse(
                "<a xmlns='foo' xmlns:a='a' a:x='y'/>",
                options);

        XmlCursor c = x.newCursor();

        c.toNextToken();
        assertEquals("moo", c.getName().getNamespaceURI());

        c.toNextToken();
        assertEquals("moo", c.getName().getNamespaceURI());

        c.toNextToken();
        assertEquals("b", c.getName().getNamespaceURI());

        c.toNextToken();
        assertEquals("b", c.getName().getNamespaceURI());
    }

    @Test
    public void testNamespaceInsertion()
        throws Exception {
        XmlObject x = XmlObject.Factory.newInstance();

        XmlCursor c = x.newCursor();

        c.toNextToken();
        c.insertElement("foo", "http://p.com");
        c.toPrevToken();
        c.insertNamespace("p", "http://p.com");

        assertEquals("<p:foo xmlns:p=\"http://p.com\"/>", x.xmlText());
    }

    private void dotestParser(String xml, String xmlResult)
        throws Exception {
        XmlObject x = XmlObject.Factory.parse(xml);
        String result = x.xmlText();
        assertEquals(xmlResult, result);
    }

    private void dotestParserErrors(String xml) {
        try {
            XmlObject.Factory.parse(xml);
        } catch (Throwable t) {
            return;
        }

        fail();
    }

    @Test
    public void testNil()
        throws Exception {
        XmlObject x = noNamespace.CanBeNilDocument.Factory.parse("<canBeNil/>");
        XmlCursor c = x.newCursor();
        c.toFirstChild();
        XmlObject fc = c.getObject();
        assertTrue(!fc.isNil());
        fc.setNil();
        assertTrue(fc.isNil());
        assertEquals("<canBeNil xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", x.xmlText());
        c.toNextToken();
        assertTrue(c.isAttr());
        c.removeXml();
        assertEquals("<canBeNil/>", x.xmlText());
        assertTrue(!fc.isNil());
    }

    @Test
    public void testParser()
        throws Exception {
        dotestParserErrors("<hee yee='five'><haw>66</haw></any>");
        dotestParserErrors("<foo></moo>");
        dotestParserErrors("<a><foo></moo></a>");
        dotestParserErrors("");
        dotestParserErrors("    ");

        dotestParserErrors("har");
        dotestParserErrors("<!-- comment -->");
        dotestParserErrors("<?moo?>");
        dotestParserErrors("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        dotestParserErrors("<a$/>");
        dotestParserErrors("<foo a='<'></foo>");
        dotestParserErrors("<foo a></foo>");
        dotestParserErrors("<foo>");
        dotestParserErrors("</foo>");
        dotestParserErrors( "<foo><!-- -- --></foo>" );
        dotestParserErrors( "<foo><!-- ---></foo>" );

        dotestParser("<a b=\"x\n\ny\"/>", "<a b=\"x  y\"/>");
    }

    @Test
    public void testSaxParser()
        throws Exception {
        String xml = "<a x='y'><!---->x<b/><c p='q'>z</c></a>";

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        InputSource is = new InputSource(new StringReader(xml));
        XmlSaxHandler sh = XmlObject.Factory.newXmlSaxHandler();

        xr.setFeature(
            "http://xml.org/sax/features/namespace-prefixes",
            true);

        xr.setFeature(
            "http://xml.org/sax/features/namespaces", true);

        xr.setFeature(
            "http://xml.org/sax/features/validation", false);

        xr.setContentHandler(sh.getContentHandler());

        xr.setProperty(
            "http://xml.org/sax/properties/lexical-handler",
            sh.getLexicalHandler());

        xr.parse(is);

        XmlObject x1 = sh.getObject();

        XmlObject x2 = XmlObject.Factory.parse(xml);

        assertEquals(x1.xmlText(), x2.xmlText());
    }

    @Test
    public void testAdditionalNamespaces()
        throws Exception {
        String xml = "<a xmlns:a='aNS'><a:b/></a>";

        Map<String, String> map = new java.util.LinkedHashMap<String, String>();
        map.put("b", "bNS");
        map.put("c", "cNS");
        map.put("a", "not-aNS");

        XmlOptions options = new XmlOptions();
        options.setLoadAdditionalNamespaces(map);

        XmlObject x = XmlObject.Factory.parse(xml, options);

        // 'a' prefix namespace is not remapped

        xml = "<a xmlns='aNS'><b/></a>";

        map = new java.util.LinkedHashMap<String, String>();
        map.put("b", "bNS");
        map.put("c", "cNS");
        map.put("", "not-aNS");

        options = new XmlOptions();
        options.setLoadAdditionalNamespaces(map);

        x = XmlObject.Factory.parse(xml, options);

        // default namespace is not remapped
        String expect = "<a xmlns=\"aNS\" xmlns:b=\"bNS\" xmlns:c=\"cNS\"><b/></a>";
        assertEquals(expect, x.xmlText());

    }


}
