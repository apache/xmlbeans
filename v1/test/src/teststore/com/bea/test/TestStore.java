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
