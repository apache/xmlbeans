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
package tools.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities to copy files, directories, etc.
 */
public class Util {
    /**
     * Helper to get the stack trace of an Exception as a String.
     *
     * @param t Use the stack trace of this exception.
     * @return The stack trace as a String.
     */
    public static String getStackTrace(Throwable t) {
        if (t == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Checks if a string is entirely whitespace
     */
    public static boolean isWhiteSpace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}


