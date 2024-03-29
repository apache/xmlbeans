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

package xmlobject.schematypes.checkin;

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

public class GDateTests {

    private static final String[] validDurations = {
        "PT0S",
        "P1Y",
        "P1M",
        "P1D",
        "P2Y",
        "P2M",
        "P2D",
        "PT1H",
        "PT1M",
        "PT1S",
        "PT3600S",
        "P1Y1M1DT1H1M1S",
        "P1Y1M1DT1H1M1.1S",
        "P1Y365D",
        "P1Y366D",
        "PT0.1S",
        "P1M29D",

        "PT0.1415926S",
        "PT5233132S",
        "PT142332M",
        "PT98023H",

        "-PT0S",
        "-P1Y",
        "-P1M",
        "-P1D",
        "-PT1H",
        "-PT1M",
        "-PT1S",
        "-P1Y1M1DT1H1M1S",
        "-P1Y1M1DT1H1M1.1S",
        "-P27D",
        "-P28D",
        "-P29D",
        "-P30D",
        "-P31D",
        "-P364D",
        "-P365D",
        "-P366D",
        "-PT0.1S",
        "-PT0.1415926S",
        "-PT5233132S",
        "-PT142332M",
        "-PT98023H",

    };

    private static final String[] invalidDurations = {
        "P1Y-364D",
        "P1Y-365D",
        "P1Y-366D",
        "P1Y-367D",
        "P1Y-12M",
        "P1M-27D",
        "P1M-28D",
        "P1M-29D",
        "P1M-31D",
        "P1M-32D",
        "P1MT-660H",
        "P1MT-670H",
        "P1MT-680H",
        "P1MT-690H",
        "P1MT-700H",
        "P1MT-710H",
        "P1MT-720H",
        "P1MT-730H",
        "P1MT-740H",

        "-PT-0S",
        "-P-1Y",
        "-P-1M",
        "-P-1D",
        "-PT-1H",
        "-PT-1M",
        "-PT-1S",
        "-P1Y1M-1DT1H1M1S",
        "-P1Y1M1DT1H-1M1.1S",
        "-PT-0.1S",
        "-PT-0.1415926S",
    };

    private static final String[] invalidDates = {
        "+14:01", // tz
        "-14:01", // tz
        "+15:00", // tz
        "-15:00", // tz
        "X",
        ",",
        "T",
        "+",
        "-",
        ":",
        "0",
        "March 2",
        "T00:00:00Z", // T not allowed
        "-00:00:00Z", // - not allowed
        "96-02-28T00:00:00Z", // year width
        "100-02-28T00:00:00Z", // year width
        "1900-2-28T00:00:00Z", // month width
        "1900-02-8T00:00:00Z", // day width
        "1900-02-08T0:00:00Z", // hour width
        "1900-02-08T00:0:00Z", // hour width
        "1900-02-08T00:00:0Z", // hour width
        "1900-02-08T00:00Z", // time incomplete
        "1900-02-08 T00:00:00Z", // space
        "1900-02-08T 00:00:00Z", // space
        "1900-02-08T00:00:00 Z", // space
        "1900-02-29T00Z", // time incomplete
        "00:00", // time incomplete
        "00", // incomplete
        "2100-02-29", // not leap
        "-999999999-02-28T00:00:00Z", // too long ago
        "999999999-02-28T00:00:00Z", // too long from now
        "9999999999999999999999999999999-02-28T00:00:00Z", // overflow?
        "0000-01-01", // year zero
        "0000-12-31T04:35:22.456", // year zero
        "1996-00-28T00:00:00Z", // month
        "1996-13-28T00:00:00Z", // month
        "1996-02-00T00:00:00Z", // day
        "2000-02-30T00:00:00Z", // day
        "1996-02-29T25:00:00Z", // hr
        "1996-02-29T24:00:01Z", // hr
        "1996-02-29T00:60:00Z", // min
        "1996-02-29T00:00:60Z", // sec
        "1996-02-29T00:00:00+14:01", // tz
        "1996-02-29T00:00:00-14:01", // tz
        "1996-02-29T00:00:00+15:00", // tz
        "1996-02-29T00:00:00-15:00", // tz
        "1996-00-29", // month
        "1996-13-29", // month
        "1996-02-00", // day
        "2000-02-30", // day
        "--00", // month
        "--13", // month
        "--00-01", // month
        "--13-01", // month
        "--02-00", // day
        "--02-30", // day
        "--01-32", // day
        "--11-31", // day
        "---00", // day
        "---32", // day
        "25:00:00Z", // hr
        "24:01:00Z", // hr
        "00:60:00Z", // min
        "00:00:60Z", // sec
        "00:00:00+14:01", // tz
        "00:00:00-14:01", // tz
        "00:00:00+15:00", // tz
        "00:00:00-15:00", // tz
    };

    private static final String[] validDates = {
        "",
        "Z", // timezone only
        "-14:00", // timezone only
        "999999-12-31T23:59:59.999Z",
        "1970-12-31T23:59:59.990+14:00",
        "1996-02-29T00:00:00Z", // leap
        "2000-02-29T00:00:00Z", // leap
        "2004-02-29T00:00:00Z", // leap
        "2008-02-29T00:00:00Z", // leap
        "2012-02-29T00:00:00Z", // leap
        "1900-02-28T00:00:00Z", // not leap
        "2100-02-28T00:00:00Z", // not leap
        "1900-03-28T00:00:00Z", // not leap
        "-4712-01-01T00:00:00Z",
        "1999-01-01T00:00:00+01:00",
        "2001-12-31T23:59:59.010",
        "2001-12-31T23:59:59.999901000",
        "1999-12-31T23:59:59.1234567890-14:00",
        "1992-12-31T23:59:59.01-14:00",
        "1965-12-31T23:59:59.000Z",
        //"0000-12-31T04:35:22.456",
        "1696-09-01T00:00:00Z",
        "1697-02-01T00:00:00Z",
        "1903-03-01T00:00:00Z",
        "1903-07-01T00:00:00Z",
        "1696-09-01T00:00:00+00:00",
        "1697-02-01T00:00:00-00:00",
        "2002",
        "-0001",
        "--12",
        "-0001-11",
        "2002-01",
        "---31",
        "1554--31",
        "-0004--31",
        "--02-29",
        "--12-31",
        "2002-04-18",
        "-0423-12-31",
        "23:59:59",
        "00:00:00",
        "2010T23:59:59",
        "-0001T00:00:00",
        "--12T23:59:59",
        "-0001-11T00:00:00",
        "2011-01T23:59:59",
        "---31T00:00:00",
        "2002--31T23:59:59",
        "-0004--31T00:00:00",
        "2002-02-18T23:59:59",
        "-0423-12-31T00:00:00",
        "2002Z",
        "-0001+01:30",
        "--12-14:00",
        "-0001-11Z",
        "1970-12-01:00",
        "---31+01:30",
        "2002--31Z",
        "--03-31-03:00",
        "--04-30+05:00",
        "-0004--31-01:00",
        "2002-04-18-14:00",
        "-0423-12-31Z",
        "23:59:59-01:00",
        "00:00:00Z",
        "00:00:00+01:30",
        "1776T23:59:59-14:00",
        "-0001T00:00:00Z",
        "--12T23:59:59+01:30",
        "-0001-11T00:00:00-01:00",
        "2002-02T23:59:59Z",
        "---31T00:00:00-14:00",
        "2002--31T23:59:59-01:00",
        "-0004--31T00:00:00+01:30",
        "2002-04-18T23:59:59Z",
        "-0423-12-31T00:00:00-05:00",
        "1996-02-29T24:00:00Z", // 24:00:00 is valid
        "24:00:00Z",            // 24:00:00 is valid

    };

    private boolean hasTime(GDuration gd) {
        return gd.getHour() != 0 || gd.getMinute() != 0 || gd.getSecond() != 0 || gd.getFraction().signum() != 0;
    }

    @Test
    void testGregorianCalendar() {
        // this is a check of DST offsets
        Date date = new GDate("2002-04-18T23:59:59Z").getDate();
        GregorianCalendar gcal = new XmlCalendar(date);
        assertEquals(date, gcal.getTime());

        // now check out some things
        GDate gd = new GDate("2001-12-31T07:00:59.010");
        GregorianCalendar gc = gd.getCalendar();
        Date gdd = gd.getDate();
        Date gcd = gc.getTime();
        assertEquals(gdd, gcd);

        // set up 2/29, and read out Feb 29 in the year 1 BC.
        Calendar gregcal = new GDate("--02-29").getCalendar();
        assertEquals(29, gregcal.get(Calendar.DAY_OF_MONTH));
        assertEquals(2 - 1, gregcal.get(Calendar.MONTH));
        assertEquals(1, gregcal.get(Calendar.YEAR));
        assertEquals(0, gregcal.get(Calendar.ERA));
        // repeat some tests to make sure it's stable.
        assertEquals(29, gregcal.get(Calendar.DAY_OF_MONTH));
        assertEquals(2 - 1, gregcal.get(Calendar.MONTH));

        // now try some setters
        gregcal = new GDate("--02-29").getCalendar();
        gregcal.set(Calendar.MONTH, 10);
        assertEquals("--11-29", gregcal.toString());
        // repeat to ensure it's stable.
        assertEquals("--11-29", gregcal.toString());
    }

    private void _testEmptyDuration(GDuration gd) {
        assertTrue(gd.isValid());
        assertEquals("PT0S", gd.toString());
        assertEquals(0, gd.getYear());
        assertEquals(0, gd.getMonth());
        assertEquals(0, gd.getDay());
        assertEquals(0, gd.getHour());
        assertEquals(0, gd.getMinute());
        assertEquals(0, gd.getSecond());
        assertEquals(BigDecimal.ZERO, gd.getFraction());
    }

    @Test
    void testEmptyDuration() {
        GDuration gd = new GDuration();
        _testEmptyDuration(gd);
        GDuration gdCopy = new GDuration(gd);
        _testEmptyDuration(gdCopy);
    }

    @Test
    void testValidDuration() {
        for (String str : validDurations) {
            GDuration gd = new GDuration(str);

            assertEquals(str, gd.toString());

            for (String validDuration : validDurations) {
                GDuration gd2 = new GDuration(validDuration);

                // subtracting two ways works
                GDuration diff = gd.subtract(gd2);
                GDurationBuilder gdb = new GDurationBuilder(gd2);
                gdb.setSign(-gdb.getSign());
                gdb.addGDuration(gd);
                GDuration sum2 = gdb.toGDuration();
                assertEquals(0, diff.compareToGDuration(sum2));
                gdb.normalize();
                GDurationBuilder gdb1 = new GDurationBuilder(diff);
                gdb1.normalize();
                assertEquals(gdb.toString(), gdb1.toString(), "Problem: " + gd + " and " + gd2);

                // comparing is reversible
                int comp1 = gd.compareToGDuration(gd2);
                int comp2 = gd2.compareToGDuration(gd);
                if (comp1 == 2) {
                    assertEquals(2, comp2);
                } else {
                    assertEquals(-comp1, comp2);
                }

                // comparing translates to addition to dates
                boolean[] seen = new boolean[3];

                for (String validDate : validDates) {
                    GDate date = new GDate(validDate);
                    if (!date.hasDate() || date.getYear() > 99999 || date.getYear() < -4000) {
                        continue;
                    }
                    if ((hasTime(gd) || hasTime(gd2)) && !date.hasTime()) {
                        continue;
                    }
                    GDate date1 = date.add(gd);
                    GDate date2 = date.add(gd2);

                    comp2 = date1.compareToGDate(date2);
                    if (comp1 != 2) {
                        assertEquals(comp1, comp2, "Adding " + date + " + " + gd + " -> " + date1 + ", " + gd2 + " -> " + date2 + ", expecting " + comp1);
                    } else {
                        assertTrue(comp2 != 2);
                        seen[comp2 + 1] = true;
                    }

                    // subtraction should yield the same result
                    if (comp1 != 2) {
                        GDate date3 = date.add(diff);
                        assertEquals(comp1, date3.compareToGDate(date));
                    }
                }

                if (comp1 == 2) {
                    int seencount = 0;
                    for (boolean b : seen) {
                        if (b) {
                            seencount += 1;
                        }
                    }
                    assertTrue(seencount > 1, "Not ambiguous as advertised");
                }
            }
        }
    }

    private void _testAddAndSubtract(String date1, String date2,
                                     String duration) {
        GDate gd1 = new GDate(date1);
        GDate gd2 = new GDate(date2);
        GDuration gdur = new GDuration(duration);
        GDate gd = gd1.add(gdur);
        assertEquals(gd2, gd);
        gd = gd2.subtract(gdur);
        assertEquals(gd1, gd);
    }

    private void _testAdd(String date1, String date2, String duration) {
        GDate gd1 = new GDate(date1);
        GDate gd2 = new GDate(date2);
        GDuration gdur = new GDuration(duration);
        GDate gd = gd1.add(gdur);
        assertEquals(gd2, gd);
    }

    private void _testSubtract(String date1, String date2, String duration) {
        GDate gd1 = new GDate(date1);
        GDate gd2 = new GDate(date2);
        GDuration gdur = new GDuration(duration);
        GDate gd = gd2.subtract(gdur);
        assertEquals(gd1, gd);
    }

    @Test
    void testAddAndSubtractDuration() {
        _testAddAndSubtract("1970-01-01", "1973-01-01", "P3Y");
        _testAddAndSubtract("0001-01-01", "0004-01-01", "P3Y");
        // there is no year 0, so 1 BCE + 3Y = 3 CE
        _testAddAndSubtract("-0001-01-01", "0003-01-01", "P3Y");
        _testAddAndSubtract("-0002-01-01", "0003-01-01", "P4Y");
        _testAddAndSubtract("-0001-01-01", "0001-01-01", "P1Y");
        _testSubtract("-0001-02-29", "0004-02-29", "P4Y");
        _testSubtract("-0001-12-31", "0001-01-01", "P1D");
        _testSubtract("-0002-12-31", "0001-12-31", "P731D");
        _testAddAndSubtract("1970-01-01T00:00:00", "1973-02-01T01:30:45", "P3Y31DT1H30M45S");
        _testAddAndSubtract("-0001-01-01T00:00:00", "0003-02-01T01:30:45", "P3Y31DT1H30M45S");
        // addition and subtraction of duration is not necessarily symmetric
        // if duration is not constant, i.e., contains a component that varies
        // in length, such as  month (or year)
        _testAdd("2000-02-29", "2001-02-28", "P1Y");
        _testSubtract("2000-02-28", "2001-02-28", "P1Y");
        _testAddAndSubtract("1970-01-01T23:00:00", "1970-01-02T00:00:00", "PT1H");
        _testAddAndSubtract("1970-01-01T00:00:00", "1969-12-31T23:00:00", "-PT1H");
        _testAddAndSubtract("1970-01-01T00:00:00", "1969-12-31T22:59:59", "-PT1H1S");
        _testAddAndSubtract("1971-02-02T01:01:01.1", "1970-01-01T00:00:00", "-P1Y1M1DT1H1M1.1S");
        _testAdd("1970-01-01T00:00:00", "1968-11-29T22:58:58.9", "-P1Y1M1DT1H1M1.1S");
        _testSubtract("1969-12-31T00:00:00", "1968-11-29T22:58:58.9", "-P1Y1M1DT1H1M1.1S");
        _testAdd("0001-01-01T00:00:00", "-0002-11-29T22:58:58.9", "-P1Y1M1DT1H1M1.1S");
        _testSubtract("0001-01-01T00:00:00", "-0002-11-29T22:58:58.9", "-P1Y1M2DT1H1M1.1S");
    }

    @Test
    void testOrder() {
        assertEquals(-1, new GDate("1998-08-26").compareToGDate(new GDate("2001-08-06")));
        assertEquals(-1, new GDate("1970-12-20T04:14:22Z").compareToGDate(new GDate("1971-04-18T12:51:41Z")));
        assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-06Z")));
        assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-07+10:00")));
        assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-05-10:00")));
        assertEquals(2, new GDate("2001-02-28").compareToGDate(new GDate("2001-03-01+10:00")));
        assertEquals(2, new GDate("2001-03-01").compareToGDate(new GDate("2001-02-28-10:00")));
        assertEquals(-1, new GDate("2000-02-28").compareToGDate(new GDate("2000-03-01+10:00")));
        assertEquals(1, new GDate("2000-03-01").compareToGDate(new GDate("2000-02-28-10:00")));
        assertEquals(-1, new GDate("2001-08-06Z").compareToGDate(new GDate("2001-08-06-00:01")));
        assertEquals(0, new GDate("00:00:00Z").compareToGDate(new GDate("00:01:00+00:01")));
        assertEquals(0, new GDate("12:00:00-05:00").compareToGDate(new GDate("09:00:00-08:00")));
        assertEquals(-1, new GDate("09:00:00-05:00").compareToGDate(new GDate("09:00:00-08:00"))); // the east coast rises before the west
        assertEquals(-1, new GDate("2003-05-05T09:00:00-05:00").compareToGDate(new GDate("2003-05-05T09:00:00-08:00"))); // the east coast rises before the west
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31")));
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31+14:00")));
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31-14:00")));
        assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01")));
        assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01+14:00")));
        assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01-14:00")));
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02")));
        assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01")));
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02Z")));
        assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01Z")));
        assertEquals(2, new GDate("---02").compareToGDate(new GDate("---01-10:00")));
        assertEquals(2, new GDate("---01").compareToGDate(new GDate("---02+10:00")));
        assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01-09:00")));
        assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02+09:00")));
        assertEquals(0, new GDate("---01").compareToGDate(new GDate("---01")));
        assertEquals(-1, new GDate("2003").compareToGDate(new GDate("2004")));
        assertEquals(-1, new GDate("--11").compareToGDate(new GDate("--12")));
        assertEquals(-1, new GDate("2003-12").compareToGDate(new GDate("2004-01")));
        assertEquals(-1, new GDate("--11-30").compareToGDate(new GDate("--12-01")));
        assertEquals(-1, new GDate("--02-28").compareToGDate(new GDate("--02-29")));
        assertEquals(-1, new GDate("--02-29").compareToGDate(new GDate("--03-01")));
        assertEquals(2, new GDate("--02-29").compareToGDate(new GDate("--03-01+10:00")));
        assertEquals(2, new GDate("--02-28").compareToGDate(new GDate("--03-01+10:00")));
        assertEquals(2, new GDate("--03-01").compareToGDate(new GDate("--02-28-10:00")));
        assertEquals(2, new GDate("--03-01").compareToGDate(new GDate("--02-29-10:00")));
        assertEquals(-1, new GDate("--02-29").compareToGDate(new GDate("--03-01+09:00")));
        assertEquals(-1, new GDate("--02-28").compareToGDate(new GDate("--03-01+09:00")));
        assertEquals(1, new GDate("--03-01").compareToGDate(new GDate("--02-28-09:00")));
        assertEquals(1, new GDate("--03-01").compareToGDate(new GDate("--02-29-09:00")));
        assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59")));
        assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59+09:59")));
        assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:01+09:59")));
        assertEquals(2, new GDate("00:00:00").compareToGDate(new GDate("23:59:00+09:59")));
        assertEquals(2, new GDate("00:00:00").compareToGDate(new GDate("23:59:59+10:00")));
        assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59-14:00")));
        assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:00-09:59")));
        assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:58-09:59")));
        assertEquals(2, new GDate("23:59:59").compareToGDate(new GDate("00:00:59-09:59")));
        assertEquals(2, new GDate("23:59:59").compareToGDate(new GDate("00:00:00-10:00")));
        assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:00+14:00")));
    }

    @Test
    void testAPI() throws Exception {
        GDateBuilder builder = new GDateBuilder("1970-12-20T04:14:22Z");
        builder.normalizeToTimeZone(1, 0, 0);
        assertEquals("1970-12-20T04:14:22+00:00", builder.toString());
        builder.setTimeZone(615);
        assertEquals("1970-12-20T04:14:22+10:15", builder.toString());
        builder.setTimeZone(-345);
        assertEquals("1970-12-20T04:14:22-05:45", builder.toString());
        builder.normalizeToTimeZone(-300);
        assertEquals("1970-12-20T04:59:22-05:00", builder.toString());
    }

    @Test
    void testFailure() throws Exception {
        for (String str : invalidDurations) {
            assertThrows(IllegalArgumentException.class, () -> new GDuration(str));
        }

        for (String str : invalidDates) {
            assertThrows(IllegalArgumentException.class, () -> new GDate(str));
        }
    }

    @Test
    void testSuccess() throws Exception {
        for (String str : validDates) {
            GDate gdate = new GDate(str);

            // for 24h if hasDay must be normalized, else has the same representation
            if (str.contains("24:00:00") && gdate.hasDay()) {
                assertTrue(gdate.hasDay() && gdate.toString().contains("00:00:00"), str + " " + gdate.toString());
            } else {
                // must round-trip to string
                assertEquals(str, gdate.toString());
            }

            // must round-trip to GregorianCalendar if fractions-of-seconds <=3 digits
            if (gdate.getFraction() == null || gdate.getFraction().scale() <= 3)
                /* bug in gcal -> 03-01*/
                if (!gdate.toString().equals("--02-29")) {
                    GregorianCalendar gcal = gdate.getCalendar();
                    GDate gdate2 = new GDate(gcal);
                    assertEquals(gdate, gdate2);

                    // and if fractions-of-seconds is 3 digits, stringrep must round-trip
                    if (gdate.getFraction() == null || gdate.getFraction().scale() == 3 || gdate.getFraction().scale() == 0)
                        assertEquals(gdate.toString(), gdate2.toString());
                }

            // must round-trip to Date if absolute time+timezone + fractions-of-seconds <= 3
            if (gdate.hasTimeZone() && gdate.getYear() > -4000 && gdate.getYear() < 99999 && gdate.getBuiltinTypeCode() == SchemaType.BTC_DATE_TIME && gdate.getFraction().scale() <= 3) {
                Date date = gdate.getDate();
                GDate gdate2 = new GDate(date);
                assertEquals(gdate, gdate2);

                // normalize to UTC fractions-of-seconds is 0 or 3 digits [not 000], stringrep must round-trip
                if (gdate.getTimeZoneSign() == 0 && ((gdate.getFraction().scale() == 3 && gdate.getFraction().signum() != 0) || gdate.getFraction().scale() == 0)) {
                    GDateBuilder gdb = new GDateBuilder(gdate2);
                    gdb.normalizeToTimeZone(0, 0, 0);
                    assertEquals(gdate.toString(), gdb.toString());
                }

                // verify that going through gcal gives us the same thing
                GregorianCalendar gcal = gdate.getCalendar();
                assertEquals(date, gcal.getTime());

                // double-check XmlCalendar constructor
                gcal = new XmlCalendar(date);
                assertEquals(date, gcal.getTime(), "Doing " + gdate);
            } else if (gdate.hasDate() && (gdate.getFraction() == null || gdate.getFraction().scale() <= 3)) {
                // must be able to get a date if time+timezone is unset (midnight, ltz are assumed)
                Date date = gdate.getDate();
                GDateBuilder gdate1 = new GDateBuilder(gdate);
                if (!gdate1.hasTime())
                    gdate1.setTime(0, 0, 0, null);
                assertEquals(gdate1.getDate(), date);

                // verify that going through gcal gives us the same thing
                GregorianCalendar gcal = gdate.getCalendar();
                assertEquals(date, gcal.getTime(), "Comparing " + gdate + " and " + gcal);
            }
        }
    }

    @Test
    void test24hDates() {
        GDate d1 = new GDate("2004-03-31T24:00:00");
        assertEquals("2004-04-01T00:00:00", d1.toString());


        GDateBuilder b = new GDateBuilder();
        b.setTime(24, 0, 0, new BigDecimal("0.00"));
        assertEquals("24:00:00.000", b.getCalendar().toString());

        b.setDay(10);
        b.setMonth(1);
        assertEquals("--01-10T24:00:00.000", b.getCalendar().toString());

        b.setYear(2010);
        assertEquals("2010-01-10T24:00:00.000", b.getCalendar().toString());

        b.setDay(31);
        b.setMonth(Calendar.APRIL);
        b.setYear(2004);

        assertEquals("2004-04-01T00:00:00", b.canonicalString());
        assertEquals("2004-03-31T24:00:00.00", b.toString());
        assertEquals("2004-03-31T24:00:00.00", b.toGDate().toString());
        assertEquals("2004-04-01T00:00:00", b.toGDate().canonicalString());
        assertEquals("2004-03-31T24:00:00.000", b.toGDate().getCalendar().toString());


        GDateBuilder gdb = new GDateBuilder("24:00:00+01:00");
        assertEquals("24:00:00+01:00", gdb.toString());

        gdb.normalize();
        assertEquals("23:00:00Z", gdb.toString());
    }

    @Test
    void testYearStartingWith0() {
        GDate gdate = new GDate("0004-08-01");    //      00004-08-01 must fail
        assertEquals("0004-08-01", gdate.toString());

        String txt = "-9999-06";
        GDate d = new GDate(txt);
        assertEquals("-9999-06", d.toString());

        txt = "-12345-06";
        d = new GDate(txt);
        assertEquals(txt, d.toString());

        assertThrows(IllegalArgumentException.class, () -> new GDate("00004-08-01"));
        assertThrows(IllegalArgumentException.class, () -> new GDate("-012340-08-01"));
    }
}
