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

import org.apache.xmlbeans.values.TypeStore;
import org.apache.xmlbeans.values.TypeStoreUser;
import org.apache.xmlbeans.values.TypeStoreVisitor;
import org.apache.xmlbeans.xml.stream.XMLName;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import xml.util.SchemaType;
import xml.util.QNameSet;
import xml.util.XmlCursor;

public class TestTypeStore implements TypeStore
{
    TestTypeStore _parent;
    TypeStoreUser _user;
    XMLName _name;
    boolean _isAttribute;
    boolean _isNil;
    String _textLeafContent;
    List _childElementTypeStores;
    Map _childAttributeTypeStores;

    public XmlCursor new_cursor()
    {
        throw new RuntimeException("Not implemented");
    }

    public void invalidate_text(TypeStoreUser user)
    {
        assert(!hasElements());
    }

    public String fetch_text(int whitespaceRule)
    {
        assert(!hasElements());
        return _textLeafContent == null ? "" : apply_wsr(_textLeafContent, whitespaceRule);
    }

    public void store_text(TypeStoreUser user, String text)
    {
        assert(!hasElements());
        _textLeafContent = text;
    }

    /**
     * Here the TypeStore is responsible for locating the default value.
     * This is done as follows
     * (1) go to the parent TypeStoreUser
     * (2) ask it to get_default_element_text(qname) (or _attribute_), and return it if not null.
     * (2) otherwise, grab a new TypeStoreUserVisitor via v = parentuser.new_visitor();
     * (3) call v.visit(name) on _every_ element qname up to and including this one in order
     * (4) return the result of v.get_default_text().
     */
    public String compute_default_text()
    {
        if (_parent == null)
            return "";
        String deftext =
            _isAttribute ? _parent._user.get_default_attribute_text(_name) :
                           _parent._user.get_default_element_text(_name);
        if (deftext == null)
        {
            TypeStoreVisitor visitor = _parent._user.new_visitor();
            for (Iterator i = _parent._childElementTypeStores.iterator(); i.hasNext(); )
            {
                TestTypeStore sibling = (TestTypeStore)i.next();
                visitor.visit(sibling._name);
                if (sibling == this)
                    return visitor.get_default_text();
            }
            assert(false) : "Invariant error: parent doesn't have this as child.";
        }
        return deftext;
    }

    public int compute_elementflags()
    {
        if (_parent == null)
            return 0;
        int flags;
        if (_isAttribute)
        {
            flags = _parent._user.get_attributeflags(_name);
        }
        else
        {
            flags = _parent._user.get_elementflags(_name);
            if (flags == -1)
            {
                TypeStoreVisitor visitor = _parent._user.new_visitor();
                for (Iterator i = _parent._childElementTypeStores.iterator(); i.hasNext(); )
                {
                    TestTypeStore sibling = (TestTypeStore)i.next();
                    visitor.visit(sibling._name);
                    if (sibling == this)
                        return visitor.get_elementflags();
                }
                assert(false) : "Invariant error: parent doesn't have this as child.";
                flags = 0;
            }
        }
        return flags;
    }

    public void invalidate_nil(TypeStoreUser user)
    {
        // this could be done later
        _isNil = _user.build_nil();
    }

    public boolean find_nil()
    {
        return _isNil;
    }

    public String find_prefix_for_nsuri(String nsuri, String suggested_prefix)
    {
        throw new RuntimeException("The test harness has no namespace support");
    }

    public int count_elements(XMLName name)
    {
        int count = 0;

        for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
        {
            if (((TestTypeStore)i.next())._name.equals(name))
                count++;
        }

        return count;
    }

    public TypeStoreUser find_element_user(XMLName name, int index)
    {
        for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
        {
            TestTypeStore store = (TestTypeStore)i.next();

            if (store._name.equals(name))
            {
                if (index == 0)
                    return store._user;
                index--;
            }
        }

        return null; // no exception please
    }

    public TypeStoreUser insert_element_user(XMLName name, int index)
    {
        TypeStoreUser target = null;
        int insertindex = 0;

        if (_childElementTypeStores == null)
        {
            _childElementTypeStores = new ArrayList();
        }
        else
        {
            int lastfoundat = 0;

            for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
            {
                TestTypeStore store = (TestTypeStore)i.next();

                if (store._name.equals(name))
                {
                    lastfoundat = insertindex;
                    index--;
                    if (index < 0)
                        break;
                }
                insertindex++;
            }

            if (index < 0)
            {
                // found one; insert right before
                insertindex = lastfoundat;
            }
            else if (index > 0)
            {
                // didn't find several; that's illegal.
                throw new ArrayIndexOutOfBoundsException();
            }
            else
            {
                // found all but last one: we must be appending.
                // but where to append?

                QNameSet delimiters = _parent._user.get_element_ending_delimiters(name);

                for (insertindex = lastfoundat + 1; insertindex < _childElementTypeStores.size(); insertindex++)
                {
                    TestTypeStore store = (TestTypeStore)_childElementTypeStores.get(insertindex);
                    if (delimiters.contains(store._name))
                        break; // insertindex is wherever the first delimiter is found
                }

                // if not found, insertindex is one past the end.
            }
        }

        TestTypeStore newChild = makeNewChild(name, false);
        _childElementTypeStores.add(insertindex, newChild);
        return newChild._user;
    }

    TestTypeStore makeNewChild(XMLName name, boolean isAttribute)
    {
        TestTypeStore result = new TestTypeStore();
        result._parent = this;
        result._name = name;
        result._isAttribute = isAttribute;
        if (isAttribute)
            result._user = _user.create_attribute_user(name);
        else
            result._user = _user.create_element_user(name, null); // no xsi:types for now
        if (result._user == null)
            throw new IllegalStateException();
        result._user.attach_store(result);
        return result;
    }

    void disconnect()
    {
        if (_childElementTypeStores != null)
        {
            for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
            {
                TestTypeStore store = (TestTypeStore)i.next();
                store.disconnect();
            }
        }
        _childElementTypeStores = null;
        if (_childAttributeTypeStores != null)
        {
            for (Iterator i = _childAttributeTypeStores.values().iterator(); i.hasNext(); )
            {
                TestTypeStore store = (TestTypeStore)i.next();
                store.disconnect();
            }
        }
        _childAttributeTypeStores = null;
        _user.disconnect_store();
        _user = null;
        _parent = null;
        _name = null;
        _textLeafContent = null;
    }

    public int findInsertIndexBasedOnOrder(XMLName[] order, XMLName nameToAdd)
    {
        int orderindex = 0;
        int insertindex = 0;

        for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
        {
            TestTypeStore store = (TestTypeStore)i.next();
            while (orderindex < order.length && !order[orderindex].equals(store._name))
            {
                if (order[orderindex].equals(nameToAdd))
                {
                    return insertindex;
                }
                orderindex++;
            }
            insertindex++;
        }
        return insertindex;
    }

    public TypeStoreUser add_element_user(XMLName name)
    {
        return insert_element_user(name, count_elements(name));
    }

    public void remove_element(XMLName name, int index)
    {
        TypeStoreUser target = null;
        int removeindex = 0;
        boolean found = false;

        for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
        {
            TestTypeStore store = (TestTypeStore)i.next();

            if (store._name.equals(name))
            {
                if (index == 0)
                {
                    _childElementTypeStores.remove(removeindex);
                    return;
                }
                index--;
            }
            removeindex++;
        }

        throw new ArrayIndexOutOfBoundsException();
    }

    public TypeStoreUser find_attribute_user(XMLName name)
    {
        if (_childAttributeTypeStores == null)
            return null;

        TestTypeStore store = (TestTypeStore)_childAttributeTypeStores.get(name);
        if (store == null)
            return null;

        return store._user;
    }

    public TypeStoreUser add_attribute_user(XMLName name)
    {
        if (_childAttributeTypeStores != null && _childAttributeTypeStores.containsKey(name))
            throw new IllegalArgumentException();

        if (_childAttributeTypeStores == null)
            _childAttributeTypeStores = new HashMap();

        TestTypeStore newChild = makeNewChild(name, true);
        _childAttributeTypeStores.put(name, newChild);
        return newChild._user;
    }

    public void remove_attribute(XMLName name)
    {
        if (_childAttributeTypeStores == null || !_childAttributeTypeStores.containsKey(name))
            throw new IllegalArgumentException();
        _childAttributeTypeStores.remove(name);
    }

    public void visit_elements(TypeStoreVisitor visitor)
    {
        for (Iterator i = _childElementTypeStores.iterator(); i.hasNext(); )
        {
            TestTypeStore store = (TestTypeStore)i.next();
            visitor.visit(store._name);
        }
        visitor.visit(null);
    }


    private boolean hasElements()
    {
        return _childElementTypeStores != null;
    }

    /**
     * The algorithm used by apply_wscanon: sometimes used in impls.
     */
    protected static String apply_wsr(String v, int wsr)
    {
        if (wsr == SchemaType.WS_PRESERVE || wsr == SchemaType.WS_UNSPECIFIED)
            return v;

        if (v.indexOf('\n') >= 0)
            v = v.replace('\n', ' ');
        if (v.indexOf('\t') >= 0)
            v = v.replace('\t', ' ');
        if (v.indexOf('\r') >= 0)
            v = v.replace('\r', ' ');

        if (wsr == SchemaType.WS_REPLACE)
            return v;

        int j = 0;
        int len = v.length();
        if (len == 0)
            return v;

        /* a trick: examine every other character looking for pairs of spaces */
        examine: if (v.charAt(0) != ' ')
        {
            for (j = 2; j < len; j += 2)
            {
                if (v.charAt(j) == ' ')
                {
                    if (v.charAt(j - 1) == ' ')
                        break examine;
                    if (j == len - 1)
                        break examine;
                    j++;
                    if (v.charAt(j) == ' ')
                        break examine;
                }
            }
            if (j == len && v.charAt(j - 1) == ' ')
                break examine;
            return v;
        }

        /* j is pointing at the first ws to be removed, or past end */
        char[] ch = v.toCharArray();
        int i = j;

        shifter: for (;;)
        {
            for (;;)
            {
                /* j was ws or past end */
                j++;
                if (j >= len)
                    break shifter;
                if (j != ' ')
                    break;
            }
            for (;;)
            {
                /* j was nonws */
                ch[i++] = ch[j++];
                if (j >= len)
                    break shifter;
                if (ch[j] == ' ')
                {
                    ch[i++] = ch[j++];
                    if (j >= len)
                        break shifter;
                    if (ch[j] == ' ')
                        break;
                }
            }
        }

        return new String(ch, 0, (i == 0 || ch[i - 1] != ' ') ? i : i - 1);
    }


}
