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

package misc.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;
import org.junit.Test;
import tools.util.JarUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;


/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 19, 2003
 */
public class RichParserTests {
    @Test
    public void testPrimitiveTypes() throws Exception
    {
        XMLStreamReader xsr = XmlObject.Factory.parse(new FileInputStream(
                JarUtil.getResourceFromJarasFile("xbean/misc/primitiveTypes.xml"))).
                    newXMLStreamReader();
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext())
        {
            switch (xsrext.next())
            {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i<xsrext.getAttributeCount(); i++)
                    {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }

    private static final String[] strings = {"    this is a long string ... in attribute  ",
                                             "    this is a long string\n... in text  "};
    private static int stringsIdx = 0;
    private static final int[] ints = {5, -6, 15, 7, 2147483647, -2147483648, 5, -6, 15, 7, 2147483647, -2147483648};
    private static int intsIdx = 0;
    private static final boolean[] bools = {true, false, false, true, false, true, false, false, true, false};
    private static int boolsIdx = 0;
    private static final short[] shorts = {3, 3};
    private static int shortsIdx = 0;
    private static final byte[] bytes = {1, 1};
    private static int bytesIdx = 0;
    private static final long[] longs = {-500000, 1, 2, -500000, 1, 2};
    private static int longsIdx = 0;
    private static final double[] doubles = {1, -2.007, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 1, -2.007, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
    private static int doublesIdx = 0;
    private static final float[] floats = {12.325f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 12.325f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    private static int floatsIdx = 0;
    private static final QName[] qnames = { new QName("pre_uri", "local1"),
         new QName("local3"), new QName("pre_uri", "local1"),
         //new QName("default_uri", "local2"), new QName("default_uri", "local2"),
         new QName("local3")};
    private static int qnamesIdx = 0;

    private static void processText(String ln, XMLStreamReaderExt xs, int attIndex)
        throws XMLStreamException, IOException
    {
        if ("int".equals(ln))
        {
            int v = attIndex>-1 ? xs.getAttributeIntValue(attIndex) : xs.getIntValue();
            assertEquals("int " + v, ints[intsIdx++], v);
        }
        else if ("boolean".equals(ln))
        {
            boolean v = attIndex>-1 ? xs.getAttributeBooleanValue(attIndex) : xs.getBooleanValue();
            assertEquals("boolean " + v, bools[boolsIdx++], v);
        }
        else if ("short".equals(ln))
        {
            short v = attIndex>-1 ? xs.getAttributeShortValue(attIndex) : xs.getShortValue();
            assertEquals("short " + v, shorts[shortsIdx++], v);
        }
        else if ("byte".equals(ln))
        {
            byte v = attIndex>-1 ? xs.getAttributeByteValue(attIndex) : xs.getByteValue();
            assertEquals("byte " + v, bytes[bytesIdx++], v);
        }
        else if ("long".equals(ln))
        {
            long v = attIndex>-1 ? xs.getAttributeLongValue(attIndex) : xs.getLongValue();
            assertEquals("long " + v, longs[longsIdx++], v);
        }
        else if ("double".equals(ln))
        {
            double v = attIndex>-1 ? xs.getAttributeDoubleValue(attIndex) : xs.getDoubleValue();
            assertEquals("double expected: " + doubles[doublesIdx] + "  actual: " + v, new Double(doubles[doublesIdx++]), new Double(v));
            // makeing new Doubles because Double.NaN==Double.NaN is false;
        }
        else if ("float".equals(ln))
        {
            float v = attIndex>-1 ? xs.getAttributeFloatValue(attIndex) : xs.getFloatValue();
            assertEquals("float expected: " + floats[floatsIdx] + "  actual: " + v, new Float(floats[floatsIdx++]), new Float(v));
            // makeing new Floats because Float.NaN==Float.NaN is false;
        }
        else if ("decimal".equals(ln))
        {
            BigDecimal v = attIndex>-1 ? xs.getAttributeBigDecimalValue(attIndex) : xs.getBigDecimalValue();
            assertEquals("BigDecimal " + v, new BigDecimal("1.001"), v);
        }
        else if ("integer".equals(ln))
        {
            BigInteger v = attIndex>-1 ? xs.getAttributeBigIntegerValue(attIndex) : xs.getBigIntegerValue();
            assertEquals("BigInteger " + v, new BigInteger("1000000000"), v);
        }
        else if ("base64Binary".equals(ln))
        {
            InputStream v = attIndex>-1 ? xs.getAttributeBase64Value(attIndex) : xs.getBase64Value();
            String a = readIS(v);
            assertEquals("Base64Binary " + a, "base64Binary", a);
        }
        else if ("hexBinary".equals(ln))
        {
            InputStream v = attIndex>-1 ? xs.getAttributeHexBinaryValue(attIndex) : xs.getHexBinaryValue();
            String a = readIS(v);
            assertEquals("HexBinary " + a, "hexBinary", a);
        }
        else if ("date".equals(ln))
        {
            Calendar v = attIndex>-1 ? xs.getAttributeCalendarValue(attIndex) : xs.getCalendarValue();
            Calendar c = new XmlCalendar( "2001-11-26T21:32:52Z" );
            assertEquals("Calendar expected:" + c.getTimeInMillis() + " actual:" + v.getTimeInMillis(), c.getTimeInMillis(), v.getTimeInMillis());
        }
        else if ("dateTime".equals(ln))
        {
            Date v = attIndex>-1 ? xs.getAttributeDateValue(attIndex) : xs.getDateValue();
            Date d = new XmlCalendar("2001-11-26T21:32:52").getTime();
            assertEquals("Date expected:" + d + " actual:" + v, d, v);
        }
        else if ("gYearMonth".equals(ln))
        {
            GDate v = attIndex>-1 ? xs.getAttributeGDateValue(attIndex) : xs.getGDateValue();
            GDateBuilder gdb = new GDateBuilder();
            gdb.setYear(2001);
            gdb.setMonth(11);
            assertEquals("GDate expected:" + gdb + " actual:" + v, gdb.toGDate(), v);
        }
        else if ("duration".equals(ln))
        {
            GDuration v = attIndex>-1 ? xs.getAttributeGDurationValue(attIndex) : xs.getGDurationValue();
            GDurationBuilder gdb = new GDurationBuilder();
            gdb.setSign(-1);
            gdb.setSecond(7);
            assertEquals("GDuration expected:" + gdb + " actual:" + v, gdb.toGDuration(), v);
        }
        else if ("QName".equals(ln))
        {
            QName v = attIndex>-1 ? xs.getAttributeQNameValue(attIndex) : xs.getQNameValue();
            assertEquals("QName expected:" + qnames[qnamesIdx] + " actual:" + v, qnames[qnamesIdx++], v);
        }
        else if ("string".equals(ln))
        {
            String v = attIndex>-1 ? xs.getAttributeStringValue(attIndex) : xs.getStringValue();
            String s = strings[stringsIdx++];


            assertEquals("String expected:\n'" + s + "'         actual:\n'" + v + "'", s, v);
        }
    }

    public static String readIS(InputStream is)
        throws IOException
    {
        String res = "";
        byte[] buf = new byte[20];
        while (true)
        {
            int l = is.read(buf);
            if (l<0)
                break;
            res += new String(buf, 0, l);
        }
        return res;
    }

    public static void main(String[] args) throws IOException, XMLStreamException
    {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xsr = factory.createXMLStreamReader(new FileInputStream(new File(args[0])));
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext())
        {
            switch (xsrext.next())
            {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i<xsrext.getAttributeCount(); i++)
                    {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }
}
