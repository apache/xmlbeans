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

package org.apache.xmlbeans.impl.marshal.util;

import org.apache.xmlbeans.impl.marshal.util.collections.StringList;

import javax.xml.namespace.QName;

public final class AttributeHolder
{
    private final StringList data;

    private static final int LOCALNAME_OFFSET = 1;
    private static final int PREFIX_OFFSET = 2;
    private static final int VALUE_OFFSET = 3;

    public AttributeHolder(int initial_capacity)
    {
        data = new StringList(4 * initial_capacity);
    }

    public AttributeHolder()
    {
        this(4);
    }

    public void clear()
    {
        data.clear();

        assert data.getSize() == 0;
    }

    public void add(String namespaceURI, String localPart, String prefix,
                    String value)
    {
        data.add(namespaceURI);
        data.add(localPart);
        data.add(prefix);
        data.add(value);

        assert (data.getSize() % 4) == 0;
    }

    public void add(QName name, String value)
    {
        add(name.getNamespaceURI(),
            name.getLocalPart(),
            name.getPrefix(),
            value);
    }


    public int getAttributeCount()
    {
        assert (data.getSize() % 4) == 0;
        return data.getSize() / 4;
    }

    public String getAttributeValue(int idx)
    {
        assert (data.getSize() % 4) == 0;

        return data.get(VALUE_OFFSET + idx * 4);
    }

    public QName getAttributeName(int idx)
    {
        //TODO: consider caching these values...

        final String uri = getAttributeNamespace(idx);
        if (uri == null || uri.length() == 0) {
            return new QName(getAttributeLocalName(idx));
        } else {
            final String pfx = getAttributePrefix(idx);
            assert pfx != null;
            assert pfx.length() > 0;
            return new QName(uri,
                             getAttributeLocalName(idx),
                             pfx);
        }
    }


    public String getAttributeNamespace(int i)
    {
        assert (data.getSize() % 4) == 0;

        return data.get(i * 4);
    }

    public String getAttributeLocalName(int i)
    {
        assert (data.getSize() % 4) == 0;

        return data.get(LOCALNAME_OFFSET + i * 4);
    }

    public String getAttributePrefix(int i)
    {
        assert (data.getSize() % 4) == 0;

        return data.get(PREFIX_OFFSET + i * 4);
    }

    public boolean isAttributeSpecified(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }


    public String getAttributeValue(String uri, String lname)
    {
        //TODO: do better than this basic and slow implementation
        for (int i = 0, len = getAttributeCount(); i < len; i++) {

            if (lname.equals(getAttributeLocalName(i))) {
                if (uri == null || uri.equals(getAttributeNamespace(i)))
                    return getAttributeValue(i);
            }
        }
        return null;
    }

}
