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

import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

final class ByNameTypeVisitor extends NamedXmlTypeVisitor
{
    private final ByNameRuntimeBindingType type;
    private final int maxPropCount;
    private int propIdx = -1;
    private List attributeNames;
    private List attributeValues;
    private Iterator currMultipleIterator;
    private Object currMultipleItem;
    private boolean haveMultipleItem;


    ByNameTypeVisitor(RuntimeBindingProperty property, Object obj,
                      MarshalContextImpl context)
    {
        super(obj, property, context);
        final BindingType pt = property.getType();
        type = (ByNameRuntimeBindingType)context.createRuntimeBindingType(pt, obj);
        maxPropCount = obj == null ? 0 : type.getPropertyCount();
    }

    protected int getState()
    {
        assert propIdx <= maxPropCount; //ensure we don't go past the end

        if (propIdx < 0) return START;

        if (propIdx >= maxPropCount) return END;

        return CONTENT;
    }

    protected int advance()
    {
        assert propIdx < maxPropCount; //ensure we don't go past the end

        do {
            boolean hit_end = advanceToNextItem();
            if (hit_end) return END;
        }
        while (!currentPropHasMore());

        assert propIdx >= 0;

        return getState();
    }

    private boolean advanceToNextItem()
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
    {
        propIdx++;
        currMultipleIterator = null;
        haveMultipleItem = false;

        if (propIdx >= maxPropCount) return true;

        updateCurrIterator();

        return false;
    }

    private void updateCurrIterator()
    {
        final RuntimeBindingProperty property = getCurrentProperty();
        if (property.isMultiple()) {
            Object prop_obj = property.getValue(getParentObject(), marshalContext);
            final Iterator itr = MarshalResult.getCollectionIterator(prop_obj);
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
    {
        if (propIdx < 0) return false;

        if (haveMultipleItem) {
            if (currMultipleItem != null) return true;
            //skip null items in a collection if this element is not nillable
            return (getCurrentProperty().isNillable()); 
        }
        if (currMultipleIterator != null) return false;  //an empty collection

        final RuntimeBindingProperty property = getCurrentProperty();

        if (property.isAttribute()) return false;

        final boolean set = property.isSet(getParentObject(), marshalContext);
        return set;
    }

    public XmlTypeVisitor getCurrentChild()
    {
        final RuntimeBindingProperty property = getCurrentProperty();

        if (haveMultipleItem) {
            return MarshalResult.createVisitor(property, currMultipleItem,
                                               marshalContext);
        } else {
            Object prop_obj = property.getValue(getParentObject(), marshalContext);
            if (prop_obj instanceof Collection) {
                throw new AssertionError("not good: " + prop_obj);
            }
            return MarshalResult.createVisitor(property, prop_obj, marshalContext);
        }
    }

    private RuntimeBindingProperty getCurrentProperty()
    {
        final RuntimeBindingProperty property = type.getProperty(propIdx);
        assert property != null;
        return property;
    }

    protected int getAttributeCount()
    {
        if (attributeValues == null) initAttributes();

        assert attributeNames.size() == attributeValues.size();

        return attributeValues.size();
    }

    protected void initAttributes()
    {
        assert attributeNames == null;
        assert attributeValues == null;

        final List vals = new ArrayList();
        final List names = new ArrayList();

        if (getParentObject() == null) {
            QName nil_qn = fillPrefix(MarshalStreamUtils.XSI_NIL_QNAME);
            names.add(nil_qn);
            vals.add(XsTypeConverter.printBoolean(true));
        } else {

            if (type.isSubType()) {
                QName aname = fillPrefix(MarshalStreamUtils.XSI_TYPE_QNAME);
                names.add(aname);
                QName tn = fillPrefix(type.getSchemaTypeName());
                String aval = XsTypeConverter.getQNameString(tn.getNamespaceURI(),
                                                             tn.getLocalPart(),
                                                             tn.getPrefix());
                vals.add(aval);
            }

            for (int i = 0, len = maxPropCount; i < len; i++) {
                final RuntimeBindingProperty prop = type.getProperty(i);

                if (!prop.isAttribute()) continue;
                if (!prop.isSet(getParentObject(), marshalContext)) continue;

                final Object value = prop.getValue(getParentObject(),
                                                   marshalContext);

                final CharSequence val = prop.getLexical(value,
                                                         marshalContext);

                if (val == null) continue;

                vals.add(val);
                names.add(fillPrefix(prop.getName()));
            }
        }

        attributeNames = names;
        attributeValues = vals;

        assert attributeNames.size() == attributeValues.size();
    }

    protected String getAttributeValue(int idx)
    {
        CharSequence val = (CharSequence)attributeValues.get(idx);
        return val.toString();
    }

    protected QName getAttributeName(int idx)
    {
        QName an = (QName)attributeNames.get(idx);

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
