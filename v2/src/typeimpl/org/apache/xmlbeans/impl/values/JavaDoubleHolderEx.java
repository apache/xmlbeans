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



public abstract class JavaDoubleHolderEx extends JavaDoubleHolder
{
    public JavaDoubleHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }
    
    private SchemaType _schemaType;

    public SchemaType schemaType()
        { return _schemaType; }

    protected void set_double(double v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);
        super.set_double(v);
    }

    public static double validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        double d = JavaDoubleHolder.validateLexical(v, context);

        if (!sType.matchPatternFacet(v))
            context.invalid("Double (" + v + ") does not match pattern for " + QNameHelper.readable(sType));
        
        return d;
    }
    
    public static void validateValue(double v, SchemaType sType, ValidationContext context)
    {
        XmlObject x;
        double d;

        if ((x = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)) != null)
        {
            if (compare(v, d = ((XmlObjectBase)x).doubleValue()) <= 0)
            {
                context.invalid(
                    "Double (" + v + ") is less than or equal to " +
                        "min exclusive facet (" + d + ") for " + QNameHelper.readable(sType));
            }
        }

        if ((x = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)) != null)
        {
            if (compare(v, d = ((XmlObjectBase)x).doubleValue()) < 0)
            {
                context.invalid(
                    "Double (" + v + ") is less than " +
                        "min inclusive facet (" + d + ") for " + QNameHelper.readable(sType));
            }
        }
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)) != null)
        {
            if (compare(v, d = ((XmlObjectBase)x).doubleValue()) > 0)
            {
                context.invalid(
                    "Double (" + v + ") is greater than " +
                        "max inclusive facet (" + d + ") for " + QNameHelper.readable(sType));
            }
        }
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)) != null)
        {
            if (compare(v, d = ((XmlObjectBase)x).doubleValue()) >= 0)
            {
                context.invalid(
                    "Double (" + v + ") is greater than or equal to " +
                        "max exclusive facet (" + d + ") for " + QNameHelper.readable(sType));
            }
        }
        
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (compare(v, ((XmlObjectBase)vals[i]).doubleValue()) == 0)
                    return;
            context.invalid("Double (" + v + ") is not a valid enumerated value for " + QNameHelper.readable(sType));
        }
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(doubleValue(), schemaType(), ctx);
    }
}
