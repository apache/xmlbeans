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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.common.ValidationContext;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class JavaDoubleHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_DOUBLE; }

    double _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets/sets raw text value
    protected String compute_text(NamespaceManager nsm) { return serialize(_value); }

    public static String serialize(double d)
    {
        if (d == Double.POSITIVE_INFINITY)
            return "INF";
        else if (d == Double.NEGATIVE_INFINITY)
            return "-INF";
        else if (d == Double.NaN)
            return "NaN";
        else
            return Double.toString(d);
    }
    protected void set_text(String s)
    {
        set_double(validateLexical(s,_voorVc));
    }
    public static double validateLexical(String v, ValidationContext context)
    {
        try
        {
            return Double.parseDouble(v);
        }
        catch(NumberFormatException e)
        {
            if (v.equals("INF"))  return Double.POSITIVE_INFINITY;
            if (v.equals("-INF")) return Double.NEGATIVE_INFINITY;
            if (v.equals("NaN"))  return Double.NaN;
            
            context.invalid("Invalid double value");

            return Double.NaN;
        }
    }
    protected void set_nil()
    {
        _value = 0.0;
    }

    // numerics: fractional
    public BigDecimal bigDecimalValue() { check_dated(); return new BigDecimal(_value); }
    public double doubleValue() { check_dated(); return _value; }
    public float floatValue() { check_dated(); return (float)_value; }

    // setters
    protected void set_double(double v) { _value = v; }
    protected void set_float(float v) { set_double((double)v); }
    protected void set_long(long v) { set_double((double)v); }
    protected void set_BigDecimal(BigDecimal v) { set_double(v.doubleValue()); }
    protected void set_BigInteger(BigInteger v) { set_double(v.doubleValue()); }

    // comparators
    protected int compare_to(XmlObject d)
    {
        return compare(_value,((XmlObjectBase)d).doubleValue());
    }
    static int compare(double thisValue, double thatValue)
    {
        if (thisValue < thatValue) return -1;
        if (thisValue > thatValue) return  1;

        long thisBits = Double.doubleToLongBits(thisValue);
        long thatBits = Double.doubleToLongBits(thatValue);

        return thisBits == thatBits ? 0 : thisBits < thatBits ? -1 : 1;
    }

    protected boolean equal_to(XmlObject d)
    {
        return compare(_value, ((XmlObjectBase)d).doubleValue()) == 0;
    }

    protected int value_hash_code()
    {
        long v = Double.doubleToLongBits(_value);
        return (int)((v >> 32) * 19 + v);
    }
}
