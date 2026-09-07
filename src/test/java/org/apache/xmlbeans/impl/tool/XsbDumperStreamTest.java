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

package org.apache.xmlbeans.impl.tool;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsbDumperStreamTest {

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

    private static final PrintStream SINK = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    // A readable .xsb header of the given type, followed by an empty string pool and
    // nothing else - the body dump then runs off the end of the file.
    private static byte[] truncated(int filetype) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(XsbDumper.DATA_BABE);
        dos.writeShort(XsbDumper.MAJOR_VERSION);
        dos.writeShort(XsbDumper.MINOR_VERSION);
        dos.writeShort(0); // release number
        dos.writeShort(filetype);
        dos.writeShort(1); // string pool holding no entries
        dos.flush();
        return bos.toByteArray();
    }

    private static void assertClosedAfterFailedDump(int filetype) throws IOException {
        TrackingInputStream stream = new TrackingInputStream(truncated(filetype));

        assertThrows(IllegalStateException.class, () -> XsbDumper.dump(stream, "", SINK));
        assertTrue(stream.closed, "the dumped .xsb stream should have been closed");
    }

    @Test
    void closesStreamOnTruncatedIndex() throws IOException {
        assertClosedAfterFailedDump(XsbDumper.FILETYPE_SCHEMAINDEX);
    }

    @Test
    void closesStreamOnTruncatedType() throws IOException {
        assertClosedAfterFailedDump(XsbDumper.FILETYPE_SCHEMATYPE);
    }

    @Test
    void closesStreamOnTruncatedPointer() throws IOException {
        assertClosedAfterFailedDump(XsbDumper.FILETYPE_SCHEMAPOINTER);
    }
}
