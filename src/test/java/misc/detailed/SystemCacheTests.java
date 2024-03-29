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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SystemCacheTests {
    @Test
    @Disabled("invalid test")
    public void testSystemCacheImplFromAPITest() {
        // Test ignored:
        // this test is dangerous if we eventually execute tests in parallel and
        // also the premise that the SystemCache instance is the default is wrong,
        // as SchemaTypeLoaderImpl overrides the SystemCache

        // store the default SystemCache implementation before switch
        SystemCache defaultImpl = SystemCache.get();

        assertEquals("org.apache.xmlbeans.impl.common.SystemCache", defaultImpl.getClass().getName());

        // switch the Impl to the test Impl
        SystemCacheTestImpl testImpl = new SystemCacheTestImpl();
        SystemCache.set(testImpl);
        assertEquals("misc.detailed.SystemCacheTestImpl", testImpl.getClass().getName());
        assertEquals(SystemCacheTestImpl.getAccessed(), 1);

        // switch back to default impl
        SystemCache.set(defaultImpl);
        assertEquals("org.apache.xmlbeans.impl.common.SystemCache", defaultImpl.getClass().getName());
    }

    @Test
    void testClearThreadLocal() {
        SystemCache cache = SystemCache.get();
        String saxLoader = "object is not cast currently...";

        cache.setSaxLoader(saxLoader);
        assertEquals(saxLoader, cache.getSaxLoader());

        cache.clearThreadLocals();
        assertNull(cache.getSaxLoader());

        cache.setSaxLoader(saxLoader);
        assertEquals(saxLoader, cache.getSaxLoader());
    }
}
