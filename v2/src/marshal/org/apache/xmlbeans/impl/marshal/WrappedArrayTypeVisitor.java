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

import java.lang.reflect.Array;

public class WrappedArrayTypeVisitor extends NamedXmlTypeVisitor
{
    private final WrappedArrayRuntimeBindingType type;
    private final int arrayLength;

    private int currIndex = -1;

    WrappedArrayTypeVisitor(RuntimeBindingProperty property,
                            Object obj,
                            MarshalResult result)
        throws XmlException
    {
        super(obj, property, result);

        type = (WrappedArrayRuntimeBindingType)getActualRuntimeBindingType();
        arrayLength = getArrayLength(obj);
    }

    private static int getArrayLength(Object obj)
    {
        if (obj == null) return 0;

        return Array.getLength(obj);
    }

    protected int getState()
    {
        assert currIndex <= arrayLength; //ensure we don't go past the end

        if (currIndex < 0) return START;

        if (currIndex >= arrayLength) return END;

        return CONTENT;
    }

    protected int advance()
        throws XmlException
    {
        assert currIndex < arrayLength; //ensure we don't go past the end

        do {
            currIndex++;
            if (currIndex == arrayLength) return END;
        }
        while (!currentItemHasValue());


        assert currIndex >= 0;
        assert (getState() == CONTENT);

        return CONTENT;
    }

    private boolean currentItemHasValue()
        throws XmlException
    {
        marshalResult.setCurrIndex(currIndex);
        return type.getElementProperty().isSet(getParentObject(),
                                               marshalResult);
    }

    private Object getCurrentValue()
        throws XmlException
    {
        marshalResult.setCurrIndex(currIndex);
        return type.getElementProperty().getValue(getParentObject(),
                                                  marshalResult);
    }

    public XmlTypeVisitor getCurrentChild()
        throws XmlException
    {
        final Object value = getCurrentValue();
        //TODO: avoid excessive object creation
        return marshalResult.createVisitor(type.getElementProperty(), value);
    }

    protected CharSequence getCharData()
    {
        throw new IllegalStateException("not text: " + this);
    }

    protected void initAttributes()
        throws XmlException
    {
        if (getParentObject() == null) {
            marshalResult.addXsiNilAttribute();
        } else if (needsXsiType()) {
            marshalResult.addXsiTypeAttribute(getActualRuntimeBindingType());
        }
    }

}
