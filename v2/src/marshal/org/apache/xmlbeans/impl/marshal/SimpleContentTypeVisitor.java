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

import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

final class SimpleContentTypeVisitor
    extends SimpleContentVisitor
{
    private final SimpleContentRuntimeBindingType type;
    private final int maxAttributePropCount;
    private List attributes; //name, value, name, value...

    public SimpleContentTypeVisitor(RuntimeBindingProperty property,
                                    Object obj,
                                    MarshalResult result)
        throws XmlException
    {
        super(property, obj, result);

        type = (SimpleContentRuntimeBindingType)getActualRuntimeBindingType();
        maxAttributePropCount =
            obj == null ? 0 : type.getAttributePropertyCount();
    }

    protected int getAttributeCount()
        throws XmlException
    {
        return attributes.size() / 2;
    }

    protected void initAttributes()
        throws XmlException
    {
        attributes = new ArrayList();
        ByNameTypeVisitor.initAttributesInternal(this,
                                                 attributes,
                                                 type,
                                                 maxAttributePropCount,
                                                 marshalResult);

    }


    protected String getAttributeValue(int idx)
    {
        CharSequence val = (CharSequence)attributes.get(1 + (idx * 2));
        return val.toString();
    }

    protected QName getAttributeName(int idx)
    {
        QName an = (QName)attributes.get(idx * 2);

        //make sure we have a valid prefix
        assert ((an.getPrefix().length() == 0) ==
            (an.getNamespaceURI().length() == 0));

        return an;
    }

    protected CharSequence getCharData()
    {
        throw new IllegalStateException("not text: " + this);
    }


}
