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
