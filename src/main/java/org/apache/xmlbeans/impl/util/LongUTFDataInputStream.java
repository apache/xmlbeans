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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

import static org.apache.xmlbeans.impl.util.LongUTFDataOutputStream.MAX_UNSIGNED_SHORT;

/**
 * This class works around the size limitation of UTF strings (&lt; 64kb) of DataInputStream
 * and needs to be used with LongUTFDataOutputStream
 */
public class LongUTFDataInputStream extends DataInputStream {
    public LongUTFDataInputStream(InputStream in) {
        super(in);
    }

    private interface IOCall {
        byte onebyte(int[] readBuf, int[] fillBuf, int[] readLen) throws IOException;
    }

    public int readUnsignedShortOrInt() throws IOException {
        return readUnsignedShortOrInt(this);
    }

    public static int readUnsignedShortOrInt(DataInputStream dis) throws IOException {
        int value = dis.readUnsignedShort();
        if (value == MAX_UNSIGNED_SHORT) {
            value = dis.readInt();
        }
        return value;
    }

    public String readLongUTF() throws IOException {
        final int utfLen = readUnsignedShortOrInt();
        StringBuilder sb = new StringBuilder(utfLen/2);
        final byte[] bytearr = new byte[4096];

        IOCall give = (readBuf, fillBuf, readLen) -> {
            if (readLen[0]+1 > utfLen) {
                throw new UTFDataFormatException("malformed input: partial character at end");
            }

            if (readBuf[0] >= fillBuf[0]) {
                fillBuf[0] = Math.min(bytearr.length, utfLen-readLen[0]);
                readFully(bytearr, 0, fillBuf[0]);
                readBuf[0] = 0;
            }
            readLen[0]++;
            return bytearr[readBuf[0]++];
        };

        final int[] readLen = { 0 }, readBuf = { 0 }, fillBuf = { 0 };
        while (readLen[0] < utfLen) {
            int c = (int)give.onebyte(readBuf, fillBuf, readLen) & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    sb.append((char)c);
                    break;
                case 12: case 13: {
                    /* 110x xxxx   10xx xxxx*/
                    int char2 = give.onebyte(readBuf, fillBuf, readLen);
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("malformed input around byte " + readLen[0]);
                    }
                    sb.append((char) (((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                }
                case 14: {
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    int char2 = give.onebyte(readBuf, fillBuf, readLen);
                    int char3 = give.onebyte(readBuf, fillBuf, readLen);

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException("malformed input around byte " + (readLen[0]-1));
                    }
                    sb.append((char) (((c & 0x0F) << 12) |
                                      ((char2 & 0x3F) << 6) |
                                      ((char3 & 0x3F))));
                    break;
                }
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException("malformed input around byte " + readLen[0]);
            }
        }
        return sb.toString();
    }
}

