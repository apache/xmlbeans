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

package drtcases;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.XmlCalendar;

import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Calendar;

public class GDateTests extends TestCase
{
    public GDateTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(GDateTests.class); }

    static String[] validDurations =
            {
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
                "P1Y-364D",
                "P1Y-365D",
                "P1Y-366D",
                "P1Y-367D",
                "P1Y-12M",
                "P1M29D",
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

    static String[] invalidDates =
            {
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
                "-9999999-02-28T00:00:00Z", // too long ago
                "9999999-02-28T00:00:00Z", // too long from now
                "9999999999999999999999999999999-02-28T00:00:00Z", // overflow?
                "1996-00-28T00:00:00Z", // month
                "1996-13-28T00:00:00Z", // month
                "1996-02-00T00:00:00Z", // day
                "2000-02-30T00:00:00Z", // day
                "1996-02-29T24:00:00Z", // hr
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
                "24:00:00Z", // hr
                "00:60:00Z", // min
                "00:00:60Z", // sec
                "00:00:00+14:01", // tz
                "00:00:00-14:01", // tz
                "00:00:00+15:00", // tz
                "00:00:00-15:00", // tz
            };

    static String[] validDates =
            {
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
                "0000-12-31T04:35:22.456",
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
            };

    private static boolean hasTime(GDuration gd)
    {
        return gd.getHour() != 0 || gd.getMinute() != 0 || gd.getSecond() != 0 || gd.getFraction().signum() != 0;
    }
    
    public static void testGregorianCalendar()
    {
        // this is a check of DST offsets
        Date date = new GDate("2002-04-18T23:59:59Z").getDate();
        GregorianCalendar gcal = new XmlCalendar(date);
        Assert.assertEquals(date, gcal.getTime());

        // now check out some things
        GDate gd = new GDate("2001-12-31T07:00:59.010");
        GregorianCalendar gc = gd.getCalendar();
        Date gdd = gd.getDate();
        Date gcd = gc.getTime();
        Assert.assertEquals(gdd, gcd);
        
        // set up 2/29, and read out Feb 29 in the year 1 BC.
        Calendar gregcal = new GDate("--02-29").getCalendar();
        Assert.assertEquals(29, gregcal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(2 - 1, gregcal.get(Calendar.MONTH));
        Assert.assertEquals(1, gregcal.get(Calendar.YEAR));
        Assert.assertEquals(0, gregcal.get(Calendar.ERA));
        // repeat some tests to make sure it's stable.
        Assert.assertEquals(29, gregcal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(2 - 1, gregcal.get(Calendar.MONTH));

        // now try some setters
        gregcal = new GDate("--02-29").getCalendar();
        gregcal.set(Calendar.MONTH, 10);
        Assert.assertEquals("--11-29", gregcal.toString());
        // repeat to ensure it's stable.
        Assert.assertEquals("--11-29", gregcal.toString());
    }

    public static void testValidDuration()
    {
        for (int i = 0; i < validDurations.length; i++)
        {
            GDuration gd = null;
            String str = validDurations[i];
            try
            {
                gd = new GDuration(str);
            }
            catch (IllegalArgumentException e)
            {
                Assert.assertTrue("Problem with " + str + ": " + e.getMessage(), false);
            }

            Assert.assertEquals(str, gd.toString());

            for (int j = 0; j < validDurations.length; j++)
            {
                GDuration gd2 = null;
                String str2 = validDurations[j];
                try
                {
                    gd2 = new GDuration(str2);
                }
                catch (IllegalArgumentException e)
                {
                    Assert.assertTrue("Problem with " + str2 + ": " + e.getMessage(), false);
                }

                // subtracting two ways works
                GDuration diff = gd.subtract(gd2);
                GDurationBuilder gdb = new GDurationBuilder(gd2);
                gdb.setSign(-gdb.getSign());
                gdb.addGDuration(gd);
                GDuration sum2 = gdb.toGDuration();
                Assert.assertEquals(0, diff.compareToGDuration(sum2));
                gdb.normalize();
                GDurationBuilder gdb1 = new GDurationBuilder(diff);
                gdb1.normalize();
                Assert.assertEquals("Problem: " + gd + " and " + gd2, gdb.toString(), gdb1.toString());


                // comparing is reversible
                int comp1 = gd.compareToGDuration(gd2);
                int comp2 = gd2.compareToGDuration(gd);
                if (comp1 == 2)
                    Assert.assertEquals(2, comp2);
                else
                    Assert.assertEquals(-comp1, comp2);

                // comparing translates to addition to dates
                boolean[] seen = new boolean[3];

                for (int k = 0; k < validDates.length; k++)
                {
                    GDate date = new GDate(validDates[k]);
                    if (!date.hasDate() || date.getYear() > 99999 || date.getYear() < -4000)
                        continue;
                    if ((hasTime(gd) || hasTime(gd2)) && !date.hasTime())
                        continue;
                    // System.out.println("Adding " + gd + " and " + gd2 + " to " + date + ", expecting " + comp1);
                    GDate date1 = date.add(gd);
                    GDate date2 = date.add(gd2);

                    comp2 = date1.compareToGDate(date2);
                    if (comp1 != 2)
                    {
                        Assert.assertEquals("Adding " + date + " + " + gd + " -> " + date1 + ", " + gd2 + " -> " + date2 + ", expecting " + comp1, comp1, comp2);
                    }
                    else
                    {
                        Assert.assertTrue(comp2 != 2);
                        seen[comp2 + 1] = true;
                    }

                    // subtraction should yeild the same result
                    if (comp1 != 2)
                    {
                        GDate date3 = date.add(diff);
                        Assert.assertEquals(comp1, date3.compareToGDate(date));
                    }
                }

                if (comp1 == 2)
                {
                    int seencount = 0;
                    for (int k = 0; k < seen.length; k++)
                        if (seen[k])
                            seencount += 1;
                    Assert.assertTrue("Not ambiguous as advertised" /* + gd + ", " + gd2 + " d: " + diff */, seencount > 1);
                }
            }
        }
    }

    public static void testOrder()
    {
        Assert.assertEquals(-1, new GDate("1998-08-26").compareToGDate(new GDate("2001-08-06")));
        Assert.assertEquals(-1, new GDate("1970-12-20T04:14:22Z").compareToGDate(new GDate("1971-04-18T12:51:41Z")));
        Assert.assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-06Z")));
        Assert.assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-07+10:00")));
        Assert.assertEquals(2, new GDate("2001-08-06").compareToGDate(new GDate("2001-08-05-10:00")));
        Assert.assertEquals(2, new GDate("2001-02-28").compareToGDate(new GDate("2001-03-01+10:00")));
        Assert.assertEquals(2, new GDate("2001-03-01").compareToGDate(new GDate("2001-02-28-10:00")));
        Assert.assertEquals(-1, new GDate("2000-02-28").compareToGDate(new GDate("2000-03-01+10:00")));
        Assert.assertEquals(1, new GDate("2000-03-01").compareToGDate(new GDate("2000-02-28-10:00")));
        Assert.assertEquals(-1, new GDate("2001-08-06Z").compareToGDate(new GDate("2001-08-06-00:01")));
        Assert.assertEquals(0, new GDate("00:00:00Z").compareToGDate(new GDate("00:01:00+00:01")));
        Assert.assertEquals(0, new GDate("12:00:00-05:00").compareToGDate(new GDate("09:00:00-08:00")));
        Assert.assertEquals(-1, new GDate("09:00:00-05:00").compareToGDate(new GDate("09:00:00-08:00"))); // the east coast rises before the west
        Assert.assertEquals(-1, new GDate("2003-05-05T09:00:00-05:00").compareToGDate(new GDate("2003-05-05T09:00:00-08:00"))); // the east coast rises before the west
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31")));
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31+14:00")));
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---31-14:00")));
        Assert.assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01")));
        Assert.assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01+14:00")));
        Assert.assertEquals(1, new GDate("---31").compareToGDate(new GDate("---01-14:00")));
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02")));
        Assert.assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01")));
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02Z")));
        Assert.assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01Z")));
        Assert.assertEquals(2, new GDate("---02").compareToGDate(new GDate("---01-10:00")));
        Assert.assertEquals(2, new GDate("---01").compareToGDate(new GDate("---02+10:00")));
        Assert.assertEquals(1, new GDate("---02").compareToGDate(new GDate("---01-09:00")));
        Assert.assertEquals(-1, new GDate("---01").compareToGDate(new GDate("---02+09:00")));
        Assert.assertEquals(0, new GDate("---01").compareToGDate(new GDate("---01")));
        Assert.assertEquals(-1, new GDate("2003").compareToGDate(new GDate("2004")));
        Assert.assertEquals(-1, new GDate("--11").compareToGDate(new GDate("--12")));
        Assert.assertEquals(-1, new GDate("2003-12").compareToGDate(new GDate("2004-01")));
        Assert.assertEquals(-1, new GDate("--11-30").compareToGDate(new GDate("--12-01")));
        Assert.assertEquals(-1, new GDate("--02-28").compareToGDate(new GDate("--02-29")));
        Assert.assertEquals(-1, new GDate("--02-29").compareToGDate(new GDate("--03-01")));
        Assert.assertEquals(2, new GDate("--02-29").compareToGDate(new GDate("--03-01+10:00")));
        Assert.assertEquals(2, new GDate("--02-28").compareToGDate(new GDate("--03-01+10:00")));
        Assert.assertEquals(2, new GDate("--03-01").compareToGDate(new GDate("--02-28-10:00")));
        Assert.assertEquals(2, new GDate("--03-01").compareToGDate(new GDate("--02-29-10:00")));
        Assert.assertEquals(-1, new GDate("--02-29").compareToGDate(new GDate("--03-01+09:00")));
        Assert.assertEquals(-1, new GDate("--02-28").compareToGDate(new GDate("--03-01+09:00")));
        Assert.assertEquals(1, new GDate("--03-01").compareToGDate(new GDate("--02-28-09:00")));
        Assert.assertEquals(1, new GDate("--03-01").compareToGDate(new GDate("--02-29-09:00")));
        Assert.assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59")));
        Assert.assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59+09:59")));
        Assert.assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:01+09:59")));
        Assert.assertEquals(2, new GDate("00:00:00").compareToGDate(new GDate("23:59:00+09:59")));
        Assert.assertEquals(2, new GDate("00:00:00").compareToGDate(new GDate("23:59:59+10:00")));
        Assert.assertEquals(-1, new GDate("00:00:00").compareToGDate(new GDate("23:59:59-14:00")));
        Assert.assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:00-09:59")));
        Assert.assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:58-09:59")));
        Assert.assertEquals(2, new GDate("23:59:59").compareToGDate(new GDate("00:00:59-09:59")));
        Assert.assertEquals(2, new GDate("23:59:59").compareToGDate(new GDate("00:00:00-10:00")));
        Assert.assertEquals(1, new GDate("23:59:59").compareToGDate(new GDate("00:00:00+14:00")));
    }
    
    public static void testAPI() throws Exception
    {
        GDateBuilder builder = new GDateBuilder("1970-12-20T04:14:22Z");
        builder.normalizeToTimeZone(1, 0, 0);
        Assert.assertEquals("1970-12-20T04:14:22+00:00", builder.toString());
        builder.setTimeZone(615);
        Assert.assertEquals("1970-12-20T04:14:22+10:15", builder.toString());
        builder.setTimeZone(-345);
        Assert.assertEquals("1970-12-20T04:14:22-05:45", builder.toString());
        builder.normalizeToTimeZone(-300);
        Assert.assertEquals("1970-12-20T04:59:22-05:00", builder.toString());
    }


    public static void testFailure() throws Exception
    {
        for (int i = 0; i < invalidDates.length; i++)
        {
            String str = invalidDates[i];
            try
            {
                new GDate(str);
            }
            catch (IllegalArgumentException e)
            {
                continue;
            }
            Assert.assertTrue("Missing exception for GDate " + str, false);
        }
    }

    public static void testSuccess() throws Exception
    {
        for (int i = 0; i < validDates.length; i++)
        {
            String str = validDates[i];
            GDate gdate = null;
            try
            {
                gdate = new GDate(str);
            }
            catch (IllegalArgumentException e)
            {
                Assert.assertTrue("Problem with " + str + ": " + e.getMessage(), false);
            }

            // must round-trip to string
            Assert.assertEquals(str, gdate.toString());

            // must round-trip to GregorianCalendar if fractions-of-seconds <=3 digits
            if (gdate.getFraction() == null || gdate.getFraction().scale() <= 3)
                if (!gdate.toString().equals("--02-29")) // bug in gcal -> 03-01
            {
                GregorianCalendar gcal = gdate.getCalendar();
                GDate gdate2 = new GDate(gcal);
                Assert.assertEquals(gdate, gdate2);

                // and if fractions-of-seconds is 3 digits, stringrep must round-trip
                if (gdate.getFraction() == null || gdate.getFraction().scale() == 3 || gdate.getFraction().scale() == 0)
                    Assert.assertEquals(gdate.toString(), gdate2.toString());
            }

            // must round-trip to Date if absolute time+timezone + fractions-of-seconds <= 3
            if (gdate.hasTimeZone() && gdate.getYear() > -4000 && gdate.getYear() < 99999 && gdate.getBuiltinTypeCode() == SchemaType.BTC_DATE_TIME && gdate.getFraction().scale() <= 3)
            {
                Date date = gdate.getDate();
                GDate gdate2 = new GDate(date);
                Assert.assertEquals(gdate, gdate2);
                
                // normalize to UTC fractions-of-seconds is 0 or 3 digits [not 000], stringrep must round-trip
                if (gdate.getTimeZoneSign() == 0 && ((gdate.getFraction().scale() == 3 && gdate.getFraction().signum() != 0) || gdate.getFraction().scale() == 0))
                {
                    GDateBuilder gdb = new GDateBuilder(gdate2);
                    gdb.normalizeToTimeZone(0, 0, 0);
                    Assert.assertEquals(gdate.toString(), gdb.toString());
                }

                // verify that going through gcal gives us the same thing
                GregorianCalendar gcal = gdate.getCalendar();
                Assert.assertEquals(date, gcal.getTime());
                
                // double-check XmlCalendar constructor
                gcal = new XmlCalendar(date);
                Assert.assertEquals("Doing " + gdate, date, gcal.getTime());
            }
            else if (gdate.hasDate() && (gdate.getFraction() == null || gdate.getFraction().scale() <= 3))
            {
                // must be able to get a date if time+timezone is unset (midnight, ltz are assumed)
                Date date = gdate.getDate();
                GDateBuilder gdate1 = new GDateBuilder(gdate);
                if (!gdate1.hasTime())
                    gdate1.setTime(0, 0, 0, null);
                Assert.assertEquals(gdate1.getDate(), date);

                // verify that going through gcal gives us the same thing
                GregorianCalendar gcal = gdate.getCalendar();
                Assert.assertEquals("Comparing " + gdate + " and " + gcal, date, gcal.getTime());
            }
        }
    }
}
