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
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;

import java.util.Collection;
import java.util.Iterator;

final class ByNameTypeVisitor
    extends NamedXmlTypeVisitor
{
    private final ByNameRuntimeBindingType type;
    private final int maxElementPropCount;
    private final int maxAttributePropCount;
    private int elemPropIdx = -1;
    private Iterator currMultipleIterator;
    private Object currMultipleItem;
    private boolean haveMultipleItem;


    ByNameTypeVisitor(RuntimeBindingProperty property,
                      Object obj,
                      PullMarshalResult result)
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
        }
        while (!currentPropHasMore());

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

    protected void initAttributes()
        throws XmlException
    {
        initAttributesInternal(this,
                               type,
                               maxAttributePropCount,
                               marshalResult);

    }

    static void initAttributesInternal(NamedXmlTypeVisitor typeVisitor,
                                       AttributeRuntimeBindingType rtt,
                                       int maxAttributePropCount,
                                       PullMarshalResult marshalResult)
        throws XmlException
    {
        final Object parent = typeVisitor.getParentObject();
        if (parent == null) {
            marshalResult.addXsiNilAttribute();
        } else {
            if (typeVisitor.needsXsiType()) {
                marshalResult.addXsiTypeAttribute(rtt);
            }

            for (int i = 0, len = maxAttributePropCount; i < len; i++) {
                final RuntimeBindingProperty prop = rtt.getAttributeProperty(i);

                if (!prop.isSet(parent, marshalResult)) continue;

                final Object value = prop.getValue(parent,
                                                   marshalResult);

                final CharSequence val = prop.getLexical(value,
                                                         marshalResult);

                if (val == null) continue;

                //REVIEW: defer toString call until actually used?
                marshalResult.fillAndAddAttribute(prop.getName(),
                                                  val.toString());
            }
        }
    }

    protected CharSequence getCharData()
    {
        throw new IllegalStateException("not text: " + this);
    }

}
