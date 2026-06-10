/*   Copyright 2026 The Apache Software Foundation
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

import org.apache.xmlbeans.soap.SOAPArrayType;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SOAPArrayTypeTest {

    private static final QName ITEM = new QName("http://example.com", "int");

    @Test
    void parsesBracketlessDimensions() {
        SOAPArrayType t = new SOAPArrayType(ITEM, "2 3 4");
        assertArrayEquals(new int[]{2, 3, 4}, t.getDimensions());
    }

    @Test
    void parsesBracketlessDimensionsWithWildcard() {
        SOAPArrayType t = new SOAPArrayType(ITEM, "* 3");
        assertArrayEquals(new int[]{-1, 3}, t.getDimensions());
    }
}
