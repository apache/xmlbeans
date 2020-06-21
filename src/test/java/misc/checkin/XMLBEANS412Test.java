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
import org.junit.Test;

import java.util.StringTokenizer;

import static org.junit.Assert.fail;

public class XMLBEANS412Test {
    static String PassedPosCharGroups = "-,\\-,--,\\--,---,\\---,--\\-,\\--\\-,-\\--,\\-\\--,-a,\\-a,a-,"+
            "a\\-,a-b,a\\-b,a\\--,-a-z,\\-a-z,a-z-,a-z\\-,a-z\\-0-9,a\\-z-,a\\-z\\-,a\\-z\\-0-9,"+
            "-0-9,0-9-,0-9aaa,0-9a-,a-z\\--/,A-F0-9.+-,-A-F0-9.+,A-F0-9.+\\-,\\-A-F0-9.+";

    static String FailedPosCharGroups =  "[a--],[a-z-0-9],[a\\-z-0-9],[0-9--],[0-9a--],[0-9-a],[0-9-a-z]";
    static String MiscPassedPatterns = "([\\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(([a-zA-Z0-9_-])*\\.([a-zA-Z0-9_-])+)+";

    @Test
    public void testPassedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(PassedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }
    }

    @Test
    public void testNegatedPassedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(PassedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[^" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }
    }

    @Test
    public void testFailedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(FailedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern,"X");
            } catch (ParseException e) {
                continue;
            }
            fail("Pattern " + pattern + " did not fail.");
        }
    }

    @Test
    public void testNegatedFailedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(FailedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[^" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern,"X");
            } catch (ParseException e) {
                continue;
            }
            fail("Pattern " + pattern + " did not fail.");
        }
    }

    @Test
    public void testMiscPassedPatterns() {
        StringTokenizer tok = new StringTokenizer(MiscPassedPatterns,",");
        while (tok.hasMoreElements()) {
            String pattern = tok.nextToken();
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }
    }
}
