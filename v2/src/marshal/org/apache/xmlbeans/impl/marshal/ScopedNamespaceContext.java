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

package org.apache.xmlbeans.impl.marshal;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

final class ScopedNamespaceContext
    implements NamespaceContext
{

    public static final String DEFAULT_NAMESPACE = "";

    private final Stack scopeStack;
    private LLNamespaceContext current = null;

    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final String XML_PREFIX = "xml";
    private static final String XMLNS_PREFIX = "xmlns";

    public ScopedNamespaceContext()
    {
        this(null, new Stack());
    }

    private ScopedNamespaceContext(LLNamespaceContext head, Stack scopeStack)
    {
        this.current = head;
        this.scopeStack = scopeStack;
    }

    public ScopedNamespaceContext(NamespaceContext root_nsctx)
    {
        //TODO: copy or use initial context!
        this(null, new Stack());
    }

    public void clear()
    {
        scopeStack.clear();
        current = null;
    }

    public String getNamespaceURI(String prefix)
    {
        //two special cases from JSR 173
        if (XML_PREFIX.equals(prefix))
            return XML_NS;

        if (XMLNS_PREFIX.equals(prefix))
            return XMLNS_URI;

        if (current == null) return null;

        return current.getNamespaceURI(prefix);
    }

    public String getPrefix(String namespaceURI)
    {
        //two special cases from JSR 173

        if (XML_NS.equals(namespaceURI))
            return XML_PREFIX;

        if (XMLNS_URI.equals(namespaceURI))
            return XMLNS_PREFIX;


        if (current == null)
            return null;
        else
            return current.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        return current.getPrefixes(namespaceURI);
    }

    public boolean scopeOpened()
    {
        return !scopeStack.isEmpty();
    }

    public void openScope()
    {
        scopeStack.push(current);
    }

    public void closeScope()
    {
        if (!scopeOpened())
            throw new IllegalStateException("no scope currently open");

        final LLNamespaceContext previous = (LLNamespaceContext)scopeStack.pop();
        current = previous;
    }

    public void bindNamespace(String prefix, String namespace)
    {
        if (!scopeOpened())
            throw new IllegalStateException("no scope currently open");
        if (namespace == null)
            throw new IllegalArgumentException("null namespace not allowed");
        if (prefix == DEFAULT_NAMESPACE || prefix == null) {
            final String msg = "Cannot use empty string or null as a prefix; " +
                "use bindDefaultNamespace() to bind the default namespace";
            throw new IllegalArgumentException(msg);
        }
        current = new LLNamespaceContext(prefix, namespace, current);
    }

    public void unbindNamespace(String prefix)
    {
        bindNamespace(prefix, null);
    }

    public void bindDefaultNamespace(String uri)
    {
        bindNamespace(DEFAULT_NAMESPACE, uri);
    }

    public void unbindDefaultNamespace()
    {
        //TODO: FIXME
        bindDefaultNamespace(null);
    }

    public int getDepth()
    {
        return scopeStack.size();
    }

    public Map getNamespaceMap()
    {
        return getNamespaceMap(current);
    }


    public int getCurrentScopeNamespaceCount()
    {
        if (current == null) return 0;

        final LLNamespaceContext top = (LLNamespaceContext)scopeStack.peek();
        int cnt = 0;
        for (LLNamespaceContext cur = current; cur != top; cur = cur.getPredecessor()) {
            ++cnt;
        }
        return cnt;
    }

    public String getCurrentScopeNamespacePrefix(int i)
    {
        return getCurrentScopeNamespace(i).getPrefix();
    }

    public String getCurrentScopeNamespaceURI(int i)
    {
        return getCurrentScopeNamespace(i).getNamespace();
    }

    private LLNamespaceContext getCurrentScopeNamespace(int i)
    {
        if (i < 0) throw new IllegalArgumentException("negative index: " + i);

        final LLNamespaceContext top = (LLNamespaceContext)scopeStack.peek();
        int cnt = 0;
        for (LLNamespaceContext cur = current; cur != top; cur = cur.getPredecessor()) {
            if (cnt == i) {
                return cur;
            }
            ++cnt;
        }
        throw new IndexOutOfBoundsException("index of out range: " + i);
    }


    private static final Map getNamespaceMap(LLNamespaceContext ctx)
    {
        if (ctx == null)
            return new HashMap();
        else {
            final Map result = getNamespaceMap(ctx.predecessor);
            result.put(ctx.prefix, ctx.namespace);
            return result;
        }
    }

    private static class LLNamespaceContext
    {
        private final String prefix;
        private final String namespace;
        private final LLNamespaceContext predecessor;


        public LLNamespaceContext(String prefix, String namespace,
                                  LLNamespaceContext predecessor)
        {
            this.prefix = prefix;
            this.namespace = namespace;
            this.predecessor = predecessor;
        }

        public final String getNamespace()
        {
            return namespace;
        }

        public final String getPrefix()
        {
            return prefix;
        }

        final LLNamespaceContext getPredecessor()
        {
            return predecessor;
        }

        public String getNamespaceURI(String prefix)
        {
            for (LLNamespaceContext entry = this; entry != null; entry = entry.predecessor) {
                if (prefix.equals(entry.prefix)) return entry.namespace;
            }
            return null;
        }

        public String getPrefix(String namespaceURI)
        {
            for (LLNamespaceContext entry = this; entry != null; entry = entry.predecessor) {
                if (namespaceURI.equals(entry.namespace)) return entry.prefix;
            }
            return null;
        }

        public Iterator getPrefixes(String namespaceURI)
        {
            final Set result = getPrefixSet(namespaceURI, this);
            return result.iterator();
        }

        static final Set getPrefixSet(String namespace,
                                      LLNamespaceContext context)
        {
            final Stack reversed = new Stack();
            LLNamespaceContext current = context;
            while (current != null) {
                reversed.push(current);
                current = current.predecessor;
            }
            Set result = new HashSet(4);
            while (!reversed.isEmpty()) {
                current = (LLNamespaceContext)reversed.pop();
                if (namespace.equals(current.namespace))
                    result.add(current.prefix);
                else
                    result.remove(current.prefix);
            }

            return result;
        }

    }
}