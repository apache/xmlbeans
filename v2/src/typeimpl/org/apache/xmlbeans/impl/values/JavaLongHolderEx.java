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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

public abstract class JavaLongHolderEx extends JavaLongHolder
{
    public JavaLongHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }
        
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    protected void set_text(String s)
    {
        long v;

        try { v = XsTypeConverter.lexLong(s); }
        catch (Exception e) { throw new XmlValueOutOfRangeException(); }
        
        if (_validateOnSet())
        {
            validateValue(v, _schemaType, _voorVc);
            validateLexical(s, _schemaType, _voorVc);
        }

        super.set_long(v);
    }
    
    protected void set_long(long v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_long(v);
    }

    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaDecimalHolder.validateLexical(v, context);
        
        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                context.invalid("Integer (" + v + ") does not match pattern for " + QNameHelper.readable(sType));
            }
        }
    }
    
    private static void validateValue(long v, SchemaType sType, ValidationContext context)
    {
        // total digits
        XmlObject td = sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            long m = getLongValue(td);
            String temp = Long.toString(v);
            int len = temp.length();
            if (len > 0 && temp.charAt(0) == '-')
                len -= 1;
            if (len > m)
            {
                context.invalid(
                    "Integer total digits (" + temp + ") is greater than " +
                        "total digits facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            long m = getLongValue(mine);
            if (!(v > m))
            {
                context.invalid(
                    "Integer (" + v + ") is less than or equal to " +
                        "min exclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // min in
        XmlObject mini = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (mini != null)
        {
            long m = getLongValue(mini);
            if (!(v >= m))
            {
                context.invalid(
                    "Integer (" + v + ") is less than or equal to " +
                        "min exclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            long m = getLongValue(maxi);
            if (!(v <= m))
            {
                context.invalid(
                    "Integer (" + v + ") is less than " +
                        "min inclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            long m = getLongValue(maxe);
            if (!(v < m))
            {
                context.invalid(
                    "Integer (" + v + ") is greater than or equal to " +
                        "max exclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // enumeration
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
            {
                if (v == getLongValue(vals[i]))
                    return;
            }
            context.invalid("Integer (" + v + ") does not match any enumeration values for " + QNameHelper.readable(sType));
        }
    }

    private static long getLongValue(XmlObject o) {
        SchemaType s = o.schemaType();
        switch (s.getDecimalSize()) 
        {
            case SchemaType.SIZE_BIG_DECIMAL:
                return ((XmlObjectBase)o).bigDecimalValue().longValue();
            case SchemaType.SIZE_BIG_INTEGER:
                return ((XmlObjectBase)o).bigIntegerValue().longValue();
            case SchemaType.SIZE_LONG:
                return ((XmlObjectBase)o).longValue();
            default:
                throw new IllegalStateException("Bad facet type: " + s);
        }

    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(longValue(), schemaType(), ctx);
    }
}
