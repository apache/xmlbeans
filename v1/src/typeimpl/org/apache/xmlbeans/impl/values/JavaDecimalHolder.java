/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public class JavaDecimalHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_DECIMAL; }

    private BigDecimal _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // sets/gets raw text value
    protected String compute_text(NamespaceManager nsm) { return _value.toString(); }
    protected void set_text(String s)
    {
        if (_validateOnSet())
            validateLexical(s, _voorVc);

        try {
            set_BigDecimal(new BigDecimal(s));
        }
        catch (NumberFormatException e)
        {
            _voorVc.invalid("Invalid Decimal");
        }
    }
    protected void set_nil()
    {
        _value = null;
    }

    /**
     * Performs lexical validation only.
     */

    public static void validateLexical(String v, ValidationContext context)
    {
        // TODO - will want to validate Chars with built in white space handling
        //        However, this fcn sometimes takes a value with wsr applied
        //        already
        int l = v.length();
        int i = 0;
        
        if (i < l)
        {
            int ch = v.charAt(i);
            
            if (ch == '+' || ch == '-')
                i++;
        }
        
        boolean sawDot = false;
        boolean sawDigit = false;

        for ( ; i < l ; i++ )
        {
            int ch = v.charAt(i);

            if (ch == '.')
            {
                if (sawDot)
                {
                    context.invalid("Illegal decimal, saw '.' more than once");
                    return;
                }

                sawDot = true;
            }
            else if (ch >= '0' && ch <= '9')
            {
                sawDigit = true;
            }
            else
            {
                // TODO - may need to escape error char
                context.invalid("Illegal decimal, unexpected char: " + ch);
                return;
            }
        }

        if (!sawDigit)
        {
            context.invalid("Illegal decimal, expected at least one digit");
            return;
        }
    }

    // numerics: fractional
    public BigDecimal bigDecimalValue() { check_dated(); return _value; }

    // setters
    protected void set_BigDecimal(BigDecimal v) { _value = v; }

    // comparators
    protected int compare_to(XmlObject decimal)
    {
        return _value.compareTo(((XmlObjectBase)decimal).bigDecimalValue());
    }
    protected boolean equal_to(XmlObject decimal)
    {
        return (_value.compareTo(((XmlObjectBase)decimal).bigDecimalValue())) == 0;
    }

    static private BigInteger _maxlong = BigInteger.valueOf(Long.MAX_VALUE);
    static private BigInteger _minlong = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * Note, this is carefully aligned with hash codes for all xsd:decimal
     * primitives.
     */
    protected int value_hash_code()
    {
        if (_value.scale() > 0)
        {
            if (_value.setScale(0, BigDecimal.ROUND_DOWN).compareTo(_value) != 0)
                return decimalHashCode();
        }

        BigInteger intval = _value.toBigInteger();

        if (intval.compareTo(_maxlong) > 0 ||
            intval.compareTo(_minlong) < 0)
            return intval.hashCode();

        long longval = intval.longValue();

        return (int)((longval >> 32) * 19 + longval);
    }

    /**
     * This method will has BigDecimals with the same arithmetic value to
     * the same hash code (eg, 2.3 & 2.30 will have the same hash.)
     * This differs from BigDecimal.hashCode()
     */
    protected int decimalHashCode() {
        assert _value.scale() > 0;

        // Get decimal value as string, and strip off zeroes on the right
        String strValue = _value.toString();
        int i;
        for (i = strValue.length() - 1 ; i >= 0 ; i --)
            if (strValue.charAt(i) != '0') break;

        assert strValue.indexOf('.') < i;

        // Return the canonicalized string hashcode
        return strValue.substring(0, i + 1).hashCode();
    }
}
