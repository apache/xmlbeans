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

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Arrays;

public abstract class JavaHexBinaryHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_HEX_BINARY; }

    protected byte[] _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets raw text value
    protected String compute_text(NamespaceManager nsm)
    {
        return new String(HexBin.encode(_value));
    }
    protected void set_text(String s)
    {
        _hashcached = false;
        if (_validateOnSet())
            _value = validateLexical(s, schemaType(), _voorVc);
        else
            _value = lex(s, _voorVc);
    }
    protected void set_nil()
    {
        _hashcached = false;
        _value = null;
    }

    public static byte[] lex(String v, ValidationContext context)
    {
        byte[] bytes = HexBin.decode(v.getBytes());

        if (bytes == null)
        {
            // TODO - get a decent error with line numbers and such here
            context.invalid("Hex encoded data not encoded properly");
        }

        return bytes;
    }

    public static byte[] validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        byte[] bytes = lex(v, context);

        if (bytes == null)
            return null;
        
        if (!sType.matchPatternFacet(v))
        {
            context.invalid( "Hex encoded data does not match pattern for " + QNameHelper.readable(sType));
            return null;
        }

        return bytes;
    }

    public byte[] byteArrayValue()
    {
        check_dated();
        if (_value == null)
            return null;

        byte[] result = new byte[_value.length];
        System.arraycopy(_value, 0, result, 0, _value.length);
        return result;
    }

    // setters
    protected void set_ByteArray(byte[] ba)
    {
        _hashcached = false;
        _value = new byte[ba.length];
        System.arraycopy(ba, 0, _value, 0, ba.length);
    }

    // comparators
    protected boolean equal_to(XmlObject i)
    {
        byte[] ival = ((XmlHexBinary) i).getByteArrayValue();
        return Arrays.equals(_value, ival);
    }

    //because computing hashcode is expensive we'll cache it
    protected boolean _hashcached = false;
    protected int hashcode = 0;
    protected static MessageDigest md5;
    static
    {
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        }
        catch( NoSuchAlgorithmException e )
        {
            throw new IllegalStateException("Cannot find MD5 hash Algorithm");
        }
    }

    protected int value_hash_code()
    {
        if( _hashcached )
            return hashcode;

        _hashcached = true;

        if( _value == null )
            return hashcode = 0;

        byte[] res = md5.digest(_value);
        return hashcode = res[0]<<24 + res[1]<<16 + res[2]<<8 + res[3];
    }

}
