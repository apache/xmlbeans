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

package org.apache.xmlbeans.impl.jaxb.runtime;

import org.apache.xmlbeans.impl.values.NamespaceManager;
import javax.xml.namespace.NamespaceContext;

public class NamespaceContextWrapper implements NamespaceManager
{
    private NamespaceContext _context;

    public NamespaceContextWrapper(NamespaceContext context)
    {
        _context = context;
    }

    public String find_prefix_for_nsuri(String nsuri, String suggested_prefix)
    {
        return _context.getPrefix(nsuri);
    }

    public String getNamespaceForPrefix(String prefix)
    {
        return _context.getNamespaceURI(prefix);
    }
}
