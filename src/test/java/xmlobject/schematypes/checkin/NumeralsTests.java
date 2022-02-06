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
import org.junit.jupiter.api.Test;
import org.openuri.testNumerals.DocDocument;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class NumeralsTests {
    private static final String XML =
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

    @Test
    void testNumerals() throws XmlException {
        DocDocument.Doc doc = DocDocument.Factory.parse(XML).getDoc();
        assertEquals("    this is a long string \n  ...   ", doc.getStringArray()[0]);
        assertEquals(5, doc.getIntArray()[0]);
        assertEquals(-6, doc.getIntArray()[1]);
        assertEquals(15, doc.getIntArray()[2]);
        assertEquals(77, doc.getIntArray()[3]);
        assertTrue(doc.getBooleanArray(0));
        assertFalse(doc.getBooleanArray(1));
        assertFalse(doc.getBooleanArray(2));
        assertTrue(doc.getBooleanArray(3));
        assertThrows(XmlValueOutOfRangeException.class, () -> { boolean b = doc.getBooleanArray()[4]; });
        assertEquals(3, doc.getShortArray()[0]);
        assertEquals(1, doc.getByteArray()[0]);
        assertEquals(-500000, doc.getLongArray()[0]);
        assertEquals(1, doc.getLongArray()[1]);
        assertEquals(2, doc.getLongArray()[2]);
        assertEquals(1, doc.getDoubleArray()[0], 0.0);
        assertEquals(-2.007d, doc.getDoubleArray()[1], 0.0);
        assertEquals(Double.POSITIVE_INFINITY, doc.getDoubleArray()[2], 0);
        assertEquals(Double.NEGATIVE_INFINITY, doc.getDoubleArray()[3], 0);
        assertEquals(Double.NaN, doc.getDoubleArray()[4], 0);
        assertEquals(12.325f, doc.getFloatArray()[0], 0.0);
        assertEquals(Float.NaN, doc.getFloatArray()[1], 0);
        assertEquals(Float.POSITIVE_INFINITY, doc.getFloatArray()[2], 0);
        assertEquals(Float.NEGATIVE_INFINITY, doc.getFloatArray()[3], 0);
        assertEquals(new BigDecimal("1.001"), doc.getDecimalArray()[0]);
        assertEquals(new BigInteger("1000000000"), doc.getIntegerArray(0));
    }
}
