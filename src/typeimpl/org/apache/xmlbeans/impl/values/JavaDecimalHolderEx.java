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
