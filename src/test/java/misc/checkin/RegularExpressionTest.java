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

import org.apache.xmlbeans.impl.regex.ParseException;
import org.apache.xmlbeans.impl.regex.RegularExpression;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegularExpressionTest {

    @Test
    void testLongString() {
        RegularExpression regex = new RegularExpression("[A-Z0-9]+");
        String rnd = randomString(10000);
        assertTrue(regex.matches(rnd));
    }

    @Test
    void testLookbehindRangeAtInputEnd() {
        // a lookbehind containing a character class, evaluated at the end of the
        // input, used to read one character past the string in the RANGE op and
        // throw StringIndexOutOfBoundsException
        assertTrue(new RegularExpression("(?<=[a-c])$").matches("abc"));
        assertTrue(new RegularExpression(".*(?<=[0-9])").matches("ab9"));
        // the same off-by-one read also returned the wrong match result: the char
        // before the lookbehind is 'x', not in [a-c], so this must not match
        assertFalse(new RegularExpression("x(?<=[a-c])").matches("xc"));
    }

    @Test
    void testHexEscapeOverflowErrorKey() {
        // a \v escape (6 hex digits) whose value is above U+10FFFF was reported with
        // the error key "parser.descappe.4", which is not in the message bundle, so
        // ResourceBundle.getString threw MissingResourceException instead of the
        // ParseException callers expect. The sibling \x{...} path already reports the
        // same condition cleanly via "parser.descape.4".
        assertThrows(ParseException.class, () -> new RegularExpression("\\v110000"));
        assertThrows(ParseException.class, () -> new RegularExpression("\\vFFFFFF"));
        assertThrows(ParseException.class, () -> new RegularExpression("\\x{110000}"));
        // the top of the Unicode range is still valid and must parse
        new RegularExpression("\\v10FFFF");
    }

    @Test
    void testQuantifierOverflow() {
        // a {min,max} count larger than Integer.MAX_VALUE overflowed the int
        // accumulator. the only guard was a post-multiply min<0/max<0 check, so
        // counts that wrapped to a non-negative value slipped through: "a{4294967296}"
        // parsed as "a{0}" (matched the empty string) and "a{1,4294967298}" as "a{1,2}",
        // while bigger ones such as "a{99999999999}" blew the heap at match time.
        assertThrows(ParseException.class, () -> new RegularExpression("a{4294967296}"));
        assertThrows(ParseException.class, () -> new RegularExpression("a{4294967297}"));
        assertThrows(ParseException.class, () -> new RegularExpression("a{99999999999}"));
        assertThrows(ParseException.class, () -> new RegularExpression("a{1,4294967298}"));
        // counts up to Integer.MAX_VALUE are representable and must still parse
        new RegularExpression("a{2147483647}");
        new RegularExpression("a{0,2147483647}");
        assertTrue(new RegularExpression("a{2,4}").matches("aaa"));
    }


    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random rnd = new Random();

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }
}
