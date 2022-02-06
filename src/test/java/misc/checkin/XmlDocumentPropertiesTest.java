/*   Copyright 2019 The Apache Software Foundation
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

import org.apache.xmlbeans.XmlDocumentProperties;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlDocumentPropertiesTest {

    @Test
    void testSetStandalone() {
        XmlDocumentProperties props = new XmlDocumentProperties() {
            final HashMap<Object, Object> props = new HashMap<>();
            @Override
            public Object put ( Object key, Object value ) {
                return props.put(key, value);
            }

            @Override
            public Object get ( Object key ) {
                return props.get(key);
            }

            @Override
            public Object remove ( Object key ) {
                return props.remove(key);
            }
        };
        assertFalse(props.getStandalone());
        props.setStandalone(true);
        assertTrue(props.getStandalone());
        props.setStandalone(false);
        assertFalse(props.getStandalone());
    }

}
