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

package org.apache.xmlbeans.impl.schema;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;

public class SchemaLocalAttributeImpl implements SchemaLocalAttribute, SchemaWSDLArrayType
{
    public SchemaLocalAttributeImpl()
    {
    }

    public void init(QName name, SchemaType.Ref typeref, int use, String deftext, XmlObject parseObject, XmlValueRef defvalue, boolean isFixed, SOAPArrayType wsdlArray, SchemaAnnotation ann)
    {
        if (_xmlName != null || _typeref != null)
            throw new IllegalStateException("Already initialized");
        _use = use;
        _typeref = typeref;
        _defaultText = deftext;
        _parseObject = parseObject;
        _defaultValue = defvalue;
        _isDefault = (deftext != null);
        _isFixed = isFixed;
        _xmlName = name;
        _wsdlArrayType = wsdlArray;
        _annotation = ann;
    }

    private String _defaultText;
    /* package */ XmlValueRef _defaultValue;
    private boolean _isFixed;
    private boolean _isDefault;
    private QName _xmlName;
    private SchemaType.Ref _typeref;
    private SOAPArrayType _wsdlArrayType;
    private int _use;
    private SchemaAnnotation _annotation;
    protected XmlObject _parseObject; // for QName resolution

    public boolean isTypeResolved()
    {
        return (_typeref != null);
    }

    public void resolveTypeRef(SchemaType.Ref typeref)
    {
        if (_typeref != null)
            throw new IllegalStateException();
        _typeref = typeref;
    }

    public int getUse()
        { return _use; }

    public QName getName()
        { return _xmlName; }

    public String getDefaultText()
        { return _defaultText; }

    public boolean isDefault()
        { return _isDefault; }

    public boolean isFixed()
        { return _isFixed; }

    public boolean isAttribute()
        { return true; }

    public SchemaAnnotation getAnnotation()
        { return _annotation; }

    public SchemaType getType()
        { return _typeref.get(); }

    public SchemaType.Ref getTypeRef()
        { return _typeref; }

    public BigInteger getMinOccurs()
        { return _use == REQUIRED ? BigInteger.ONE : BigInteger.ZERO; }

    public BigInteger getMaxOccurs()
        { return _use == PROHIBITED ? BigInteger.ZERO : BigInteger.ONE; }

    public boolean isNillable()
        { return false; }

    public SOAPArrayType getWSDLArrayType()
        { return _wsdlArrayType; }
    
    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        if (_defaultText != null && XmlAnySimpleType.type.isAssignableFrom(getType()))
        {
            if (_parseObject != null)
            {
                try
                {
                    NamespaceContext.push(new NamespaceContext(_parseObject));
                    return getType().newValue(_defaultText);
                }
                finally
                {
                    NamespaceContext.pop();
                }
            }
            return getType().newValue(_defaultText);
        }
        return null;
    }

    public void setDefaultValue(XmlValueRef defaultRef)
        { _defaultValue = defaultRef; }
}
