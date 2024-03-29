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

import org.apache.xmlbeans.XmlErrorCodes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class ErrorCodeTest {
    @Test
    void testCodes() throws Exception {
        // throws Exception if a duplicate error code value is found.
        Set<String> codes = readCodes();

        // throws Exception if a duplicate message property key is found.
        Properties messages = readMessages();

        // each message key should have an error code value
        Enumeration<Object> e = messages.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            // these properties are known not to have an error code
            if (key.equals("message.missing.resource") || key.equals("message.pattern.invalid")) {
                continue;
            }

            if (!codes.contains(key)) {
                throw new Exception("message.properties key '" + key + "' has no error code.");
            }
        }

        // each error code value should have a message key
        for (Object code : codes) {
            if (messages.get(code) == null) {
                throw new Exception("missing message.properties key for error code: " + code);
            }
        }

    }

    /**
     * Reads the field values of the public static final String fields of XmlErrorCodes.
     * Throws an Exception if a duplicate value is found.
     */
    private Set<String> readCodes() throws Exception {
        Set<String> result = new LinkedHashSet<>();

        Field[] fields = XmlErrorCodes.class.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();

            // only look at fields that are public static final
            if ((modifiers & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) == 0) {
                continue;
            }

            // only look at String fields
            if (field.getType() != String.class) {
                continue;
            }

            String value = (String) field.get(null);

            if (!result.add(value)) {
                throw new Exception("duplicate error code value in XmlErrorCodes: " + value + " in field " + field.getName());
            }
        }

        return result;
    }

    /**
     * Reads the message.properties file and throws an exception if a duplicate property key is found.
     */
    private Properties readMessages() throws Exception {
        class UniqueProperties extends Properties {
            public Object put(Object key, Object value) {
                Object old = super.put(key, value);
                if (old != null) {
                    throw new RuntimeException("duplicate property key '" + key + "' with value: " + value);
                }
                return null;
            }
        }

        Properties result = new UniqueProperties();
        result.load(XmlErrorCodes.class.getResourceAsStream("message.properties"));
        return result;
    }

}
