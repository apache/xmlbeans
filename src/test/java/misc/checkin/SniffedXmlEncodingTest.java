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

import org.apache.xmlbeans.impl.common.SniffedXmlInputStream;
import org.apache.xmlbeans.impl.common.SniffedXmlReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SniffedXmlEncodingTest {

    // An xml declaration pseudo-attribute ending in '=' with no value, only
    // whitespace up to the end of the sniffed buffer, used to index buf[-1]
    // while scanning for the opening quote. The sniffer must report "no
    // encoding found" instead of throwing ArrayIndexOutOfBoundsException.
    @Test
    void valuelessAttributeReader() throws Exception {
        for (String s : new String[]{"<?xml x=", "<?xml version=\"1.0\" encoding=  ", "<?xml a=   \t\n"}) {
            assertNull(new SniffedXmlReader(new StringReader(s)).getXmlEncoding());
        }
    }

    @Test
    void valuelessAttributeStream() throws Exception {
        byte[] b = "<?xml version=\"1.0\" encoding= ".getBytes(StandardCharsets.US_ASCII);
        assertEquals("UTF-8", new SniffedXmlInputStream(new ByteArrayInputStream(b)).getXmlEncoding());
    }

    @Test
    void wellFormedDeclarationStillDetected() throws Exception {
        byte[] b = "<?xml version='1.0' encoding='ISO-8859-1'?>".getBytes(StandardCharsets.US_ASCII);
        assertEquals("ISO-8859-1", new SniffedXmlInputStream(new ByteArrayInputStream(b)).getXmlEncoding());
    }
}
