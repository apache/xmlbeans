/*   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaxNumberCharsValidateTest {

    // XmlOptions.DEFAULT_MAX_NUMBER_CHARS is 1024; a lexically valid number longer
    // than that trips the length cap in MathUtil.parseAsFloat/parseAsDouble, which
    // throws IllegalArgumentException rather than NumberFormatException.
    private static String tooLong(char c) {
        char[] cs = new char[1025];
        Arrays.fill(cs, c);
        return new String(cs);
    }

    private static boolean validates(String base, String value) throws Exception {
        String xsd =
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='urn:t' " +
            "targetNamespace='urn:t' elementFormDefault='qualified'>" +
            "  <xs:element name='root' type='" + base + "'/>" +
            "</xs:schema>";
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[]{SchemaDocument.Factory.parse(xsd)});
        XmlObject doc = loader.parse("<t:root xmlns:t='urn:t'>" + value + "</t:root>", null, null);
        return doc.validate();
    }

    @Test
    void overlongFloatIsReportedNotThrown() throws Exception {
        // used to escape validate() as java.lang.IllegalArgumentException
        assertFalse(validates("xs:float", tooLong('1')));
    }

    @Test
    void overlongDoubleIsReportedNotThrown() throws Exception {
        assertFalse(validates("xs:double", tooLong('1')));
    }

    @Test
    void normalFloatingPointValuesStillValidate() throws Exception {
        assertTrue(validates("xs:float", "1.5"));
        assertTrue(validates("xs:double", "-3.25E7"));
    }
}
