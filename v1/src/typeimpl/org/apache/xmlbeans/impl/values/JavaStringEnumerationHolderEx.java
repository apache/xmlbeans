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
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;


public abstract class JavaStringEnumerationHolderEx extends JavaStringHolderEx
{
    public JavaStringEnumerationHolderEx(SchemaType type, boolean complex)
    {
        super(type, complex);
    }

    private StringEnumAbstractBase _val;

    // update the intval when setting via text, nil.
    protected void set_text(String s)
    {
        StringEnumAbstractBase enum = schemaType().enumForString(s);
        if (enum == null)
            throw new XmlValueOutOfRangeException("String '" + s + "' is not a valid enumerated value for " + schemaType());

        super.set_text(s);
        _val = enum;
    }

    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaStringHolderEx.validateLexical(v, sType, context);

        if (sType.hasStringEnumValues())
        {
            if (sType.enumForString(v) == null)
            {
                if (context != null)
                {
                    context.invalid("String '" + v + "' is not a valid enumerated value for " + QNameHelper.readable(sType));
                }
            }
        }
    }
    
    protected void set_nil()
    {
        _val = null;
        super.set_nil();
    }

    // set/get the enum
    public StringEnumAbstractBase enumValue()
    {
        check_dated();
        return _val;
    }

    protected void set_enum(StringEnumAbstractBase enum)
    {
        Class ejc = schemaType().getEnumJavaClass();
        if (ejc != null && !enum.getClass().equals(ejc))
            throw new XmlValueOutOfRangeException();

        super.set_text(enum.toString());
        _val = enum;
    }
}