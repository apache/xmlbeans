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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaResourceManagerWriteTest {

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

    @Test
    void closesInputWhenTheTargetCannotBeOpened(@TempDir File dir) throws IOException {
        // a plain file where the target's parent directory should be, so opening
        // the output fails
        File blocker = new File(dir, "blocker");
        assertTrue(blocker.createNewFile());

        SchemaResourceManager manager = new SchemaResourceManager(dir);
        TrackingInputStream input =
            new TrackingInputStream("<xsd/>".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class,
            () -> manager.writeInputStreamToFile(input, "blocker/child.xsd"));
        assertTrue(input.closed, "the caller's stream should have been closed");
    }
}
