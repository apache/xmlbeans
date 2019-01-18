/*
 *   Copyright 2004 The Apache Software Foundation
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

package misc.detailed;

import org.apache.xmlbeans.impl.common.SystemCache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemCacheTests {
    @Test
    public void testSystemCacheImplFromAPITest() throws Throwable {
        // store the default SystemCache implementation before switch
        SystemCache defaultImpl = SystemCache.get();

        assertEquals("org.apache.xmlbeans.impl.common.SystemCache", defaultImpl.getClass().getName());

        // switch the Impl to the test Impl
        SystemCacheTestImpl testImpl = new SystemCacheTestImpl();
        SystemCache.set(testImpl);
        assertEquals("misc.detailed.SystemCacheTestImpl", testImpl.getClass().getName());
        assertEquals(testImpl.getAccessed(), 1);

        // switch back to default impl
        SystemCache.set(defaultImpl);
        assertEquals("org.apache.xmlbeans.impl.common.SystemCache", defaultImpl.getClass().getName());
    }

}
