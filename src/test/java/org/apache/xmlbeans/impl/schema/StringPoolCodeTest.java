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
package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.apache.xmlbeans.impl.util.LongUTFDataInputStream;
import org.apache.xmlbeans.impl.util.LongUTFDataOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringPoolCodeTest {

    @Test
    void stringForCodeRejectsOutOfRange() {
        SchemaTypeSystemImpl.StringPool pool = new SchemaTypeSystemImpl.StringPool("handle", "name");
        int code = pool.codeForString("abc");

        assertNull(pool.stringForCode(0));
        assertEquals("abc", pool.stringForCode(code));

        // a code past the end of the pool, as read from a crafted .xsb, must
        // surface as the documented loader exception, not IndexOutOfBoundsException
        assertThrows(SchemaTypeLoaderException.class, () -> pool.stringForCode(code + 1));
    }

    @Test
    void stringForCodeRejectsNegative() {
        SchemaTypeSystemImpl.StringPool pool = new SchemaTypeSystemImpl.StringPool("handle", "name");
        // readUnsignedShortOrInt() falls back to a signed readInt() for the 0xffff
        // marker, so a negative code can reach stringForCode
        assertThrows(SchemaTypeLoaderException.class, () -> pool.stringForCode(-1));
    }

    @Test
    void readFromRejectsRepeatedEntry() throws IOException {
        // craft a string pool section that declares three entries but repeats a
        // string; codeForString hands back the earlier code, so code != i in the
        // populate loop, which must surface as the documented loader exception
        // rather than a bare IllegalStateException
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LongUTFDataOutputStream out = new LongUTFDataOutputStream(bos);
        out.writeShortOrInt(3);
        out.writeLongUTF("dup");
        out.writeLongUTF("dup");
        out.flush();

        SchemaTypeSystemImpl.StringPool pool = new SchemaTypeSystemImpl.StringPool("handle", "name");
        LongUTFDataInputStream in = new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertThrows(SchemaTypeLoaderException.class, () -> pool.readFrom(in));
    }
}
