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

package xmlobject.detailed;

import org.junit.Assert;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


public class TypedSettersTests {
    private static final String schemaNs = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    private static final String instanceNs = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    private static String fmt(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (ch != '$') {
                sb.append(ch);
                continue;
            }

            ch = s.charAt(++i);

            String id = "";

            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                id = id + ch;
                ch = s.charAt(++i);
            }

            String arg = "";

            if (ch == '(') {
                ch = s.charAt(++i);

                while (ch != ')') {
                    arg += ch;
                    ch = s.charAt(++i);
                }
            } else
                i--;

            if (id.equals("schema"))
                sb.append(schemaNs);
            else if (id.equals("xsi"))
                sb.append(instanceNs);
            else if (id.equals("type")) {
                Assert.assertTrue(arg.length() > 0);
                sb.append("xsi:type=\"" + arg + "\"");
            } else
                Assert.fail();
        }

        return sb.toString();
    }

    private static final String nses = schemaNs + " " + instanceNs;

    @Test
    public void testJavaNoTypeSingletonElement()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<xyzzy/>");
        XmlObject x2 = XmlObject.Factory.parse("<bubba>moo</bubba>");
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c2.toNextToken();

        c.getObject().set(c2.getObject());

        assertEquals("<xyzzy>moo</xyzzy>", x.xmlText());
    }

    @Test
    public void testJavaNoTypeSingletonAttribute()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<xyzzy a=''/>");
        XmlObject x2 = XmlObject.Factory.parse("<bubba b='moo'/>");
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c.toNextToken();
        c2.toNextToken();
        c2.toNextToken();

        c.getObject().set(c2.getObject());

        assertEquals("<xyzzy a=\"moo\"/>", x.xmlText());
    }

    @Test
    public void testJavaNoTypeSingletonElementWithXsiType()
        throws Exception {
        XmlObject x = XmlObject.Factory.parse("<xyzzy/>", new XmlOptions()
            .setDocumentType(XmlObject.type));
        String input = fmt("<xml-fragment $type(xs:int) $xsi $schema>" +
            "69</xml-fragment>");
        //String input=
        XmlObject x2 = XmlObject.Factory.parse(input);


        assertSame(x2.schemaType(), XmlInt.type);

    }

}
