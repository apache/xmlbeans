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
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

final class ByNameTypeVisitor
    extends NamedXmlTypeVisitor
{
    private final ByNameRuntimeBindingType type;
    private final int maxElementPropCount;
    private final int maxAttributePropCount;
    private int elemPropIdx = -1;
    private List attributes; //name, value, name, value...
    private Iterator currMultipleIterator;
    private Object currMultipleItem;
    private boolean haveMultipleItem;


    ByNameTypeVisitor(RuntimeBindingProperty property, Object obj,
                      MarshalResult result)
        throws XmlException
    {
        super(obj, property, result);

        type = (ByNameRuntimeBindingType)getActualRuntimeBindingType();
        maxElementPropCount = obj == null ? 0 : type.getElementPropertyCount();
        maxAttributePropCount = obj == null ? 0 : type.getAttributePropertyCount();
    }


    protected int getState()
    {
        assert elemPropIdx <= maxElementPropCount; //ensure we don't go past the end

        if (elemPropIdx < 0) return START;

        if (elemPropIdx >= maxElementPropCount) return END;

        return CONTENT;
    }

    protected int advance()
        throws XmlException
    {
        assert elemPropIdx < maxElementPropCount; //ensure we don't go past the end

        do {
            boolean hit_end = advanceToNextItem();
            if (hit_end) return END;
        } while (!currentPropHasMore());

        assert elemPropIdx >= 0;

        return getState();
    }

    private boolean advanceToNextItem()
        throws XmlException
    {
        if (haveMultipleItem && currMultipleIterator.hasNext()) {
            currMultipleItem = currMultipleIterator.next();
            haveMultipleItem = true;
            return false;
        } else {
            return (advanceToNextProperty());
        }
    }

    //return true if we hit the end of our properties
    private boolean advanceToNextProperty()
        throws XmlException
    {
        elemPropIdx++;
        currMultipleIterator = null;
        haveMultipleItem = false;

        if (elemPropIdx >= maxElementPropCount) return true;

        updateCurrIterator();

        return false;
    }

    private void updateCurrIterator()
        throws XmlException
    {
        final RuntimeBindingProperty property = getCurrentElementProperty();
        if (property.isMultiple()) {
            final Object parent = getParentObject();
            final Object prop_obj = property.isSet(parent, marshalResult) ?
                property.getValue(parent, marshalResult) : null;
            final Iterator itr = ArrayUtils.getCollectionIterator(prop_obj);
            currMultipleIterator = itr;
            if (itr.hasNext()) {
                currMultipleItem = itr.next();
                haveMultipleItem = true;
            } else {
                haveMultipleItem = false;
            }
        }
    }

    private boolean currentPropHasMore()
        throws XmlException
    {
        if (elemPropIdx < 0) return false;

        if (haveMultipleItem) {
            if (currMultipleItem != null) return true;
            //skip null items in a collection if this element is not nillable
            return (getCurrentElementProperty().isNillable());
        }
        if (currMultipleIterator != null) return false;  //an empty collection

        final RuntimeBindingProperty property = getCurrentElementProperty();

        return property.isSet(getParentObject(), marshalResult);
    }

    public XmlTypeVisitor getCurrentChild() throws XmlException
    {
        final RuntimeBindingProperty property = getCurrentElementProperty();

        if (haveMultipleItem) {
            return marshalResult.createVisitor(property, currMultipleItem);
        } else {
            Object prop_obj = property.getValue(getParentObject(), marshalResult);
            if (prop_obj instanceof Collection) {
                throw new AssertionError("not good: " + prop_obj);
            }
            return marshalResult.createVisitor(property, prop_obj);
        }
    }

    private RuntimeBindingProperty getCurrentElementProperty()
    {
        final RuntimeBindingProperty property = type.getElementProperty(elemPropIdx);
        assert property != null;
        return property;
    }

    protected int getAttributeCount()
        throws XmlException
    {
        final int sz = ((attributes.size()) / 2);
        return sz;
    }

    protected void initAttributes()
        throws XmlException
    {
        attributes = new ArrayList();
        initAttributesInternal(this,
                               attributes,
                               type,
                               maxAttributePropCount,
                               marshalResult);

    }

    static void initAttributesInternal(NamedXmlTypeVisitor typeVisitor,
                                       List atts,
                                       AttributeRuntimeBindingType rtt,
                                       int maxAttributePropCount,
                                       MarshalResult marshalResult)
        throws XmlException
    {
        assert atts != null;
        atts.clear();


        final Object parent = typeVisitor.getParentObject();
        if (parent == null) {
            QName nil_qn = typeVisitor.fillPrefix(MarshalStreamUtils.XSI_NIL_QNAME);
            addAttribute(atts, nil_qn, XsTypeConverter.printBoolean(true));
        } else {
            if (typeVisitor.needsXsiType()) {
                QName aname = typeVisitor.fillPrefix(MarshalStreamUtils.XSI_TYPE_QNAME);
                QName tn = typeVisitor.fillPrefix(rtt.getSchemaTypeName());
                String aval = XsTypeConverter.getQNameString(tn.getNamespaceURI(),
                                                             tn.getLocalPart(),
                                                             tn.getPrefix());

                addAttribute(atts, aname, aval);
            }

            for (int i = 0, len = maxAttributePropCount; i < len; i++) {
                final RuntimeBindingProperty prop = rtt.getAttributeProperty(i);

                if (!prop.isSet(parent, marshalResult)) continue;

                final Object value = prop.getValue(parent,
                                                   marshalResult);

                final CharSequence val = prop.getLexical(value,
                                                         marshalResult);

                if (val == null) continue;

                addAttribute(atts, typeVisitor.fillPrefix(prop.getName()), val);
            }
        }

        assert (atts.size() % 2) == 0;
    }

    static void addAttribute(List atts, QName name, CharSequence value) {
        atts.add(name);
        atts.add(value);

        assert (atts.size() % 2) == 0;
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
