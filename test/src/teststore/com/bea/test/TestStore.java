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

package com.bea.test;

import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.values.TypeStoreUserFactory;
import xml.util.SchemaTypeSystem;
import xml.util.XmlObject;
import xml.util.SchemaType;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

public class TestStore
{
    TestTypeStore _root;

    public TestStore(SchemaType rootType)
    {
        _root = makeRootStore(rootType);
    }

    Cursor newCursor()
    {
        return new Cursor();
    }

    public class Cursor
    {
        TestTypeStore _curpos = _root;
        int _curIndex = 0;

        public XmlObject obj()
        {
            return (XmlObject)_curpos._user;
        }

        public boolean hasParent()
        {
            return _curpos._parent != null;
        }

        public boolean toRoot()
        {
            _curpos = _root;
            _curIndex = 0;
            return true;
        }

        public boolean toParent()
        {
            if (!hasParent())
                return false;
            _curpos = _curpos._parent;
            return true;
        }

        public boolean hasNext()
        {
            if (_curpos._parent == null)
                return false;
            if (_curIndex + 1 < _curpos._parent._childElementTypeStores.size())
                return true;
            return false;
        }

        public boolean toNext()
        {
            if (!hasNext())
                return false;
            _curIndex++;
            _curpos = (TestTypeStore)_curpos._parent._childElementTypeStores.get(_curIndex);
            return true;
        }

        public boolean toPrev()
        {
            if (!hasPrev())
                return false;
            _curIndex--;
            _curpos = (TestTypeStore)_curpos._parent._childElementTypeStores.get(_curIndex);
            return true;
        }

        public boolean hasPrev()
        {
            if (_curpos._parent == null)
                return false;
            if (_curIndex - 1 > 0)
                return true;
            return false;
        }

        public boolean hasChild()
        {
            return _curpos._childElementTypeStores != null &&
                   _curpos._childElementTypeStores.size() > 0;
        }

        public boolean toFirstChild()
        {
            if (!hasChild())
                return false;
            _curIndex = 0;
            _curpos = (TestTypeStore)_curpos._childElementTypeStores.get(_curIndex);
            return true;
        }

        public boolean toLastChild()
        {
            if (!hasChild())
                return false;
            _curIndex = _curpos._childElementTypeStores.size() - 1;
            _curpos = (TestTypeStore)_curpos._childElementTypeStores.get(_curIndex);
            return true;
        }

        public boolean toChildNamed(XMLName name, int index)
        {
            if (!hasChild())
                return false;
            int targetindex = 0;
            for (Iterator i = _curpos._childElementTypeStores.iterator(); i.hasNext(); )
            {
                TestTypeStore store = (TestTypeStore)i.next();

                if (store._name.equals(name))
                {
                    if (index == 0)
                    {
                        _curIndex = targetindex;
                        _curpos = store;
                        return true;
                    }
                    index--;
                }
                targetindex++;
            }
            return false;
        }

        public boolean toChildAt(int index)
        {
            if (!hasChild())
                return false;
            if (index < 0 || index >= _curpos._childElementTypeStores.size())
                return false;
            _curIndex = index;
            _curpos = (TestTypeStore)_curpos._childElementTypeStores.get(index);
            return true;
        }

        public void removeChildAt(int index)
        {
            if (!hasChild() || index < 0 || index >= _curpos._childElementTypeStores.size())
                throw new ArrayIndexOutOfBoundsException();
            TestTypeStore target = (TestTypeStore)_curpos._childElementTypeStores.get(index);
            target.disconnect();
            _curpos._childElementTypeStores.remove(index);
        }

        public void insertChildAt(int index, XMLName name)
        {
            if (index < 0 ||
                    (_curpos._childElementTypeStores == null ?
                    index > 0 : index > _curpos._childElementTypeStores.size()))
                throw new ArrayIndexOutOfBoundsException();
            _curpos._textLeafContent = null;
            if (_curpos._childElementTypeStores == null)
                _curpos._childElementTypeStores = new ArrayList();
            _curpos._childElementTypeStores.add(index, _curpos.makeNewChild(name, false));
        }

        public void addChild(XMLName name)
        {
            _curpos._textLeafContent = null;
            if (_curpos._childElementTypeStores == null)
                _curpos._childElementTypeStores = new ArrayList();
            _curpos._childElementTypeStores.add(_curpos.makeNewChild(name, false));
        }

        public int countChild()
        {
            if (_curpos._childElementTypeStores == null)
                return 0;
            return _curpos._childElementTypeStores.size();
        }

        public String getAttribute(XMLName name)
        {
            if (_curpos._childAttributeTypeStores == null)
                return null;

            TestTypeStore attr = (TestTypeStore)_curpos._childAttributeTypeStores.get(name);
            if (attr == null)
                return null;

            String result = attr._textLeafContent;
            return result == null ? "" : result;
        }

        public void clearAttribute(XMLName name)
        {
            if (_curpos._childAttributeTypeStores == null)
                return;
        }

        public void setAttribute(XMLName name, String value)
        {
            if (_curpos._childAttributeTypeStores == null)
                _curpos._childAttributeTypeStores = new HashMap();

            TestTypeStore attr = _curpos.makeNewChild(name, true);

            attr._textLeafContent = value;
            _curpos._childAttributeTypeStores.put(name, attr);
        }

        public void setText(String text)
        {
            if (_curpos._childElementTypeStores != null)
            {
                for (Iterator i = _curpos._childElementTypeStores.iterator(); i.hasNext(); )
                {
                    TestTypeStore store = (TestTypeStore)i.next();
                    store.disconnect();
                }
                _curpos._childElementTypeStores = null;
            }
            _curpos._textLeafContent = text;
        }

        public String getText()
        {
            if (_curpos._childElementTypeStores != null)
                throw new IllegalStateException(); // in practice perhaps we want to concatenate flattened stripped text
            String result = _curpos._textLeafContent;
            return result == null ? "" : result;
        }

        Cursor newCursor()
        {
            Cursor result = new Cursor();
            result._curIndex = _curIndex;
            result._curpos = _curpos;
            return result;
        }
    }

    private TestTypeStore makeRootStore(SchemaType rootType)
    {
        TestTypeStore result = new TestTypeStore();
        result._parent = null;
        result._name = null;
        result._isAttribute = false;
        result._user = ((TypeStoreUserFactory)rootType).createTypeStoreUser();
        if (result._user == null)
            throw new IllegalStateException();
        result._user.attach_store(result);
        return result;
    }
}
