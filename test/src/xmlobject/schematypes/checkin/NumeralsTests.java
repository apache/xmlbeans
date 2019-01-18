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


import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openuri.testNumerals.DocDocument;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumeralsTests {
    private static DocDocument.Doc doc;

    @BeforeClass
    public static void initDoc() throws XmlException {
        String inst =
            "<doc xmlns='http://openuri.org/testNumerals'>\n" +
            "  <string>    this is a long string \n" +
            "  ...   </string>\n" +
            "  <int>\n" +
            "  		+5\n" +
            "  </int>\n" +
            "  <int>\n" +
            "  		-6\n" +
            "  </int>\n" +
            "  <int>\n" +
            "  		+00000000015\n" +
            "  </int>\n" +
            "  <int>7<!--this has to be a 77 int value-->7</int>\n" +
            "  <boolean>\n" +
            "  		true\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		false\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		0\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		1\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		true is not\n" +
            "  </boolean>\n" +
            "  <short>\n" +
            "  		+03\n" +
            "  </short>\n" +
            "  <byte>\n" +
            "  		+001\n" +
            "  </byte>\n" +
            "  <long>-0500000</long>\n" +
            "  <long>\n" +
            "    001\n" +
            "  </long>\n" +
            "  <long>\n" +
            "    +002\n" +
            "  </long>\n" +
            "  <double>\n" +
            "    +001\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    -002.007000\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    INF\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    -INF\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    NaN\n" +
            "  </double>\n" +
            "  <float>\n" +
            "    +12.325\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    NaN\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    INF\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    -INF\n" +
            "  </float>\n" +
            "  <decimal>\n" +
            "    +001.001\n" +
            "  </decimal>\n" +
            "  <integer>\n" +
            "    +001<!--comments-->000000000\n" +
            "  </integer>\n" +
            "</doc>";

        doc = DocDocument.Factory.parse(inst).getDoc();
    }

    @Test
    public void test1() {
        String s = "    this is a long string \n" + "  ...   ";
        assertEquals("String expected:\n'" + s + "'         actual:\n'" +
            doc.getStringArray()[0] + "'", s, doc.getStringArray()[0]);
    }

    @Test
    public void test2() {
        assertEquals("int expected:" + 5 + " actual:" + doc.getIntArray()[0], 5, doc.getIntArray()[0]);
    }

    @Test
    public void test3() {
        assertEquals("int expected:" + (-6) + " actual:" + doc.getIntArray()[1], doc.getIntArray()[1], -6);
    }

    @Test
    public void test4() {
        assertEquals("int expected:" + 15 + " actual:" + doc.getIntArray()[2], 15, doc.getIntArray()[2]);
    }

    @Test
    public void test5() {
        assertEquals("int expected:" + 77 + " actual:" + doc.getIntArray()[3], 77, doc.getIntArray()[3]);
    }

    @Test
    public void test6() {
        assertTrue(doc.getBooleanArray(0));
    }

    @Test
    public void test7() {
        assertFalse(doc.getBooleanArray(1));
    }

    @Test
    public void test8() {
        assertFalse(doc.getBooleanArray(2));
    }

    @Test
    public void test9() {
        assertTrue(doc.getBooleanArray(3));
    }

    @Test(expected = XmlValueOutOfRangeException.class)
    public void test10() {
        boolean b = doc.getBooleanArray()[4];
    }

    @Test
    public void test11() {
        assertEquals(3, doc.getShortArray()[0]);
    }

    @Test
    public void test12() {
        assertEquals(1, doc.getByteArray()[0]);
    }

    @Test
    public void test13() {
        assertEquals("long expected:" + (-50000) + " actual:" + doc.getLongArray()[0], doc.getLongArray()[0], -500000);
    }

    @Test
    public void test14() {
        assertEquals("long expected:" + 1 + " actual:" + doc.getLongArray()[1], 1, doc.getLongArray()[1]);
    }

    @Test
    public void test15() {
        assertEquals("long expected:" + 2 + " actual:" + doc.getLongArray()[2], 2, doc.getLongArray()[2]);
    }

    @Test
    public void test16() {
        assertEquals(1, doc.getDoubleArray()[0], 0.0);
    }

    @Test
    public void test17() {
        assertEquals("double expected:" + -2.007d + " actual:" + doc.getDoubleArray()[1], doc.getDoubleArray()[1], -2.007d, 0.0);
    }

    @Test
    public void test18() {
        assertEquals(Double.POSITIVE_INFINITY, doc.getDoubleArray()[2], 0);
    }

    @Test
    public void test19() {
        assertEquals(Double.NEGATIVE_INFINITY, doc.getDoubleArray()[3], 0);
    }

    @Test
    public void test20() {
        assertEquals(Double.NaN, doc.getDoubleArray()[4], 0);
    }

    @Test
    public void test21() {
        assertEquals("fload expected:" + 12.325f + " actual:" + doc.getFloatArray()[0], 12.325f, doc.getFloatArray()[0], 0.0);
    }

    @Test
    public void test22() {
        assertEquals(Float.NaN, doc.getFloatArray()[1], 0);
    }

    @Test
    public void test23() {
        assertEquals("fload expected:" + Float.POSITIVE_INFINITY + " actual:" + doc.getFloatArray()[2], Float.POSITIVE_INFINITY, doc.getFloatArray()[2], 0);
    }

    @Test
    public void test24() {
        assertEquals(Float.NEGATIVE_INFINITY, doc.getFloatArray()[3], 0);
    }

    @Test
    public void test25() {
        assertEquals(new BigDecimal("1.001"), doc.getDecimalArray()[0]);
    }

    @Test
    public void test26() {
        assertEquals(new BigInteger("1000000000"), doc.getIntegerArray(0));
    }
}
