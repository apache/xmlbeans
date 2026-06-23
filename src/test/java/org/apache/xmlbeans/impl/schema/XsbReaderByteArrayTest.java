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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.impl.util.LongUTFDataInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class XsbReaderByteArrayTest {

    /**
     * writeByteArray stores the length with writeShort, which accepts any
     * unsigned short (0..65535). readByteArray must read it back the same way.
     * A length above Short.MAX_VALUE used to come back negative and blew up in
     * new byte[len] with NegativeArraySizeException.
     */
    @Test
    void readsByteArrayLengthAboveShortMax() throws Exception {
        final int len = 40000; // > Short.MAX_VALUE, still a valid unsigned short

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(len); // same on-wire form as XsbReader.writeByteArray
        byte[] payload = new byte[len];
        for (int i = 0; i < len; i++) {
            payload[i] = (byte) (i & 0xff);
        }
        dos.write(payload);
        dos.flush();

        XsbReader reader = new XsbReader(new SchemaTypeSystemImpl("test"), "h");
        Field input = XsbReader.class.getDeclaredField("_input");
        input.setAccessible(true);
        input.set(reader, new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray())));

        byte[] result = reader.readByteArray();

        assertEquals(len, result.length);
        assertArrayEquals(payload, result);
    }
}
