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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.ValidationContext;


public abstract class JavaHexBinaryHolderEx extends JavaHexBinaryHolder
{
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    public JavaHexBinaryHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected int get_wscanon_rule()
    {
        return schemaType().getWhiteSpaceRule();
    }

    protected void set_text(String s)
    {
        byte[] v;

        if (_validateOnSet())
            v = validateLexical(s, schemaType(), _voorVc);
        else
            v = lex(s, _voorVc);

        if (_validateOnSet() && v != null)
            validateValue(v, schemaType(), XmlObjectBase._voorVc);
        
        super.set_ByteArray(v);

        _value = v;
    }

    // setters
    protected void set_ByteArray(byte[] v)
    {
        if (_validateOnSet())
            validateValue(v, schemaType(), _voorVc);
        
        super.set_ByteArray(v);
    }

    public static void validateValue(byte[] v, SchemaType sType, ValidationContext context)
    {
        int i;
        XmlObject o;

        if ((o = sType.getFacet(SchemaType.FACET_LENGTH)) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) != v.length)
            {
                context.invalid(
                    "Hex encoded data does not have " + i +
                        " bytes per length facet" );
            }
        }

        if ((o = sType.getFacet( SchemaType.FACET_MIN_LENGTH )) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) > v.length)
            {
                context.invalid(
                    "Hex encoded data has only " + v.length +
                        " bytes, fewer than min length facet" );
            }
        }

        if ((o = sType.getFacet( SchemaType.FACET_MAX_LENGTH )) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) < v.length)
            {
                context.invalid(
                    "Hex encoded data has " + v.length +
                        " bytes, more than max length facet" );
            }
        }
        
        XmlObject[] vals = sType.getEnumerationValues();

        if (vals != null)
        {
            enumLoop: for ( i = 0 ; i < vals.length ; i++ )
            {
                byte[] enumBytes = ((XmlObjectBase)vals[i]).byteArrayValue();

                if (enumBytes.length != v.length)
                    continue;

                for ( int j = 0 ; j < enumBytes.length ; j++ )
                    if (enumBytes[j] != v[j])
                        continue enumLoop;
                
                break;
            }
            
            if (i >= vals.length)
                context.invalid("Hex encoded data does not match any of the enumeration values");
        }
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(byteArrayValue(), schemaType(), ctx);
    }
}
