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

package org.apache.xmlbeans.impl.jaxb.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.DatatypeConverterInterface;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.ParseUtil;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlTime;

import org.apache.xmlbeans.impl.values.JavaGDateHolderEx;
import org.apache.xmlbeans.impl.values.JavaBase64Holder;
import org.apache.xmlbeans.impl.values.JavaHexBinaryHolder;
import org.apache.xmlbeans.impl.values.JavaIntegerHolder;
import org.apache.xmlbeans.impl.values.JavaDoubleHolder;
import org.apache.xmlbeans.impl.values.JavaFloatHolder;
import org.apache.xmlbeans.impl.values.JavaBooleanHolder;
import org.apache.xmlbeans.impl.values.JavaQNameHolder;


/* 
   Implementation note:

   The parse methods all return null when possible and throw NPE when the
   return type is a scalar. I don't know why they do this, but that's what
   the JAXB 1.0 RI does, so I'm copying. It seems these should have declared
   exceptions, but they don't.
   
*/
   

public class DatatypeConverterImpl implements DatatypeConverterInterface
{

    private DatatypeConverterImpl() {}
    public static DatatypeConverterImpl instance = new DatatypeConverterImpl();

    public String printDecimal(java.math.BigDecimal value)
    {
        return value.toString();
    }
    public String printQName(QName value, NamespaceContext nsc)
    {
        if (nsc == null)
            throw new IllegalArgumentException();

        final String localpart = value.getLocalPart();
        final String uri = value.getNamespaceURI();

        final String prefix = nsc.getPrefix(uri);

        if (prefix == null)
            // throw new MarshalException("Namespace uri '" + uri + "' not bound in current context");
            return null;

        return prefix + ":" + localpart;

    }
    public String printFloat(float value)
    {
        return JavaFloatHolder.serialize(value);
    }

    public String printDouble(double value)
    {
        return JavaDoubleHolder.serialize(value);
    }

    public String printBoolean(boolean value)
    {
        return value ? "true" : "false";
    }
    public String printByte(byte value)
    {
        return Byte.toString(value);
    }
    public String printDateTime(java.util.Calendar value)
    {
        return new GDate(value).toString();

    }
    public String printBase64Binary(byte[] value)
    {
        return new String(Base64.encode(value));
    }

    public String printHexBinary(byte[] value)
    {
        return new String(HexBin.encode(value));
    }

    public String printUnsignedInt(long value)
    {
        return Long.toString(value);
    }

    public String printUnsignedShort(int value)
    {
        return Integer.toString(value);
    }

    public String printTime(java.util.Calendar value)
    {
        return new GDate(value).toString();
    }

    public String printDate(java.util.Calendar value)
    {
        return new GDate(value).toString();
    }

    public String printAnySimpleType(String value)
    {
        return value;
    }

    public String printString(String s)
    {
        return s;
    }

    public String printInteger(java.math.BigInteger s)
    {
        return s.toString();
    }

    public String printInt(int s)
    {
        return Integer.toString(s);
    }

    public String printLong(long s)
    {
        return Long.toString(s);
    }

    public String printShort(short s)
    {
        return Short.toString(s);
    }

    public String parseString(String value)
    {
        return value;
    }

    public java.util.Calendar parseDateTime(String s)
    {
        GDate date = JavaGDateHolderEx.lex(s, XmlDateTime.type, _evc);
        return date.getCalendar();
    }

    public byte[] parseBase64Binary(String s)
    {
        return JavaBase64Holder.lex(s, _evc);
    }

    public byte[] parseHexBinary(String s)
    {
        return JavaHexBinaryHolder.lex(s, _evc);
    }

    public long parseUnsignedInt(String s)
    {
        try { return Long.parseLong(ParseUtil.trimInitialPlus(s)); }
        catch (Exception e) { throw new NullPointerException("invalid unsigned int " + s); }
    }

    public int parseUnsignedShort(String s)
    {
        try { return Integer.parseInt(ParseUtil.trimInitialPlus(s)); }
        catch (Exception e) { throw new NullPointerException("invalid unsigned short " + s); }
    }

    public java.util.Calendar parseTime(String s)
    {
        GDate date = JavaGDateHolderEx.lex(s, XmlTime.type, _evc);
        return date.getCalendar();
    }

    public java.math.BigInteger parseInteger(String s)
    {
        return JavaIntegerHolder.lex(s, _evc);
    }

    public java.math.BigDecimal parseDecimal(String s)
    {
        try { return new BigDecimal(s); }
        catch (Exception e) { return null; }
    }

    public int parseInt(String s)
    {
        try { return Integer.parseInt(ParseUtil.trimInitialPlus(s)); }
        catch (Exception e) { throw new NullPointerException("invalid int " + s); }
    }

    public long parseLong(String s)
    {
        try { return Long.parseLong(ParseUtil.trimInitialPlus(s)); }
        catch (Exception e) { throw new NullPointerException("invalid long " + s); }
    }

    public short parseShort(String s)
    {
        try { return Short.parseShort(s); }
        catch (Exception e) { throw new NullPointerException("invalid short " + s); }
    }

    public float parseFloat(String s)
    {
        return JavaFloatHolder.validateLexical(s, _evc);
    }

    public double parseDouble(String s)
    {
        return JavaDoubleHolder.validateLexical(s, _evc);
    }

    public boolean parseBoolean(String s)
    {
        return JavaBooleanHolder.validateLexical(s, _evc);
    }

    public byte parseByte(String s)
    {
        try { return Byte.parseByte(s); }
        catch(Exception e) { throw new NullPointerException("invalid byte " + s); }
    }

    public QName parseQName(String s, NamespaceContext c)
    {
        return JavaQNameHolder.validateLexical(s, _evc, 
            new NamespaceContextWrapper(c));
    }

    public java.util.Calendar parseDate(String s)
    {
        return JavaGDateHolderEx.lex(s, XmlDate.type, _evc).getCalendar();
    }

    public String parseAnySimpleType(String s)
    {
        return s;
    }


    public static final ValidationContext _evc = new ExceptionValidationContext();
    /**
     * Use _evc when you want to throw an Exception when
     * parsing a simple type.
     */
    private static final class ExceptionValidationContext implements ValidationContext
    {
        public void invalid(String message)
        {
            throw new NullPointerException( message );
        }
    }
}
