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

import org.apache.xmlbeans.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TypesTest {
    private XmlObject o;
    private XmlObject[] res;

    @Before
    public void setUp()
        throws Exception {
        o = XmlObject.Factory.parse("<a/>");
    }

    @Test
    public void testDate() {
        res = o.selectPath("xs:date(\"2000-01-01\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDate) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testZDate() {
        res = o.selectPath("xs:date(\"2000-01-01Z\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDate) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, d.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void testCaliforniaDate() {
        res = o.selectPath("xs:date(\"2000-01-01-08:00\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDate) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
        assertEquals((-8 * 60 * 60 * 1000), d.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void testDateTime() {
        res = o.selectPath("xs:dateTime(\"2000-01-01T15:03:06.123\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDateTime) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(3, d.get(Calendar.MINUTE));
        assertEquals(6, d.get(Calendar.SECOND));
        assertEquals(123, d.get(Calendar.MILLISECOND));
    }

    @Test
    public void testZDateTime() {
        res = o.selectPath("xs:dateTime(\"2000-01-01T15:03:06.123Z\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDateTime) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(3, d.get(Calendar.MINUTE));
        assertEquals(6, d.get(Calendar.SECOND));
        assertEquals(123, d.get(Calendar.MILLISECOND));
        assertEquals(0, d.get(Calendar.ZONE_OFFSET));
        assertEquals(0, d.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void testCaliforniaDateTime() {
        res = o.selectPath("xs:dateTime(\"2000-01-01T15:03:06.123-08:00\")");
        assertEquals(1, res.length);
        Calendar d = ((XmlDateTime) res[0]).getCalendarValue();
        assertEquals(2000, d.get(Calendar.YEAR));
        assertEquals(0, d.get(Calendar.MONTH));
        assertEquals(1, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(3, d.get(Calendar.MINUTE));
        assertEquals(6, d.get(Calendar.SECOND));
        assertEquals(123, d.get(Calendar.MILLISECOND));
        assertEquals((-8 * 60 * 60 * 1000), d.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void testDecimal() {
        res = o.selectPath(
            "seconds-from-dateTime(xs:dateTime('1997-07-16T19:20:30+01:00'))");
        assertEquals(1, res.length);
        XmlDecimal dec = ((XmlDecimal) res[0]);
        assertEquals("<xml-fragment>30</xml-fragment>", dec.xmlText());
    }

    //Saxon returns string here, though the string is a valid duration
    //representation
    @Test
    public void testDuration() {
        res = o.selectPath("xs:dayTimeDuration(\"PT12H\")*4");
        assertEquals(1, res.length);
        //System.out.println(res[0].schemaType());
        String s = res[0].xmlText();
        //System.out.println(s);
        int i = s.indexOf("(\"");
        int j = s.indexOf("\")");
        assertTrue(0 < i);
        assertTrue(i < j);
        String duration = s.substring(i + 2, j);
        //System.out.println(duration);
        GDurationSpecification gDur = new GDurationBuilder(duration);
        //System.out.println(gDur.getDay());
        assertEquals(2, gDur.getDay());
    }

    @Test
    public void testTypes() throws Exception {
        XmlObject o = XmlObject.Factory.parse(
            "<a xml:base='abc'>foo<b>bar</b></a>");
        XmlObject[] res = null;

        //Long
        res = o.selectPath("hours-from-dateTime(" +
                           "current-dateTime()) cast as xs:integer");
        assertEquals(1, res.length);
        //System.out.println(res[0].schemaType());
        XmlLong xl = ((XmlLong) res[0]);
        //System.out.println(xl.xmlText());

        //Java type is string...
        res = o.selectPath("current-time()");
        assertEquals(1, res.length);
        //System.out.println(res[0].schemaType());
        //System.out.println(res[0].xmlText());
        XmlTime time = XmlTime.Factory.parse(res[0].xmlText());
        System.out.println(time.xmlText());

        /*
        res = o.selectPath("subtract-dateTimes-yielding-dayTimeDuration(" +
            "current-dateTime()," +
            "current-dateTime())");
        assertEquals(1, res.length);
        XmlDuration dur = ((XmlDuration) res[0]);
        System.out.println(dur.xmlText());
        */
        //Java type is long--is query right?
        res = o.selectPath("xs:byte(3)");
        assertEquals(1, res.length);
        System.out.println(res[0].schemaType()); //xs:long
        //XmlByte b = ((XmlByte) res[0]);
        //System.out.println(b.xmlText());

        //Java type is string
        res = o.selectPath("base-uri(/a)");
        assertEquals(1, res.length);
        System.out.println(res[0].schemaType()); //xs:string
        XmlAnyURI u = XmlAnyURI.Factory.parse(res[0].xmlText());
        System.out.println(u.xmlText());

        //java type is Date
        res = o.selectPath("current-dateTime()");
        assertEquals(1, res.length);
        System.out.println(res[0].schemaType());
        XmlDateTime dt = ((XmlDateTime) res[0]);
        System.out.println(dt.xmlText());
    }

}
