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
package xmlcursor.xpath.complex.detailed;

import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlDuration;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlTime;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlDateTime;
import junit.framework.TestCase;

/**
 *
 */
public class TypesTest
    extends TestCase
{
    public void testDate()
    {
        res = o.selectPath("xs:date(\"2000-01-01\")");
        assertEquals(1, res.length);
        XmlDate d = ((XmlDate) res[0]);
        assertEquals(
            "<xml-fragment>Fri Dec 31 16:00:00 PST 1999</xml-fragment>",
            d.xmlText());
    }

    public void testDecimal()
    {
        res =
            o.selectPath(
                "seconds-from-dateTime(xs:dateTime('1997-07-16T19:20:30+01:00'))");
        assertEquals(1, res.length);
        XmlDecimal dec = ((XmlDecimal) res[0]);
        assertEquals("<xml-fragment>30</xml-fragment>", dec.xmlText());
    }

    //Saxon returns string here, though the string is a valid duration
    //representation
    public void testDuration() throws Exception
    {
        res = o.selectPath("xdt:dayTimeDuration(\"PT12H\")*4");
        assertEquals(1, res.length);
        XmlDuration test=XmlDuration.Factory.parse(res[0].xmlText());
        System.out.println(test.getGDurationValue().getDay());
        XmlDuration dec = ((XmlDuration) res[0]);
        assertEquals("", dec.xmlText());
    }


     public static void testTypes()
        throws Exception
    {
        XmlObject o = XmlObject.Factory.parse(
            "<a xmlns='abc'>foo<b>bar</b></a>");
        XmlObject[] res = null;

        //Long
        res = o.selectPath("hours-from-dateTime(" +
            "current-dateTime()) cast as xs:integer");
        assertEquals(1, res.length);
        XmlInt xi = ((XmlInt) res[0]);
        System.out.println(xi.xmlText());

        //Java type is string...
        res = o.selectPath("current-time()");
        assertEquals(1, res.length);
        XmlTime time = ((XmlTime) res[0]);
        System.out.println(time.xmlText());


        res = o.selectPath("subtract-dateTimes-yielding-dayTimeDuration(" +
            "current-dateTime()," +
            "current-dateTime())");
        assertEquals(1, res.length);
        XmlDuration dur = ((XmlDuration) res[0]);
        System.out.println(dur.xmlText());

        //Java type is long--is query rigth?
        res = o.selectPath("xs:byte(3)");
        assertEquals(1, res.length);
        XmlByte b = ((XmlByte) res[0]);
        System.out.println(b.xmlText());

        //Java type is string
        res = o.selectPath("base-uri()");
        assertEquals(1, res.length);
        XmlAnyURI u = ((XmlAnyURI) res[0]);
        System.out.println(u.xmlText());
        //java type is Date
        res = o.selectPath("current-dateTime()");
        assertEquals(1, res.length);
        XmlDateTime dt = ((XmlDateTime) res[0]);
        System.out.println(dt.xmlText());
    }
    public void setUp()
        throws Exception
    {
        o = XmlObject.Factory.parse("<a/>");
    }

    XmlObject o;
    XmlObject[] res;
}
