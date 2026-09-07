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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolveHandleFileTypeTest {

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

    // A complete, readable .xsb header for a file type that resolveHandle does not
    // know how to load, followed by an empty string pool.
    private static byte[] pointerFile() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(SchemaTypeSystemImpl.DATA_BABE);
        dos.writeShort(SchemaTypeSystemImpl.MAJOR_VERSION);
        dos.writeShort(SchemaTypeSystemImpl.MINOR_VERSION);
        dos.writeShort(0); // release number
        dos.writeShort(SchemaTypeSystemImpl.FILETYPE_SCHEMAPOINTER);
        dos.writeShort(1); // string pool holding no entries
        dos.flush();
        return bos.toByteArray();
    }

    @Test
    void closesReaderOnUnhandledFileType() throws Exception {
        TrackingInputStream stream = new TrackingInputStream(pointerFile());

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

        assertThrows(IllegalStateException.class, () -> typeSystem.resolveHandle("h"));
        assertTrue(stream.closed, "the reader should have been closed before unwinding");
    }
}
