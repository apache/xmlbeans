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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.impl.common.ValidationContext;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public abstract class JavaIntegerHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_INTEGER; }

    private BigInteger _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets/sets raw text value
    protected String compute_text(NamespaceManager nsm) { return _value.toString(); }
    protected void set_text(String s)
    {
        set_BigInteger(lex(s, _voorVc));
    }
    public static BigInteger lex(String s, ValidationContext vc) 
    {
        if (s.length() > 0 && s.charAt( 0 ) == '+' )
            s = s.substring(1);

        try { return new BigInteger(s); }
        catch (Exception e) { vc.invalid("Not a valid integer: " + s); return null; }
    }
    protected void set_nil()
    {
        _value = null;
    }
    // numerics: fractional
    public BigDecimal bigDecimalValue() { check_dated(); return _value == null ? null : new BigDecimal(_value); }
    public BigInteger bigIntegerValue() { check_dated(); return _value; }

    // setters
    protected void set_BigDecimal(BigDecimal v) { _value = v.toBigInteger(); }
    protected void set_BigInteger(BigInteger v) { _value = v; }

    // comparators
    protected int compare_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_BIG_INTEGER)
            return -i.compareTo(this);

        return _value.compareTo(((XmlObjectBase)i).bigIntegerValue());
    }

    protected boolean equal_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_BIG_INTEGER)
            return i.valueEquals(this);

        return _value.equals(((XmlObjectBase)i).bigIntegerValue());
    }

    static private BigInteger _maxlong = BigInteger.valueOf(Long.MAX_VALUE);
    static private BigInteger _minlong = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * Note, this is carefully aligned with hash codes for all xsd:decimal
     * primitives.
     */
    protected int value_hash_code()
    {
        if (_value.compareTo(_maxlong) > 0 ||
            _value.compareTo(_minlong) < 0)
            return _value.hashCode();

        long longval = _value.longValue();

        return (int)((longval >> 32) * 19 + longval);
    }
}
