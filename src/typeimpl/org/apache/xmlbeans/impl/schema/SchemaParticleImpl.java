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

import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;

import javax.xml.namespace.QName;

public class SchemaParticleImpl implements SchemaParticle
{
    private int _particleType;
    private BigInteger _minOccurs;
    private BigInteger _maxOccurs;
    private SchemaParticle[] _particleChildren;
    private boolean _isImmutable;
    private QNameSet _startSet;
    private QNameSet _excludeNextSet;
    private boolean _isSkippable;
    private boolean _isDeterministic;
    private int _intMinOccurs;
    private int _intMaxOccurs;
    private QNameSet _wildcardSet;
    private int _wildcardProcess;
    private String _defaultText;
    private boolean _isDefault;
    private boolean _isFixed;
    private QName _qName;
    private boolean _isNillable;
    private SchemaType.Ref _typeref;
    protected XmlObject _parseObject;
    private XmlValueRef _defaultValue;

    protected void mutate()
        { if (_isImmutable) throw new IllegalStateException(); }

    public void setImmutable()
        { mutate(); _isImmutable = true; }

    public boolean hasTransitionRules()
        { return (_startSet != null); }

    public boolean hasTransitionNotes()
        { return (_excludeNextSet != null); }

    public void setTransitionRules(QNameSet start,
                                   boolean isSkippable)
    {
        _startSet = start;
        _isSkippable = isSkippable;
    }

    public void setTransitionNotes(QNameSet excludeNext, boolean isDeterministic)
    {
        _excludeNextSet = excludeNext;
        _isDeterministic = isDeterministic;
    }

    public boolean canStartWithElement(QName name)
        { return name != null && _startSet.contains(name); }

    public QNameSet acceptedStartNames()
        { return _startSet; }

    public QNameSet getExcludeNextSet()
        { return _excludeNextSet; }

    public boolean isSkippable()
        { return _isSkippable; }

    public boolean isDeterministic()
        { return _isDeterministic; }

    public int getParticleType()
        { return _particleType; }

    public void setParticleType(int pType)
        { mutate(); _particleType = pType; }

    public boolean isSingleton()
        { return _maxOccurs != null &&
                 _maxOccurs.compareTo(BigInteger.ONE) == 0 &&
                 _minOccurs.compareTo(BigInteger.ONE) == 0; }

    public BigInteger getMinOccurs()
        { return _minOccurs; }

    public void setMinOccurs(BigInteger min)
        { mutate(); _minOccurs = min; _intMinOccurs = pegBigInteger(min); }

    public int getIntMinOccurs()
        { return _intMinOccurs; }

    public BigInteger getMaxOccurs()
        { return _maxOccurs; }

    public int getIntMaxOccurs()
        { return _intMaxOccurs; }

    public void setMaxOccurs(BigInteger max)
        { mutate(); _maxOccurs = max; _intMaxOccurs = pegBigInteger(max); }

    public SchemaParticle[] getParticleChildren()
    {
        SchemaParticle[] result = new SchemaParticle[_particleChildren.length];
        System.arraycopy(_particleChildren, 0, result, 0, _particleChildren.length);
        return result;
    }

    public void setParticleChildren(SchemaParticle[] children)
        { mutate(); _particleChildren = children; }

    public SchemaParticle getParticleChild(int i)
        { return _particleChildren[i]; }

    public int countOfParticleChild()
        { return _particleChildren == null ? 0 : _particleChildren.length; }

    public void setWildcardSet(QNameSet set)
        { mutate(); _wildcardSet = set; }

    public QNameSet getWildcardSet()
        { return _wildcardSet; }

    public void setWildcardProcess(int process)
        { mutate(); _wildcardProcess = process; }

    public int getWildcardProcess()
        { return _wildcardProcess; }

    private static final BigInteger _maxint = BigInteger.valueOf(Integer.MAX_VALUE);

    private static final int pegBigInteger(BigInteger bi)
    {
        if (bi == null)
            return Integer.MAX_VALUE;
        if (bi.signum() <= 0)
            return 0;
        if (bi.compareTo(_maxint) >= 0)
            return Integer.MAX_VALUE;
        return bi.intValue();
    }

    public QName getName()
        { return _qName; }

    public void setNameAndTypeRef(QName formname, SchemaType.Ref typeref)
        { mutate(); _qName = formname; _typeref = typeref; }

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

    public boolean isAttribute()
        { return false; }

    public SchemaType getType()
        { if (_typeref == null) return null; return _typeref.get(); }

    public String getDefaultText()
        { return _defaultText; }

    public boolean isDefault()
        { return _isDefault; }

    public boolean isFixed()
        { return _isFixed; }

    public void setDefault(String deftext, boolean isFixed, XmlObject parseObject)
    {
        mutate();
        _defaultText = deftext;
        _isDefault = (deftext != null);
        _isFixed = isFixed;
        _parseObject = parseObject;
    }

    public boolean isNillable()
        { return _isNillable; }

    public void setNillable(boolean nillable)
        { mutate(); _isNillable = nillable; }

    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        if (_defaultText != null && XmlAnySimpleType.type.isAssignableFrom(getType()))
        {
            if (_parseObject != null && XmlQName.type.isAssignableFrom(getType()))
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
    {
        mutate();
        _defaultValue = defaultRef;
    }
}
