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
package org.apache.xmlbeans.impl.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LongUTFDataInputStreamTest {

    @Test
    void readLongUTFRejectsNegativeLength() {
        // readUnsignedShortOrInt() falls back to a signed readInt() for the
        // 0xfffe marker, so the length can be negative. A crafted .xsb string
        // pool entry whose length prefix is FF FE followed by a negative int
        // must surface as a UTFDataFormatException, not NegativeArraySizeException.
        byte[] bytes = { (byte) 0xFF, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE };
        LongUTFDataInputStream in = new LongUTFDataInputStream(new ByteArrayInputStream(bytes));
        assertThrows(UTFDataFormatException.class, in::readLongUTF);
    }

    @Test
    void readLongUTFRoundTripsShortString() throws IOException {
        assertEquals("hello", roundTrip("hello"));
    }

    @Test
    void readLongUTFRoundTripsLargeStringViaEscape() throws IOException {
        // a value whose UTF byte length exceeds 65534 is written with the 0xfffe
        // escape and a full int length, so the length reaches readLongUTF through
        // readInt - the negative-length guard must not reject a valid large length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) {
            sb.append('a');
        }
        String big = sb.toString();
        assertEquals(big, roundTrip(big));
    }

    private static String roundTrip(String s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LongUTFDataOutputStream out = new LongUTFDataOutputStream(bos);
        out.writeLongUTF(s);
        out.flush();
        LongUTFDataInputStream in = new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return in.readLongUTF();
    }
}
