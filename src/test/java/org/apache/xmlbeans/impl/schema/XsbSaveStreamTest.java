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

import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsbSaveStreamTest {

    // Stands in for a full disk or a revoked permission: the file opens, then every
    // write fails.
    private static class FailingOutputStream extends OutputStream {
        private boolean closed;

        @Override
        public void write(int b) throws IOException {
            throw new IOException("disk full");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            throw new IOException("disk full");
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    @Test
    void closesBinaryFileWhenTheWriteFails() throws Exception {
        FailingOutputStream stream = new FailingOutputStream();

        SchemaTypeSystemImpl typeSystem = new SchemaTypeSystemImpl("test");
        Field filerF = SchemaTypeSystemImpl.class.getDeclaredField("_filer");
        filerF.setAccessible(true);
        filerF.set(typeSystem, new Filer() {
            @Override
            public OutputStream createBinaryFile(String typename) {
                return stream;
            }

            @Override
            public Writer createSourceFile(String typename, String sourceCodeEncoding) {
                throw new UnsupportedOperationException();
            }
        });

        assertThrows(SchemaTypeLoaderException.class, () -> typeSystem.savePointerFile("p", "test"));
        assertTrue(stream.closed, "the abandoned .xsb output should have been closed");
    }
}
