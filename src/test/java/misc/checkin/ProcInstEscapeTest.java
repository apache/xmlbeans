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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ProcInstEscapeTest {

    private static XmlObject docWithProcInst(String piText) throws Exception {
        XmlObject o = XmlObject.Factory.parse("<root>x</root>");
        try (XmlCursor c = o.newCursor()) {
            c.toNextToken();
            c.toNextToken();
            c.insertProcInst("tgt", piText);
        }
        return o;
    }

    private static String saveForSpeed(XmlObject o) throws Exception {
        XmlOptions opts = new XmlOptions();
        opts.setSaveOptimizeForSpeed(true);
        StringWriter sw = new StringWriter();
        o.save(sw, opts);
        return sw.toString();
    }

    // a bad char immediately before "?>" used to make the default saver skip the
    // "?>" so it survived inside the processing instruction
    @Test
    void testBadCharBeforePiEnd() throws Exception {
        XmlObject o = docWithProcInst("\u0007?>");
        String out = o.xmlText();
        assertEquals("<root><?tgt ?? ?>x</root>", out);
        // the only "?>" in the output is the PI terminator
        assertFalse(out.substring(0, out.indexOf("?>x")).contains("?>"));
        // round-trips and both savers agree
        XmlObject.Factory.parse(out);
        assertEquals(out, saveForSpeed(o));
    }

    @Test
    void testPlainQuestionGtStillEscaped() throws Exception {
        XmlObject o = docWithProcInst("a?>b");
        assertEquals("<root><?tgt a? b?>x</root>", o.xmlText());
    }
}
