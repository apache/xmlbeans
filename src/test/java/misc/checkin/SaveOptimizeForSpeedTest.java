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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void testCDataEndInText() throws Exception {
        XmlObject o = XmlObject.Factory.parse("<root/>");
        try (XmlCursor cur = o.newCursor()) {
            cur.toFirstChild();
            cur.toFirstContentToken();
            cur.insertChars("a]]>b");
        }
        String out = saveForSpeed(o);
        // ']]>' is forbidden in element content and must be escaped to ']]&gt;'
        assertFalse(out.contains("]]>"));
        // before the fix the literal ']]>' makes this a fatal parse error
        XmlObject.Factory.parse(out);
    }

    @Test
    void testCDataEndAcrossChunkBoundary() throws Exception {
        // a ']]>' that straddles the 512-char chunk boundary: ']]' end the first
        // chunk, '>' is the first char of the second. The trailing-bracket state
        // has to carry across chunks or the '>' is emitted unescaped.
        String data = repeat('x', 510) + "]]>" + repeat('y', 600);
        XmlObject o = XmlObject.Factory.parse("<root/>");
        try (XmlCursor cur = o.newCursor()) {
            cur.toFirstChild();
            cur.toFirstContentToken();
            cur.insertChars(data);
        }
        String out = saveForSpeed(o);
        assertFalse(out.contains("]]>"));
        XmlObject.Factory.parse(out);
    }

    @Test
    void testBadCharInText() throws Exception {
        // an invalid XML 1.0 character (a C0 control) in element content. The
        // default saver replaces it with '?'; the speed path emitted it raw,
        // producing output that is not well-formed.
        XmlObject o = XmlObject.Factory.parse("<root/>");
        try (XmlCursor cur = o.newCursor()) {
            cur.toFirstChild();
            cur.toFirstContentToken();
            cur.insertChars("a\u0001b");
        }
        String out = saveForSpeed(o);
        assertFalse(out.indexOf('\u0001') >= 0);
        // before the fix the raw control char makes this a fatal parse error
        XmlObject.Factory.parse(out);
    }

    @Test
    void testCommentDashAtChunkBoundary() throws Exception {
        // a lone '-' sitting on the 512-char chunk boundary. It is legal in
        // comment content, but the speed path applied its trailing-dash fixup
        // at the end of every chunk, so the boundary '-' was silently rewritten
        // to a space and the saved comment no longer matched the source.
        String data = repeat('a', 511) + "-" + repeat('b', 600);
        XmlObject o = XmlObject.Factory.parse("<root><!--" + data + "--></root>");
        String out = saveForSpeed(o);
        XmlObject.Factory.parse(out);
        assertTrue(out.contains(repeat('a', 511) + "-"));
    }

    @Test
    void testProcInstTerminatorAcrossChunkBoundary() throws Exception {
        // a '?>' that straddles the 512-char chunk boundary: '?' is the last char
        // of the first chunk, '>' the first char of the second. The per-chunk
        // question-mark state used to reset between chunks, so the '>' escaped the
        // processing instruction and the trailing text broke out of the pi.
        String data = repeat('x', 511) + "?>" + repeat('y', 600);
        XmlObject o = XmlObject.Factory.parse("<root>z</root>");
        try (XmlCursor cur = o.newCursor()) {
            cur.toFirstChild();
            cur.toFirstContentToken();
            cur.insertProcInst("tgt", data);
        }
        String out = saveForSpeed(o);
        assertFalse(out.contains("?>" + repeat('y', 600)));
        // breakout produced text that is not allowed where the pi sat, so this throws before the fix
        XmlObject.Factory.parse(out);
    }
}
