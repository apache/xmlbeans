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
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;


import java.math.BigDecimal;

public abstract class JavaDecimalHolderEx extends JavaDecimalHolder
{
    private SchemaType _schemaType;

    public SchemaType schemaType()
        { return _schemaType; }

    public JavaDecimalHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected void set_text(String s)
    {
        if (_validateOnSet())
            validateLexical(s, _schemaType, _voorVc);

        BigDecimal v = null;
        try {
            v = new BigDecimal(s);
        }
        catch (NumberFormatException e)
        {
            _voorVc.invalid("Invalid Decimal");
        }

        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_BigDecimal(v);
    }
    
    protected void set_BigDecimal(BigDecimal v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);
        super.set_BigDecimal(v);
    }
    
    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaDecimalHolder.validateLexical(v, context);
        
        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                // TODO - describe string and pattern here in error
                context.invalid("Decimal (" + v + ") does not match pattern for " + QNameHelper.readable(sType));
            }
        }
    }
    
    /**
     * Performs facet validation only.
     */

    public static void validateValue(BigDecimal v, SchemaType sType, ValidationContext context)
    {
        // fractional digits
        XmlObject fd = sType.getFacet(SchemaType.FACET_FRACTION_DIGITS);
        if (fd != null)
        {
            int scale = ((XmlObjectBase)fd).bigIntegerValue().intValue();
            if (v.scale() > scale)
            {
                context.invalid(
                    "Decimal fractional digits (" + v.scale() + ") does not match " +
                        "fractional digits facet (" + scale + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // total digits
        XmlObject td = sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            String temp = v.unscaledValue().toString();
            int tdf = ((XmlObjectBase)td).bigIntegerValue().intValue();
            int len = temp.length();
            if (len > 0 && temp.charAt(0) == '-')
                len -= 1;
            if (len > tdf)
            {
                context.invalid(
                    "Decimal total digits (" + temp + ") is greater than " +
                        "total digits facet (" + tdf + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            BigDecimal m = ((XmlObjectBase)mine).bigDecimalValue();
            if (v.compareTo(m) <= 0)
            {
                context.invalid(
                    "Decimal (" + v + ") is less than or equal to " +
                        "min exclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // min in
        XmlObject mini = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (mini != null)
        {
            BigDecimal m = ((XmlObjectBase)mini).bigDecimalValue();
            if (v.compareTo(m) < 0)
            {
                context.invalid(
                    "Decimal (" + v + ") is less than " +
                        "min inclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            BigDecimal m = ((XmlObjectBase)maxi).bigDecimalValue();
            if (v.compareTo(m) > 0)
            {
                context.invalid(
                    "Decimal (" + v + ") is greater than " +
                        "max inclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            BigDecimal m = ((XmlObjectBase)maxe).bigDecimalValue();
            if (v.compareTo(m) >= 0)
            {
                context.invalid(
                    "Decimal (" + v + ") is greater than or equal to " +
                        "max exclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // enumeration
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.equals(((XmlObjectBase)vals[i]).bigDecimalValue()))
                    return;
            context.invalid("Decimal (" + v + ") does not match any enumeration values for " + QNameHelper.readable(sType));
        }
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(bigDecimalValue(), schemaType(), ctx);
    }

}
