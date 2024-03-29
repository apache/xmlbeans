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
package misc.detailed;

import jira.xmlbeans177.TestListDocument;
import jira.xmlbeans177A.TestListADocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static common.Common.P;
import static common.Common.XBEAN_CASE_ROOT;
import static org.junit.jupiter.api.Assertions.*;

public class CharEscapeTest {
    private static final String inputFile = XBEAN_CASE_ROOT + P + "misc" + P + "jira" + P + "xmlbeans_177.xml";
    private static final String inputFile2 = XBEAN_CASE_ROOT + P + "misc" + P + "jira" + P + "xmlbeans_177a.xml";

    private static final String start = "<jira:testList xmlns:jira=\"http://jira/xmlbeans_177\">";
    private static final String end = "</jira:testList>";
    private static final String start2 = "<jira:testListA xmlns:jira=\"http://jira/xmlbeans_177a\">";
    private static final String end2 = "</jira:testListA>";

    @Test
    void testAddMapping() throws Exception {
        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        // a brand new map does not contain predefined entities
        assertNull(charEsc.getEscapedString('<'));
        assertNull(charEsc.getEscapedString('>'));
        assertNull(charEsc.getEscapedString('&'));
        assertNull(charEsc.getEscapedString('\''));
        assertNull(charEsc.getEscapedString('"'));

        // test predefined entities
        charEsc.addMapping('<', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        charEsc.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        charEsc.addMapping('&', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        charEsc.addMapping('\'', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        charEsc.addMapping('"', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        assertEquals("&lt;", charEsc.getEscapedString('<'));
        assertEquals("&gt;", charEsc.getEscapedString('>'));
        assertEquals("&amp;", charEsc.getEscapedString('&'));
        assertEquals("&apos;", charEsc.getEscapedString('\''));
        assertEquals("&quot;", charEsc.getEscapedString('"'));

        // additions can be overwritten
        charEsc.addMapping('<', XmlOptionCharEscapeMap.HEXADECIMAL);
        assertEquals("&#x3c;", charEsc.getEscapedString('<'));
        charEsc.addMapping('<', XmlOptionCharEscapeMap.DECIMAL);
        assertEquals("&#60;", charEsc.getEscapedString('<'));

        // test some other characters
        assertNull(charEsc.getEscapedString('A'));
        charEsc.addMapping('A', XmlOptionCharEscapeMap.DECIMAL);
        charEsc.addMapping('B', XmlOptionCharEscapeMap.HEXADECIMAL);
        assertEquals("&#65;", charEsc.getEscapedString('A'));
        assertEquals("&#x42;", charEsc.getEscapedString('B'));

        // non-xml entities cannot be escaped as predefined entities
        XmlException e = assertThrows(XmlException.class, () -> charEsc.addMapping('C', XmlOptionCharEscapeMap.PREDEF_ENTITY));
        String msg = "the PREDEF_ENTITY mode can only be used for the following characters: <, >, &, \" and '";
        assertTrue(e.getMessage().endsWith(msg));
    }

    @Test
    void testAddMappings() throws Exception {
        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        // non-xml entities cannot be escaped as predefined entities
        XmlException e = assertThrows(XmlException.class, () -> charEsc.addMappings('A', 'Z', XmlOptionCharEscapeMap.PREDEF_ENTITY));
        String msg = "the PREDEF_ENTITY mode can only be used for the following characters: <, >, &, \" and '";
        assertTrue(e.getMessage().endsWith(msg));

        // start char must be before end char
        e = assertThrows(XmlException.class, () -> charEsc.addMappings('a', 'Z', XmlOptionCharEscapeMap.HEXADECIMAL));
        msg = "ch1 must be <= ch2";
        assertTrue(e.getMessage().endsWith(msg));

        charEsc.addMappings('A', 'Z', XmlOptionCharEscapeMap.HEXADECIMAL);
        assertEquals("&#x41;", charEsc.getEscapedString('A'));
        assertEquals("&#x42;", charEsc.getEscapedString('B'));
        assertEquals("&#x43;", charEsc.getEscapedString('C'));
        assertEquals("&#x58;", charEsc.getEscapedString('X'));
        assertEquals("&#x59;", charEsc.getEscapedString('Y'));
        assertEquals("&#x5a;", charEsc.getEscapedString('Z'));

        // overwrite a mapping
        charEsc.addMapping('X', XmlOptionCharEscapeMap.DECIMAL);
        assertEquals("&#88;", charEsc.getEscapedString('X'));
        assertEquals("&#x59;", charEsc.getEscapedString('Y'));
        assertEquals("&#x5a;", charEsc.getEscapedString('Z'));
    }

    @Test
    void testEscape1() throws Exception {
        File f = new File(inputFile);
        TestListDocument doc = TestListDocument.Factory.parse(f);

        // default behavior: without the character replacement map,
        // only the minimal, required characters are escaped
        String exp1 =
            start + "\n" +
            "  <test>This is a greater than sign: ></test>\n" +
            "  <test>This is a less than sign: &lt;</test>\n" +
            "  <test>This is a single quote: '</test>\n" +
            "  <test>This is a double quote: \"</test>\n" +
            "  <test>W.L.Gore &amp; Associates</test>\n" +
            "  <test>Character data may not contain the three-character sequence ]]&gt; with the > unescaped.</test>\n" +
            "  <test>In particular, character data in a CDATA section may not contain the three-character sequence ]]&amp;gt; with the > unescaped.</test>\n" +
            end;
        assertEquals(exp1, doc.xmlText().replaceFirst("(?s)<!--.*-->", ""));

        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        charEsc.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        XmlOptions opts = new XmlOptions();
        opts.setSaveSubstituteCharacters(charEsc);

        // escape '>' as predefined entity as well
        String exp2 =
            start + "\n" +
            "  <test>This is a greater than sign: &gt;</test>\n" +
            "  <test>This is a less than sign: &lt;</test>\n" +
            "  <test>This is a single quote: '</test>\n" +
            "  <test>This is a double quote: \"</test>\n" +
            "  <test>W.L.Gore &amp; Associates</test>\n" +
            "  <test>Character data may not contain the three-character sequence ]]&gt; with the &gt; unescaped.</test>\n" +
            "  <test>In particular, character data in a CDATA section may not contain the three-character sequence ]]&amp;gt; with the &gt; unescaped.</test>\n" +
            end;
        assertEquals(exp2, doc.xmlText(opts).replaceFirst("(?s)<!--.*-->", ""));

        // escape block of chars as hexadecimal
        charEsc.addMappings('A', 'D', XmlOptionCharEscapeMap.HEXADECIMAL);
        // opts holds a reference to charEsc, so opts is updated
        String exp3 =
            start + "\n" +
            "  <test>This is a greater than sign: &gt;</test>\n" +
            "  <test>This is a less than sign: &lt;</test>\n" +
            "  <test>This is a single quote: '</test>\n" +
            "  <test>This is a double quote: \"</test>\n" +
            "  <test>W.L.Gore &amp; &#x41;ssociates</test>\n" +
            "  <test>&#x43;haracter data may not contain the three-character sequence ]]&gt; with the &gt; unescaped.</test>\n" +
            "  <test>In particular, character data in a &#x43;&#x44;&#x41;T&#x41; section may not contain the three-character sequence ]]&amp;gt; with the &gt; unescaped.</test>\n" +
            end;
        assertEquals(exp3, doc.xmlText(opts).replaceFirst("(?s)<!--.*-->", ""));
    }

    @Test
    void testEscape2() throws Exception {
        TestListDocument doc = TestListDocument.Factory.newInstance();
        TestListDocument.TestList testList = doc.addNewTestList();
        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        // define a block
        charEsc.addMappings('a', 'z', XmlOptionCharEscapeMap.DECIMAL);
        // overwrite
        charEsc.addMapping('x', XmlOptionCharEscapeMap.HEXADECIMAL);
        charEsc.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        // pi
        charEsc.addMapping('\u03c0', XmlOptionCharEscapeMap.HEXADECIMAL);
        XmlOptions opts = new XmlOptions();
        opts.setSaveSubstituteCharacters(charEsc);
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("http://jira/xmlbeans_177", "jira");
        opts.setSaveSuggestedPrefixes(prefixes);

        String[] testStrings = {"e < \u03c0", "\u03c0 > 3", "abcxyz"};
        testList.setTestArray(testStrings);

        String exp =
            start +
            "<test>&#101; &lt; &#x3c0;</test>" +
            "<test>&#x3c0; &gt; 3</test>" +
            "<test>&#97;&#98;&#99;&#x78;&#121;&#122;</test>" +
            end;
        assertEquals(exp, doc.xmlText(opts));
    }

    @Test
    void testEscapeAttribute() throws Exception {
        File f = new File(inputFile2);
        TestListADocument doc = TestListADocument.Factory.parse(f);

        // default behavior: without the character replacement map,
        // only the minimal, required characters are escaped
        String exp1 =
            start2 + "\n" +
            "  <test a=\"This is a greater than sign: >\"/>\n" +
            "  <test a=\"This is a less than sign: &lt;\"/>\n" +
            "  <test a=\"This is a single quote: '\"/>\n" +
            "  <test a=\"This is a double quote: &quot;\"/>\n" +
            "  <test a=\"W.L.Gore &amp; Associates\"/>\n" +
            end2;
        assertEquals(exp1, doc.xmlText().replaceFirst("(?s)<!--.*-->", ""));

        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        charEsc.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        XmlOptions opts = new XmlOptions();
        opts.setSaveSubstituteCharacters(charEsc);

        // escape '>' as predefined entity as well
        String exp2 =
            start2 + "\n" +
            "  <test a=\"This is a greater than sign: &gt;\"/>\n" +
            "  <test a=\"This is a less than sign: &lt;\"/>\n" +
            "  <test a=\"This is a single quote: '\"/>\n" +
            "  <test a=\"This is a double quote: &quot;\"/>\n" +
            "  <test a=\"W.L.Gore &amp; Associates\"/>\n" +
            end2;
        assertEquals(exp2, doc.xmlText(opts).replaceFirst("(?s)<!--.*-->", ""));

        // escape block of chars as hexadecimal
        charEsc.addMappings('A', 'D', XmlOptionCharEscapeMap.HEXADECIMAL);
        // opts holds a reference to charEsc, so opts is updated
        String exp3 =
            start2 + "\n" +
            "  <test a=\"This is a greater than sign: &gt;\"/>\n" +
            "  <test a=\"This is a less than sign: &lt;\"/>\n" +
            "  <test a=\"This is a single quote: '\"/>\n" +
            "  <test a=\"This is a double quote: &quot;\"/>\n" +
            "  <test a=\"W.L.Gore &amp; &#x41;ssociates\"/>\n" +
            end2;
        assertEquals(exp3, doc.xmlText(opts).replaceFirst("(?s)<!--.*-->", ""));
    }
}
