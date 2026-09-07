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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaResourceManagerStreamTest {

    private static final String INDEX =
        "<dls:downloaded-schemas xmlns:dls='http://www.bea.com/2003/01/xmlbean/xsdownload'" +
        " defaultDirectory='/schemas'/>";

    private static final String XSD =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        " targetNamespace='http://example.org/ns' elementFormDefault='qualified'>" +
        "<xs:element name='root' type='xs:string'/>" +
        "</xs:schema>";

    /** Opens, then fails every read - a cached file that has become unreadable. */
    private static class FailingInputStream extends InputStream {
        private boolean closed;

        @Override
        public int read() throws IOException {
            throw new IOException("unreadable");
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            throw new IOException("unreadable");
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /**
     * Serves an unreadable stream for the cached .xsd. The field deliberately has no
     * initialiser: init() runs from the super constructor, before a subclass field
     * initialiser would.
     */
    private static class UnreadableXsdManager extends SchemaResourceManager {
        private FailingInputStream failing;

        UnreadableXsdManager(File dir) {
            super(dir);
        }

        @Override
        protected InputStream inputStreamForFile(String filename) throws IOException {
            if (filename.endsWith(".xsd")) {
                failing = new FailingInputStream();
                return failing;
            }
            return super.inputStreamForFile(filename);
        }

        @Override
        protected void warning(String message) {
            // keep the test output quiet
        }
    }

    @Test
    void closesTheDigestStreamWhenTheReadFails(@TempDir File dir) throws IOException {
        Files.write(new File(dir, "xsdownload.xml").toPath(), INDEX.getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "foo.xsd").toPath(), XSD.getBytes(StandardCharsets.UTF_8));

        UnreadableXsdManager manager = new UnreadableXsdManager(dir);

        // syncing digests every local .xsd that is not in the index; the IOException is
        // swallowed and the walk carries on to the next file
        manager.processAll(true, false, false);

        assertNotNull(manager.failing, "expected the cached .xsd to be digested");
        assertTrue(manager.failing.closed, "the digest stream should have been closed");
    }
}
