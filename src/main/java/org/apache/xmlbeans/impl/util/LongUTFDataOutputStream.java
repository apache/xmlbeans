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

package org.apache.xmlbeans.impl.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class works around the size limitation of UTF strings (&lt; 64kb) of DataOutputStream
 * and needs to be used with LongUTFDataInputStream
 */
public class LongUTFDataOutputStream extends DataOutputStream {
    // MAX_UNSIGNED_SHORT - actually (+1) but for the magic value we use the reduced value
    static final int MAX_UNSIGNED_SHORT = Short.MAX_VALUE * 2;

    public LongUTFDataOutputStream(OutputStream out) {
        super(out);
    }

    public void writeShortOrInt(int value) throws IOException {
        writeShortOrInt(this, value);
    }

    public static void writeShortOrInt(DataOutputStream dos, int value) throws IOException {
        // there are two values (0xFFFE and 0xFFFF) which are incompatible to the older (writeShort)
        // implementation, i.e. if old schemas based on writeShort are processed
        if (value < MAX_UNSIGNED_SHORT) {
            dos.writeShort(value);
        } else {
            dos.writeShort(MAX_UNSIGNED_SHORT);
            dos.writeInt(value);
        }
    }

    /**
     * Checks the length of the to-be-written UTF-8 array, if the length is below 64k then
     * {@link DataOutputStream#writeUTF(String)} is called, otherwise a 4-byte (int) is injected to list/count
     * the appended UTF-8 bytes
     * @param str the string to be written as UTF8-modified
     */
    public void writeLongUTF(String str) throws IOException {
        // DataOutputStream allows only 64k chunks - see XMLBeans-235
        final int utfLen = countUTF(str);
        writeShortOrInt(utfLen);

        final byte[] bytearr = new byte[4096];
        final int strlen = str.length();
        int count = 0;
        for (int i=0; i < strlen; i++) {
            if (count >= bytearr.length-3) {
                write(bytearr, 0, count);
                count = 0;
            }
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else {
                if (c > 0x07FF) {
                    bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                    bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                } else {
                    bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                }
                bytearr[count++] = (byte) (0x80 | (c & 0x3F));
            }
        }
        write(bytearr, 0, count);
    }

    public static int countUTF(String str) {
        final int strlen = str.length();
        int count = 0;
        for (int i=0; i<strlen; i++) {
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                count += 1;
            } else if (c > 0x07FF) {
                count += 3;
            } else {
                count += 2;
            }
        }
        return count;
    }
}
