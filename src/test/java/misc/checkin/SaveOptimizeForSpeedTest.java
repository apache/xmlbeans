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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaveOptimizeForSpeedTest {

    // longer than the 512 char chunk OptimizedForSpeedSaver emits comment/pi text in
    private static final String LONG = repeat('a', 1500);

    private static String repeat(char c, int n) {
        char[] a = new char[n];
        Arrays.fill(a, c);
        return new String(a);
    }

    private static String saveForSpeed(XmlObject o) throws Exception {
        XmlOptions opts = new XmlOptions();
        opts.setSaveOptimizeForSpeed(true);
        StringWriter sw = new StringWriter();
        o.save(sw, opts);
        return sw.toString();
    }

    @Test
    void testLongCommentSavesForSpeed() throws Exception {
        XmlObject o = XmlObject.Factory.parse("<root><!--" + LONG + "--></root>");
        String out = saveForSpeed(o);
        XmlObject.Factory.parse(out);
        assertTrue(out.contains(LONG));
    }

    @Test
    void testLongProcInstSavesForSpeed() throws Exception {
        // pi text of 1024 chars or more spins forever before the fix
        String out = assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            XmlObject o = XmlObject.Factory.parse("<root><?tgt " + LONG + "?></root>");
            return saveForSpeed(o);
        });
        XmlObject.Factory.parse(out);
        assertTrue(out.contains(LONG));
    }
}
