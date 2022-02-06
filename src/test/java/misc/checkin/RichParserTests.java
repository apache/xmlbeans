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
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 19, 2003
 */
public class RichParserTests {
    @Test
    void testPrimitiveTypes() throws Exception {
        XMLStreamReader xsr = XmlObject.Factory.parse(new FileInputStream(
                JarUtil.getResourceFromJarasFile("xbean/misc/primitiveTypes.xml"))).
            newXMLStreamReader();
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext()) {
            switch (xsrext.next()) {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i < xsrext.getAttributeCount(); i++) {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }

    private static final String[] strings = {
        "    this is a long string ... in attribute  ",
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
    private static final QName[] qnames = {new QName("pre_uri", "local1"),
        new QName("local3"), new QName("pre_uri", "local1"),
        //new QName("default_uri", "local2"), new QName("default_uri", "local2"),
        new QName("local3")};
    private static int qnamesIdx = 0;

    private static void processText(String ln, XMLStreamReaderExt xs, int attIndex)
        throws XMLStreamException, IOException {
        switch (ln) {
            case "int": {
                int v = attIndex > -1 ? xs.getAttributeIntValue(attIndex) : xs.getIntValue();
                assertEquals(ints[intsIdx++], v, "int " + v);
                break;
            }
            case "boolean": {
                boolean v = attIndex > -1 ? xs.getAttributeBooleanValue(attIndex) : xs.getBooleanValue();
                assertEquals(bools[boolsIdx++], v, "boolean " + v);
                break;
            }
            case "short": {
                short v = attIndex > -1 ? xs.getAttributeShortValue(attIndex) : xs.getShortValue();
                assertEquals(shorts[shortsIdx++], v, "short " + v);
                break;
            }
            case "byte": {
                byte v = attIndex > -1 ? xs.getAttributeByteValue(attIndex) : xs.getByteValue();
                assertEquals(bytes[bytesIdx++], v, "byte " + v);
                break;
            }
            case "long": {
                long v = attIndex > -1 ? xs.getAttributeLongValue(attIndex) : xs.getLongValue();
                assertEquals(longs[longsIdx++], v, "long " + v);
                break;
            }
            case "double": {
                double v = attIndex > -1 ? xs.getAttributeDoubleValue(attIndex) : xs.getDoubleValue();
                assertEquals(doubles[doublesIdx], v, 0.0, "double expected: " + doubles[doublesIdx] + "  actual: " + v);
                doublesIdx++;
                // makeing new Doubles because Double.NaN==Double.NaN is false;
                break;
            }
            case "float": {
                float v = attIndex > -1 ? xs.getAttributeFloatValue(attIndex) : xs.getFloatValue();
                assertEquals(floats[floatsIdx], v, 0.0f, "float expected: " + floats[floatsIdx] + "  actual: " + v);
                floatsIdx++;
                // makeing new Floats because Float.NaN==Float.NaN is false;
                break;
            }
            case "decimal": {
                BigDecimal v = attIndex > -1 ? xs.getAttributeBigDecimalValue(attIndex) : xs.getBigDecimalValue();
                assertEquals(new BigDecimal("1.001"), v, "BigDecimal " + v);
                break;
            }
            case "integer": {
                BigInteger v = attIndex > -1 ? xs.getAttributeBigIntegerValue(attIndex) : xs.getBigIntegerValue();
                assertEquals(new BigInteger("1000000000"), v, "BigInteger " + v);
                break;
            }
            case "base64Binary": {
                InputStream v = attIndex > -1 ? xs.getAttributeBase64Value(attIndex) : xs.getBase64Value();
                String a = readIS(v);
                assertEquals("base64Binary", a, "Base64Binary " + a);
                break;
            }
            case "hexBinary": {
                InputStream v = attIndex > -1 ? xs.getAttributeHexBinaryValue(attIndex) : xs.getHexBinaryValue();
                String a = readIS(v);
                assertEquals("hexBinary", a, "HexBinary " + a);
                break;
            }
            case "date": {
                Calendar v = attIndex > -1 ? xs.getAttributeCalendarValue(attIndex) : xs.getCalendarValue();
                Calendar c = new XmlCalendar("2001-11-26T21:32:52Z");
                assertEquals(c.getTimeInMillis(), v.getTimeInMillis(), "Calendar expected:" + c.getTimeInMillis() + " actual:" + v.getTimeInMillis());
                break;
            }
            case "dateTime": {
                Date v = attIndex > -1 ? xs.getAttributeDateValue(attIndex) : xs.getDateValue();
                Date d = new XmlCalendar("2001-11-26T21:32:52").getTime();
                assertEquals(d, v, "Date expected:" + d + " actual:" + v);
                break;
            }
            case "gYearMonth": {
                GDate v = attIndex > -1 ? xs.getAttributeGDateValue(attIndex) : xs.getGDateValue();
                GDateBuilder gdb = new GDateBuilder();
                gdb.setYear(2001);
                gdb.setMonth(11);
                assertEquals(gdb.toGDate(), v, "GDate expected:" + gdb + " actual:" + v);
                break;
            }
            case "duration": {
                GDuration v = attIndex > -1 ? xs.getAttributeGDurationValue(attIndex) : xs.getGDurationValue();
                GDurationBuilder gdb = new GDurationBuilder();
                gdb.setSign(-1);
                gdb.setSecond(7);
                assertEquals(gdb.toGDuration(), v, "GDuration expected:" + gdb + " actual:" + v);
                break;
            }
            case "QName": {
                QName v = attIndex > -1 ? xs.getAttributeQNameValue(attIndex) : xs.getQNameValue();
                assertEquals(qnames[qnamesIdx], v, "QName expected:" + qnames[qnamesIdx] + " actual:" + v);
                qnamesIdx++;
                break;
            }
            case "string": {
                String v = attIndex > -1 ? xs.getAttributeStringValue(attIndex) : xs.getStringValue();
                String s = strings[stringsIdx];
                stringsIdx++;
                assertEquals(s, v, "String expected:\n'" + s + "'         actual:\n'" + v + "'");
                break;
            }
        }
    }

    public static String readIS(InputStream is)
        throws IOException {
        String res = "";
        byte[] buf = new byte[20];
        while (true) {
            int l = is.read(buf);
            if (l < 0) {
                break;
            }
            res += new String(buf, 0, l);
        }
        return res;
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xsr = factory.createXMLStreamReader(new FileInputStream(new File(args[0])));
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext()) {
            switch (xsrext.next()) {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i < xsrext.getAttributeCount(); i++) {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }
}
