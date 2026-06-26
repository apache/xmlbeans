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

import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.apache.xmlbeans.impl.util.LongUTFDataInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class XsbReaderHandleTest {

    // Builds an XsbReader whose next readString() returns the given component
    // handle, exactly as a crafted/corrupt .xsb would supply it.
    private static XsbReader readerForHandle(String handle) throws Exception {
        XsbReader reader = new XsbReader(new SchemaTypeSystemImpl("test"), "h");

        Field poolF = XsbReader.class.getDeclaredField("_stringPool");
        poolF.setAccessible(true);
        SchemaTypeSystemImpl.StringPool pool = (SchemaTypeSystemImpl.StringPool) poolF.get(reader);
        int code = pool.codeForString(handle);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(code); // readString -> readUnsignedShortOrInt -> stringForCode(code)
        dos.flush();

        Field inputF = XsbReader.class.getDeclaredField("_input");
        inputF.setAccessible(true);
        inputF.set(reader, new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray())));

        return reader;
    }

    @Test
    void rejectsEmptyHandle() throws Exception {
        // an empty handle used to throw StringIndexOutOfBoundsException at handle.charAt(0)
        XsbReader reader = readerForHandle("");
        assertThrows(SchemaTypeLoaderException.class, reader::readHandle);
    }

    @Test
    void rejectsShortUnderscoreHandle() throws Exception {
        // "_X" starts with '_' but is too short for handle.charAt(2)
        XsbReader reader = readerForHandle("_X");
        assertThrows(SchemaTypeLoaderException.class, reader::readHandle);
    }
}
