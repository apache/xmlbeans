/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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