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

package org.apache.xmlbeans.impl.common;

import java.io.InputStream;

import org.apache.xmlbeans.ResourceLoader;

public class DefaultClassLoaderResourceLoader implements ResourceLoader
{
    public InputStream getResourceAsStream(String resourceName) {
        InputStream in = null;
        try {
            in = getResourceAsStream(Thread.currentThread().getContextClassLoader(), resourceName);
        } catch (SecurityException securityexception) {}
        if (in == null) {
            in = getResourceAsStream(DefaultClassLoaderResourceLoader.class.getClassLoader(), resourceName);
        }
        if (in == null) {
            in = DefaultClassLoaderResourceLoader.class.getResourceAsStream(resourceName);
        }
        return in;
    }

    public void close() {}

    private InputStream getResourceAsStream(ClassLoader loader, String resourceName) {
        return loader == null ? null : loader.getResourceAsStream(resourceName);
    }
}
