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

package misc.detailed;

import org.apache.xmlbeans.ThreadLocalUtil;
import org.apache.xmlbeans.impl.common.SystemCache;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.store.CharUtil;
import org.junit.Test;

public class ThreadLocalUtilTest {
    @Test
    public void testClearThreadLocalsNoData() {
        // simply calling it without any thread locals should work
        ThreadLocalUtil.clearAllThreadLocals();
    }

    @Test
    public void testClearThreadLocalsWithData() {
        // calling it with thread locals should work as well
        CharUtil.getThreadLocalCharUtil();

        SystemCache cache = SystemCache.get();
        String saxLoader = "object is not cast currently...";
        cache.setSaxLoader(saxLoader);
        StscState.start();
        StscState.end();

        ThreadLocalUtil.clearAllThreadLocals();
    }
}
