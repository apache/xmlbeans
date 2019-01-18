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

package xmlobject.detailed;

import org.junit.Test;
import tools.util.JarUtil;
import xmlobject.common.StringXmlReader;

import static org.junit.Assert.assertTrue;


public class TestXmlReader {

    /**
     * Tests read-only concurrency support in XML Beans
     * See Radar Bug: 33254
     */
    @Test
    public void testConcurrency() throws Exception {
        // Get the file contents
        String xmlFile = JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml");

        StringXmlReader rdr1 = new StringXmlReader(xmlFile);
        StringXmlReader rdr2 = new StringXmlReader(xmlFile);
        StringXmlReader rdr3 = new StringXmlReader(xmlFile);

        Thread t1 = new Thread(rdr1, "Reader1");
        Thread t2 = new Thread(rdr2, "Reader2");
        Thread t3 = new Thread(rdr3, "Reader3");
        t1.start();
        t2.start();
        t3.start();

        // Wait for threads to finish
        t1.join();
        t2.join();
        t3.join();

        // Check the status of the XmlReaders
        boolean status = rdr1.getStatus() & rdr2.getStatus() & rdr3.getStatus();

        assertTrue("Concurrency Test Failed.", status);
    }
}