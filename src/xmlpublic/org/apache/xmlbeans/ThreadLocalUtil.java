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

package org.apache.xmlbeans;

import org.apache.xmlbeans.impl.common.SystemCache;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.store.CharUtil;
import org.apache.xmlbeans.impl.store.Locale;
import org.apache.xmlbeans.impl.values.NamespaceContext;

public class ThreadLocalUtil {

    /**
     * Clear {@link ThreadLocal}s of the current thread.
     *
     * This can be used to clean out a thread before "returning"
     * it to a thread-pool or a Web-Container like Tomcat.
     */
    public static void clearAllThreadLocals() {
        // clear thread locals in all classes which may hold some
        XmlBeans.clearThreadLocals();
        XmlFactoryHook.ThreadContext.clearThreadLocals();
        StscState.clearThreadLocals();
        CharUtil.clearThreadLocals();
        Locale.clearThreadLocals();
        NamespaceContext.clearThreadLocals();

        // SystemCache is not a singleton, but also creates ThreadLocals,
        // so we get the current instance and clean it out as well
        SystemCache systemCache = SystemCache.get();
        systemCache.clearThreadLocals();
    }
}
