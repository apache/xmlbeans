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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.ParseUtil;

public abstract class JavaIntHolderEx extends JavaIntHolder
{
    public JavaIntHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }
        
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    protected void set_text(String s)
    {
        int v;

        try { v = Integer.parseInt(ParseUtil.trimInitialPlus(s)); }
        catch (Exception e) { throw new XmlValueOutOfRangeException(); }
        
        if (_validateOnSet())
        {
            validateValue(v, _schemaType, _voorVc);
            validateLexical(s, _schemaType, _voorVc);
        }

        super.set_int(v);
    }
    
    protected void set_int(int v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_int(v);
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
    
    private static void validateValue(int v, SchemaType sType, ValidationContext context)
    {
        // total digits
        XmlObject td = sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            String temp = Integer.toString(v);
            int len = temp.length();
            if (len > 0 && temp.charAt(0) == '-')
                len -= 1;
            int m = getIntValue(td);
            if (len > m)
            {
                context.invalid(
                    "Integer total digits (" + temp + ") is greater than " +
                        "total digits facet (" + getIntValue(td) + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            int m = getIntValue(mine);
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
            int m = getIntValue(mini);
            if (!(v >= m))
            {
                context.invalid(
                    "Integer (" + v + ") is less than " +
                        "min inclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            int m = getIntValue(maxi);
            if (!(v <= m))
            {
                context.invalid(
                    "Integer (" + v + ") is greater than " +
                        "max inclusive facet (" + m + ") for " + QNameHelper.readable(sType));
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            int m = getIntValue(maxe);
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
                if (v == getIntValue(vals[i]))
                    return;
            }
            context.invalid("Integer (" + v + ") does not match any enumeration values for " + QNameHelper.readable(sType));
        }
    }

    private static int getIntValue(XmlObject o) {
        SchemaType s = o.schemaType();
        switch (s.getDecimalSize()) 
        {
            case SchemaType.SIZE_BIG_DECIMAL:
                return ((XmlObjectBase)o).bigDecimalValue().intValue();
            case SchemaType.SIZE_BIG_INTEGER:
                return ((XmlObjectBase)o).bigIntegerValue().intValue();
            case SchemaType.SIZE_LONG:
                return (int)((XmlObjectBase)o).longValue();
            default:
                return ((XmlObjectBase)o).intValue();
        }

    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(intValue(), schemaType(), ctx);
    }
    
}

