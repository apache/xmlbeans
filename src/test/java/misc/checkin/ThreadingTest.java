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

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import tools.util.JarUtil;

import javax.xml.namespace.QName;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;

public class ThreadingTest {

    public static final int THREAD_COUNT = 4;
    public static final int ITERATION_COUNT = 1;

    private static class CompilationThread extends Thread {
        private Throwable _throwable;
        private boolean _result;

        public Throwable getException() {
            return _throwable;
        }

        public boolean getResult() {
            return _result;
        }

        public void run() {
            try {
                for (int i = 0; i < ITERATION_COUNT; i++) {
                    SchemaTypeLoader loader = XmlBeans.loadXsd(jobj("xbean/misc/xmldsig-core-schema.xsd"));
                    File temp = JarUtil.getResourceFromJarasFile(
                            "xbean/misc/signature-example.xml");
                    XmlObject result = loader.parse(temp, null, null);
                    assertEquals(loader.findDocumentType(new QName(
                            "http://www.w3.org/2000/09/xmldsig#",
                            "Signature")), result.schemaType());
                }
                _result = true;
            }
            catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }
    }

    @Test
    void testThreadedCompilation() throws Throwable {
        CompilationThread[] threads = new CompilationThread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CompilationThread();
        }

        for (CompilationThread thread : threads) {
            thread.start();
        }

        for (CompilationThread thread : threads) {
            thread.join();
        }

        for (int i = 0; i < threads.length; i++) {
            assertNull(threads[i].getException());
            assertTrue(threads[i].getResult(), "Thread " + i + " didn't succeed");
        }
    }
}

