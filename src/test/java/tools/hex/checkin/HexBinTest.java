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
package tools.hex.checkin;

import java.util.Random;

import org.apache.xmlbeans.impl.util.HexBin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HexBinTest {
    @Test
    void testDecodeInvalid() {
        final byte[] bytes = {(byte)0xEF, (byte)0xBF};
        final byte[] result = HexBin.decode(bytes);
        assertNull(result);
    }

    @Test
    void testDecodeNull() {
        final byte[] result = HexBin.decode((byte[]) null);
        assertNull(result);
    }

    @Test
    void testDecodeEmpty() {
        final byte[] bytes = new byte[0];
        final byte[] result = HexBin.decode(bytes);
        assertArrayEquals(bytes, result);
    }

    @Test
    void testEncodeDecode() {
        Random rnd = new Random();
        final byte[] bytes = new byte[64];
        rnd.nextBytes(bytes);
        final byte[] encoded = HexBin.encode(bytes);
        final byte[] decoded = HexBin.decode(encoded);
        assertArrayEquals(bytes, decoded);
    }
}
