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

import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;
import java.util.Set;

import javax.xml.namespace.QName;

public class SchemaPropertyImpl implements SchemaProperty
{
    private QName _name;
    private SchemaType.Ref _typeref;
    private boolean _isAttribute;
    private SchemaType.Ref _containerTypeRef;
    private String _javaPropertyName;
    private BigInteger _minOccurs;
    private BigInteger _maxOccurs;
    private int _hasNillable;
    private int _hasDefault;
    private int _hasFixed;
    private String _defaultText;
    private boolean _isImmutable;
    private SchemaType.Ref _javaBasedOnTypeRef;
    private boolean _extendsSingleton;
    private boolean _extendsArray;
    private boolean _extendsOption;
    private int _javaTypeCode;
    private QNameSet _javaSetterDelimiter;
    private XmlValueRef _defaultValue;
    private Set _acceptedNames;

    private void mutate()
        { if (_isImmutable) throw new IllegalStateException(); }

    public void setImmutable()
        { mutate(); _isImmutable = true; }

    public SchemaType getContainerType()
        { return _containerTypeRef.get(); }

    public void setContainerTypeRef(SchemaType.Ref typeref)
        { mutate(); _containerTypeRef = typeref; }

    public QName getName()
        { return _name; }

    public void setName(QName name)
        { mutate(); _name = name; }

    public String getJavaPropertyName()
        { return _javaPropertyName; }

    public void setJavaPropertyName(String name)
        { mutate(); _javaPropertyName = name; }

    public boolean isAttribute()
        { return _isAttribute; }

    public void setAttribute(boolean isAttribute)
        { mutate(); _isAttribute = isAttribute; }

    public boolean isReadOnly()
        { return false; }

    public SchemaType getType()
        { return _typeref.get(); }

    public void setTypeRef(SchemaType.Ref typeref)
        { mutate(); _typeref = typeref; }

    public SchemaType javaBasedOnType()
        { return _javaBasedOnTypeRef.get(); }

    public boolean extendsJavaSingleton()
        { return _extendsSingleton; }

    public boolean extendsJavaArray()
        { return _extendsArray; }

    public boolean extendsJavaOption()
        { return _extendsOption; }

    public void setExtendsJava(SchemaType.Ref javaBasedOnTypeRef, boolean singleton, boolean option, boolean array)
    {
        mutate();
        _javaBasedOnTypeRef = javaBasedOnTypeRef;
        _extendsSingleton = singleton;
        _extendsOption = option;
        _extendsArray = array;
    }

    public QNameSet getJavaSetterDelimiter()
        { return _javaSetterDelimiter == null ? QNameSet.EMPTY : _javaSetterDelimiter; }

    public void setJavaSetterDelimiter(QNameSet set)
        { mutate(); _javaSetterDelimiter = set; }

    public QName[] acceptedNames()
    { 
        if (_acceptedNames == null)
            return new QName[] { _name };

        return (QName[])_acceptedNames.toArray(new QName[_acceptedNames.size()]); 
    }

    public void setAcceptedNames(Set set)
    {
        mutate(); 
        _acceptedNames = set;
    }
    public void setAcceptedNames(QNameSet set)
    { 
        mutate(); 
        _acceptedNames = set.includedQNamesInExcludedURIs();
    }

    public BigInteger getMinOccurs()
        { return _minOccurs; }

    public void setMinOccurs(BigInteger min)
        { mutate(); _minOccurs = min; }

    public BigInteger getMaxOccurs()
        { return _maxOccurs; }

    public void setMaxOccurs(BigInteger max)
        { mutate(); _maxOccurs = max; }

    public int hasNillable()
        { return _hasNillable; }

    public void setNillable(int when)
        { mutate(); _hasNillable = when; }

    public int hasDefault()
        { return _hasDefault; }

    public void setDefault(int when)
        { mutate(); _hasDefault = when; }

    public int hasFixed()
        { return _hasFixed; }

    public void setFixed(int when)
        { mutate(); _hasFixed = when; }

    public String getDefaultText()
        { return _defaultText; }

    public void setDefaultText(String val)
        { mutate(); _defaultText = val; }

    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        return null;
    }
    
    public void setDefaultValue(XmlValueRef defaultRef)
    {
        mutate();
        _defaultValue = defaultRef;
    }

    public int getJavaTypeCode()
        { return _javaTypeCode; }

    public void setJavaTypeCode(int code)
        { mutate(); _javaTypeCode = code; }
}
