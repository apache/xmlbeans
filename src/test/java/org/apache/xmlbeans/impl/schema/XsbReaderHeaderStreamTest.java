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

import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsbReaderHeaderStreamTest {

    // Records whether the resource stream handed to XsbReader was closed again.
    private static class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        TrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static SchemaTypeSystemImpl typeSystemServing(InputStream stream) throws Exception {
        SchemaTypeSystemImpl typeSystem = new SchemaTypeSystemImpl("test");

        Field loaderF = SchemaTypeSystemImpl.class.getDeclaredField("_resourceLoader");
        loaderF.setAccessible(true);
        loaderF.set(typeSystem, new ResourceLoader() {
            @Override
            public InputStream getResourceAsStream(String resourceName) {
                return stream;
            }

            @Override
            public void close() {
            }
        });

        return typeSystem;
    }

    private static byte[] header(int magic, int majorver, int minorver, int filetype) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(magic);
        dos.writeShort(majorver);
        dos.writeShort(minorver);
        dos.writeShort(0); // release number
        dos.writeShort(filetype);
        dos.flush();
        return bos.toByteArray();
    }

    private static void assertRejectedAndClosed(byte[] bytes) throws Exception {
        TrackingInputStream stream = new TrackingInputStream(bytes);
        SchemaTypeSystemImpl typeSystem = typeSystemServing(stream);

        assertThrows(SchemaTypeLoaderException.class,
            () -> new XsbReader(typeSystem, "h", SchemaTypeSystemImpl.FILETYPE_SCHEMAINDEX));
        assertTrue(stream.closed, "the rejected .xsb stream should have been closed");
    }

    @Test
    void closesStreamOnWrongMagicCookie() throws Exception {
        assertRejectedAndClosed(header(0xDEADBEEF, SchemaTypeSystemImpl.MAJOR_VERSION, SchemaTypeSystemImpl.MINOR_VERSION,
            SchemaTypeSystemImpl.FILETYPE_SCHEMAINDEX));
    }

    @Test
    void closesStreamOnWrongMajorVersion() throws Exception {
        assertRejectedAndClosed(header(SchemaTypeSystemImpl.DATA_BABE, SchemaTypeSystemImpl.MAJOR_VERSION + 1, SchemaTypeSystemImpl.MINOR_VERSION,
            SchemaTypeSystemImpl.FILETYPE_SCHEMAINDEX));
    }

    @Test
    void closesStreamOnWrongFileType() throws Exception {
        assertRejectedAndClosed(header(SchemaTypeSystemImpl.DATA_BABE, SchemaTypeSystemImpl.MAJOR_VERSION, SchemaTypeSystemImpl.MINOR_VERSION,
            SchemaTypeSystemImpl.FILETYPE_SCHEMATYPE));
    }

    @Test
    void closesStreamOnTruncatedStringPool() throws Exception {
        // a well-formed header followed by nothing - the string pool read hits EOF
        assertRejectedAndClosed(header(SchemaTypeSystemImpl.DATA_BABE, SchemaTypeSystemImpl.MAJOR_VERSION, SchemaTypeSystemImpl.MINOR_VERSION,
            SchemaTypeSystemImpl.FILETYPE_SCHEMAINDEX));
    }
}
