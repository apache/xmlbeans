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

import java.nio.charset.StandardCharsets;

/**
 * format validation
 * <p>
 * This class encodes/decodes hexadecimal data
 */
public final class HexBin {
    static private final int BASELENGTH = 255;
    static private final int LOOKUPLENGTH = 16;
    static private final byte[] hexNumberTable = new byte[BASELENGTH];
    static private final byte[] lookUpHexAlphabet = new byte[LOOKUPLENGTH];


    static {
        for (int i = 0; i < BASELENGTH; i++) {
            hexNumberTable[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            hexNumberTable[i] = (byte) (i - '0');
        }
        for (int i = 'F'; i >= 'A'; i--) {
            hexNumberTable[i] = (byte) (i - 'A' + 10);
        }
        for (int i = 'f'; i >= 'a'; i--) {
            hexNumberTable[i] = (byte) (i - 'a' + 10);
        }

        for (int i = 0; i < 10; i++) {
            lookUpHexAlphabet[i] = (byte) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            lookUpHexAlphabet[i] = (byte) ('A' + i - 10);
        }
    }

    /**
     * byte to be tested if it is Base64 alphabet
     */
    static boolean isHex(byte octect) {
        // byte is a signed type and [] operator takes an int as an index.
        // If use `octect` directly, negative byte will be promoted to the
        // negative int and ArrayIndexOutOfBoundException will be thrown.
        // `& 0xFF` will convert negative byte to positive int
        return (hexNumberTable[octect & 0xFF] != -1);
    }

    /**
     * Converts bytes to a hex string
     */
    static public String bytesToString(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        return new String(encode(binaryData), StandardCharsets.ISO_8859_1);
    }

    /**
     * Converts a hex string to a byte array.
     */
    static public byte[] stringToBytes(String hexEncoded) {
        return decode(hexEncoded.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * array of byte to encode
     *
     * @return return encode binary array
     */
    static public byte[] encode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        int lengthEncode = lengthData * 2;
        byte[] encodedData = new byte[lengthEncode];
        for (int i = 0; i < lengthData; i++) {
            encodedData[i * 2] = lookUpHexAlphabet[(binaryData[i] >> 4) & 0xf];
            encodedData[i * 2 + 1] = lookUpHexAlphabet[binaryData[i] & 0xf];
        }
        return encodedData;
    }

    static public byte[] decode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        if (lengthData % 2 != 0) {
            return null;
        }

        int lengthDecode = lengthData / 2;
        byte[] decodedData = new byte[lengthDecode];
        for (int i = 0; i < lengthDecode; i++) {
            if (!isHex(binaryData[i * 2]) || !isHex(binaryData[i * 2 + 1])) {
                return null;
            }
            decodedData[i] = (byte) ((hexNumberTable[binaryData[i * 2]] << 4) | hexNumberTable[binaryData[i * 2 + 1]]);
        }
        return decodedData;
    }

    /**
     * Decodes Hex data into octects
     *
     * @param binaryData String containing Hex data
     * @return string containing decoded data.
     */
    public static String decode(String binaryData) {
        if (binaryData == null) {
            return null;
        }

        byte[] decoded = decode(binaryData.getBytes(StandardCharsets.ISO_8859_1));
        return decoded == null ? null : new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Encodes octects (using utf-8) into Hex data
     *
     * @param binaryData String containing Hex data
     * @return string containing decoded data.
     */
    public static String encode(String binaryData) {
        if (binaryData == null) {
            return null;
        }

        byte[] encoded = encode(binaryData.getBytes(StandardCharsets.UTF_8));
        return encoded == null ? null : new String(encoded, StandardCharsets.ISO_8859_1);
    }

}
