/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.impl.common.PrefixResolver;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

import java.util.ArrayList;
import java.util.Map;
import java.lang.reflect.Proxy;

import weblogic.xml.stream.StartElement;

public class NamespaceContext implements PrefixResolver
{
    private static final int TYPE_STORE    = 1;
    private static final int XML_OBJECT    = 2;
    private static final int MAP           = 3;
    private static final int START_ELEMENT = 4;
    private static final int RESOLVER      = 5;

    private Object _obj;
    private int _code;

    public NamespaceContext(Map prefixToUriMap)
    {
        _code = MAP;
        _obj = prefixToUriMap;
    }

    public NamespaceContext(TypeStore typeStore)
    {
        _code = TYPE_STORE;
        _obj = typeStore;
    }

    public NamespaceContext(XmlObject xmlObject)
    {
        _code = XML_OBJECT;
        _obj = xmlObject;
    }

    public NamespaceContext(StartElement start)
    {
        _code = START_ELEMENT;
        _obj = start;
    }

    public NamespaceContext(PrefixResolver resolver)
    {
        _code = RESOLVER;
        _obj = resolver;
    }

    /**
     * Stack management if (heaven help us) we ever need to do
     * nested compilation of schema type system.
     */
    private static final class NamespaceContextStack
    {
        NamespaceContext current;
        ArrayList stack = new ArrayList();
        final void push(NamespaceContext next)
        {
            stack.add(current);
            current = next;
        }
        final void pop()
        {
            current = (NamespaceContext)stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
    }

    private static ThreadLocal NamespaceContextStack = new ThreadLocal()
    {
        protected Object initialValue() { return new NamespaceContextStack(); }
    };

    public static void push(NamespaceContext next)
    {
        ((NamespaceContextStack)NamespaceContextStack.get()).push(next);
    }
            
    public String getNamespaceForPrefix(String prefix)
    {
        if (prefix != null && prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";
        
        switch (_code)
        {
            case XML_OBJECT:
            {
                TypeStoreUser impl;
                Object obj = _obj;
                if (Proxy.isProxyClass(obj.getClass()))
                    obj = Proxy.getInvocationHandler(obj);

                if (obj instanceof TypeStoreUser)
                    return ((TypeStoreUser)obj).get_store().getNamespaceForPrefix(prefix);

                XmlCursor cur = ((XmlObject)_obj).newCursor();
                if (cur != null)
                {
                    if (cur.currentTokenType() == XmlCursor.TokenType.ATTR)
                        cur.toParent();
                    try { return cur.namespaceForPrefix(prefix); }
                    finally { cur.dispose(); }
                }
            }
            
            case MAP:
                return (String)((Map)_obj).get(prefix);
                
            case TYPE_STORE:
                return ((TypeStore)_obj).getNamespaceForPrefix(prefix);
                
            case START_ELEMENT:
                return ((StartElement)_obj).getNamespaceUri(prefix);
                
            case RESOLVER:
                return ((PrefixResolver)_obj).getNamespaceForPrefix(prefix);
                
            default:
                assert false : "Improperly initialized NamespaceContext.";
                return null;
        }
    }

    public static PrefixResolver getCurrent()
    {
        return ((NamespaceContextStack)NamespaceContextStack.get()).current;
    }
    
    public static void pop()
    {
        ((NamespaceContextStack)NamespaceContextStack.get()).pop();
    }

}
