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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;

public class WrappedArrayTypeVisitor extends NamedXmlTypeVisitor
{
    private final WrappedArrayRuntimeBindingType type;
    private final int arrayLength;

    private QName attributeName;
    private String xsiTypeAttVal;
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
        return MarshalResult.createVisitor(type.getElementProperty(),
                                           value,
                                           marshalResult);
    }

    protected int getAttributeCount()
        throws XmlException
    {
        return attributeName == null ? 0 : 1;
    }

    protected String getAttributeValue(int idx)
    {
        assert attributeName != null;

        return xsiTypeAttVal == null ? NIL_ATT_VAL : xsiTypeAttVal;
    }

    protected QName getAttributeName(int idx)
    {
        assert attributeName != null;
        return attributeName;
    }

    protected CharSequence getCharData()
    {
        throw new IllegalStateException("not text: " + this);
    }

    //TODO: refactor to avoid duplicate code in SimpleTypeVisitor
    protected void initAttributes()
        throws XmlException
    {
        if (getParentObject() == null) {
            attributeName = fillPrefix(MarshalStreamUtils.XSI_NIL_QNAME);
        } else if (needsXsiType()) {
            attributeName = fillPrefix(MarshalStreamUtils.XSI_TYPE_QNAME);

            final QName schema_type_name =
                getActualRuntimeBindingType().getSchemaTypeName();

            QName tn = fillPrefix(schema_type_name);
            xsiTypeAttVal = XsTypeConverter.getQNameString(tn.getNamespaceURI(),
                                                           tn.getLocalPart(),
                                                           tn.getPrefix());
        } else {
            attributeName = null;
        }
    }

}
