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

import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.HexBinaryValue;
import org.apache.xmlbeans.impl.util.HexBin;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class HexBinTest {

    @Test
    public void testSurrogate() throws Exception {
        String exp = "ABC\ud83c\udf09123";
        String enc = HexBin.encode(exp);
        String dec = HexBin.decode(enc);
        assertEquals(exp, dec);
    }

    @Test
    public void knownValue() throws XPathException {
        // I've looked for comparison values but the following definition seems to be wrong,
        // because Saxon also returns the same encoded value
        // see http://books.xmlschemata.org/relaxng/ch19-77143.html
        // "Relax NG" by Eric van der Vlist

        String in = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>a";

        String exp = "3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d38223f3e61";
        String enc = HexBin.encode(in);

        HexBinaryValue val = new HexBinaryValue(enc);
        String saxIn = new String(val.getBinaryValue(), StandardCharsets.UTF_8);

        assertEquals(exp, enc.toLowerCase(Locale.ROOT));
        assertEquals(in, saxIn);
    }
}
