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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.joust.Expression;
import org.apache.xmlbeans.impl.binding.joust.ExpressionFactory;
import org.apache.xmlbeans.impl.binding.joust.Variable;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.values.XmlListImpl;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Helper class for generating a JAX-RPC enumeration type
 */
public class EnumerationPrintHelper
{
  // ===================================================
  // Member variables

  private boolean mArray;
  private ExpressionFactory mExprFactory;
  private int mTypeCode;
  private int mSchemaTypeCode;
  private boolean mPrimitive;

  // ====================================================
  // Constants

  // For each of the "simple" (from JAX-RPC perspective) Java types, we
  // have to have custom code that deals with generating the right strings
  // to use in the generated file. Tedious.
  // This is the list of currently supported Java types

  private static final int T_STRING        =  1; // java.lang.String
  private static final int T_GDURATION     =  2; // org.apache.xmlbeans.GDuration
  private static final int T_CALENDAR      =  3; // java.util.Calendar
  private static final int T_BOOLEAN       =  4; // boolean
  private static final int T_FLOAT         =  5; // float
  private static final int T_DOUBLE        =  6; // double
  private static final int T_BIGDECIMAL    =  7; // java.math.BigDecimal
  private static final int T_BIGINTEGER    =  8; // java.math.BigInteger
  private static final int T_LONG          =  9; // long
  private static final int T_INT           = 10; // int
  private static final int T_SHORT         = 11; // short
  private static final int T_BYTE          = 12; // byte
  private static final int T_QNAME         = 13; // javax.xml.namespace.QName
  private static final int T_FLOAT_CLASS   = 14; // java.lang.Float
  private static final int T_DOUBLE_CLASS  = 15; // java.lang.Double
  private static final int T_LONG_CLASS    = 16; // java.lang.Long
  private static final int T_INT_CLASS     = 17; // java.lang.Integer
  private static final int T_SHORT_CLASS   = 18; // java.lang.Short
  private static final int T_BYTE_CLASS    = 19; // java.lang.Byte
  private static final int T_BOOLEAN_CLASS = 20; // java.lang.Boolean
  private static final int T_URI           = 21; // java.net.URI
  private static final int T_BYTE_ARRAY    = 22; // byte[]

  EnumerationPrintHelper(JavaTypeName typeName, ExpressionFactory exprFactory, SchemaType schemaType)
  {
    mSchemaTypeCode = extractTypeCode(schemaType);
    if (mSchemaTypeCode == SchemaType.BTC_BASE_64_BINARY ||
      mSchemaTypeCode == SchemaType.BTC_HEX_BINARY)
      mArray = typeName.getArrayDepth() > 1;
    else
      mArray = typeName.getArrayDepth() > 0;

    mExprFactory = exprFactory;
    String t;
    if (mArray)
      t = typeName.getArrayItemType(1).toString();
    else
      t = typeName.toString();

    switch (t.charAt(0))
    {
      case 'j':
      if (t.equals("java.lang.String"))
        mTypeCode = T_STRING;
      else if (t.equals("java.util.Calendar"))
        mTypeCode = T_CALENDAR;
      else if (t.equals("java.math.BigDecimal"))
        mTypeCode = T_BIGDECIMAL;
      else if (t.equals("java.math.BigInteger"))
        mTypeCode = T_BIGINTEGER;
      else if (t.equals("javax.xml.namespace.QName"))
        mTypeCode = T_QNAME;
      else if (t.equals("java.net.URI"))
        mTypeCode = T_URI;
      else if (t.equals(Float.class.getName()))
        mTypeCode = T_FLOAT_CLASS;
      else if (t.equals(Double.class.getName()))
        mTypeCode = T_DOUBLE_CLASS;
      else if (t.equals(Long.class.getName()))
        mTypeCode = T_LONG_CLASS;
      else if (t.equals(Integer.class.getName()))
        mTypeCode = T_INT_CLASS;
      else if (t.equals(Short.class.getName()))
        mTypeCode = T_SHORT_CLASS;
      else if (t.equals(Byte.class.getName()))
        mTypeCode = T_BYTE_CLASS;
      else if (t.equals(Boolean.class.getName()))
        mTypeCode = T_BOOLEAN_CLASS;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'b':
      if (t.equals("boolean"))
        mTypeCode = T_BOOLEAN;
      else if (t.equals("byte"))
        mTypeCode = T_BYTE;
      else if (t.equals("byte[]"))
        mTypeCode = T_BYTE_ARRAY;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'f':
      if (t.equals("float"))
        mTypeCode = T_FLOAT;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'd':
      if (t.equals("double"))
        mTypeCode = T_DOUBLE;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'l':
      if (t.equals("long"))
        mTypeCode = T_LONG;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'i':
      if (t.equals("int"))
        mTypeCode = T_INT;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 's':
      if (t.equals("short"))
        mTypeCode = T_SHORT;
      else
        throw new IllegalArgumentException("Unknown simple type: " + t);
      break;
      case 'o':
      if (t.equals("org.apache.xmlbeans.GDuration"))
        mTypeCode = T_GDURATION;
      break;
      default:
        throw new IllegalArgumentException("Unknown simple type: " + t);
    }
    switch (mTypeCode)
    {
      case T_BOOLEAN:
      case T_BYTE:
      case T_FLOAT:
      case T_DOUBLE:
      case T_LONG:
      case T_INT:
      case T_SHORT:
        mPrimitive = true;
    }
  }

  /**
   * Returns the init expression corresponding to a value of type mJavaType
   * and XML representation value
   * Ex value="abc" --> "abc"
   *    value="123" --> 123
   *    value="456" --> new Float(456)
   */
  public Expression getInitExpr(XmlAnySimpleType value) {
    if (mArray)
    {
      StringBuffer arrayInit = new StringBuffer();
      arrayInit.append('{');
      for (Iterator it = ((XmlListImpl) value).xlistValue().iterator(); it.hasNext(); )
      {
        Expression exprInit = getInitExprHelper((XmlAnySimpleType) it.next());
        arrayInit.append(exprInit.getMemento().toString()).append(',').append(' ');
      }
      arrayInit.append('}');
      return mExprFactory.createVerbatim(arrayInit.toString());
    }
    else
      return getInitExprHelper(value);
  }

  private Expression getInitExprHelper(XmlAnySimpleType value) {
    Expression result = null;
    switch (mTypeCode)
    {
      case T_STRING:
        result = mExprFactory.createVerbatim("\"" + value.getStringValue() + "\"");
        break;
      case T_CALENDAR:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexDateTime(\"" + value.getStringValue() + "\")");
        break;
      case T_BIGDECIMAL:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexDecimal(\"" + value.getStringValue() + "\")");
        break;
      case T_BIGINTEGER:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexInteger(\"" + value.getStringValue() + "\")");
        break;
      case T_QNAME:
        QName qName = ((XmlQName) value).getQNameValue();
        result = mExprFactory.createVerbatim("new javax.xml.namespace.QName(\"" +
                qName.getNamespaceURI() + "\", \"" +
                qName.getLocalPart() + "\", \"" +
                qName.getPrefix() + "\")");
        break;
      case T_URI:
        result = mExprFactory.createVerbatim("new java.net.URI(\"" + value.getStringValue() + "\")");
        break;
      case T_FLOAT_CLASS:
        result = mExprFactory.createVerbatim("new Float(" + ((XmlFloat) value).getFloatValue() + ")");
        break;
      case T_DOUBLE_CLASS:
        result = mExprFactory.createVerbatim("new Double(" + ((XmlDouble) value).getDoubleValue() + ")");
        break;
      case T_LONG_CLASS:
        result = mExprFactory.createVerbatim("new Long(" + ((XmlLong) value).getLongValue() + ")");
        break;
      case T_INT_CLASS:
        result = mExprFactory.createVerbatim("new Integer(" + ((XmlInt) value).getIntValue() + ")");
        break;
      case T_SHORT_CLASS:
        result = mExprFactory.createVerbatim("new Short(" + ((XmlShort) value).getShortValue() + ")");
        break;
      case T_BYTE_CLASS:
        result = mExprFactory.createVerbatim("new Byte(" + ((XmlByte) value).getByteValue() + ")");
        break;
      case T_BOOLEAN_CLASS:
        result = mExprFactory.createVerbatim("new Boolean(" + ((XmlBoolean) value).getBooleanValue() + ")");
        break;
      case T_BOOLEAN:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlBoolean) value).getBooleanValue()));
        break;
      case T_BYTE:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlByte) value).getByteValue()));
        break;
      case T_FLOAT:
        result = mExprFactory.createVerbatim("(float)" + String.valueOf(((XmlFloat) value).getFloatValue()));
        break;
      case T_DOUBLE:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlDouble) value).getDoubleValue()));
        break;
      case T_LONG:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlLong) value).getLongValue()));
        break;
      case T_INT:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlInt) value).getIntValue()));
        break;
      case T_SHORT:
        result = mExprFactory.createVerbatim(String.valueOf(((XmlShort) value).getShortValue()));
        break;
      case T_BYTE_ARRAY:
        {
          StringBuffer arrayInit = new StringBuffer();
          arrayInit.append('{');
          byte[] bytes = null;
          if (mSchemaTypeCode == SchemaType.BTC_BASE_64_BINARY)
            bytes = ((XmlBase64Binary) value).getByteArrayValue();
          else if (mSchemaTypeCode == SchemaType.BTC_HEX_BINARY)
            bytes = ((XmlHexBinary) value).getByteArrayValue();
          if (bytes == null)
            break;
          for (int i = 0; i < bytes.length; i++)
            arrayInit.append(bytes[i]).append(',').append(' ');
          arrayInit.append('}');
          result = mExprFactory.createVerbatim(arrayInit.toString());
        }
        break;
      case T_GDURATION:
        result = mExprFactory.createVerbatim("new org.apache.xmlbeans.GDuration(\"" + value.getStringValue() + "\")");
        break;
      default:
    }
    if (result == null)
      throw new IllegalStateException();
    return result;
  }

  public Expression getFromStringExpr(Expression param) {
    Expression result = null;
    String s = param.getMemento().toString();
    switch (mTypeCode)
    {
      case T_STRING:
        result = param;
        break;
      case T_CALENDAR:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexDateTime(" + s + ")");
        break;
      case T_BIGDECIMAL:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexDecimal(" + s + ")");
        break;
      case T_BIGINTEGER:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexInteger(" + s + ")");
        break;
      case T_QNAME:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexQName(" + s +
                ", new javax.xml.namespace.NamespaceContext() {public String getNamespaceURI(String p) {return \"\";} public String getPrefix(String u) {return null;} public java.util.Iterator getPrefixes(String s) {return null;}})");
        break;
      case T_URI:
        result = mExprFactory.createVerbatim("new java.net.URI(" + s + ")");
        break;
      case T_FLOAT_CLASS:
        result = mExprFactory.createVerbatim("new Float(XsTypeConverter.lexFloat(" + s + "))");
        break;
      case T_DOUBLE_CLASS:
        result = mExprFactory.createVerbatim("new Double(XsTypeConverter.lexDouble(" + s + "))");
        break;
      case T_LONG_CLASS:
        result = mExprFactory.createVerbatim("new Long(XsTypeConverter.lexLong(" + s + "))");
        break;
      case T_INT_CLASS:
        result = mExprFactory.createVerbatim("new Integer(XsTypeConverter.lexInt(" + s + "))");
        break;
      case T_SHORT_CLASS:
        result = mExprFactory.createVerbatim("new Short(XsTypeConverter.lexShort(" + s + "))");
        break;
      case T_BYTE_CLASS:
        result = mExprFactory.createVerbatim("new Byte(XsTypeConverter.lexByte(" + s + "))");
        break;
      case T_BOOLEAN_CLASS:
        result = mExprFactory.createVerbatim("new Boolean(XsTypeConverter.lexBoolean(" + s + "))");
        break;
      case T_BOOLEAN:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexBoolean(" + s + ")");
        break;
      case T_BYTE:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexByte(" + s + ")");
        break;
      case T_FLOAT:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexFloat(" + s + ")");
        break;
      case T_DOUBLE:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexDouble(" + s + ")");
        break;
      case T_LONG:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexLong(" + s + ")");
        break;
      case T_INT:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexInt(" + s + ")");
        break;
      case T_SHORT:
        result = mExprFactory.createVerbatim("XsTypeConverter.lexShort(" + s + ")");
        break;
      case T_GDURATION:
        result = mExprFactory.createVerbatim("new org.apache.xmlbeans.GDuration(" + s + ")");
      case T_BYTE_ARRAY:
        if (mSchemaTypeCode == SchemaType.BTC_BASE_64_BINARY)
          result = mExprFactory.createVerbatim("org.apache.xmlbeans.impl.util.Base64.encode("+
            s + ".getBytes())");
        else if (mSchemaTypeCode == SchemaType.BTC_HEX_BINARY)
          result = mExprFactory.createVerbatim("org.apache.xmlbeans.impl.util.HexBin.stringToBytes(" +
            s + ")");
        break;
      default:
    }
    if (result == null)
      throw new IllegalStateException();
    return result;
  }

  public String getToXmlString(Variable var, String index) {
    assert mArray : "This method can only be called for a list enumeration";
    return getToXmlExpr(mExprFactory.createVerbatim(var.getMemento().toString() +
        "[" + index + "]")).getMemento().toString();
  }

  public Expression getToXmlExpr(Expression var) {
    Expression result = null;
    String s = var.getMemento().toString();
    switch (mTypeCode)
    {
      case T_STRING:
        result = mExprFactory.createVerbatim("XsTypeConverter.printString(" + s + ").toString()");
        break;
      case T_CALENDAR:
        result = mExprFactory.createVerbatim("XsTypeConverter.printDateTime(" + s + ", " + mSchemaTypeCode +").toString()");
        break;
      case T_BIGDECIMAL:
        result = mExprFactory.createVerbatim("XsTypeConverter.printDecimal(" + s + ").toString()");
        break;
      case T_BIGINTEGER:
        result = mExprFactory.createVerbatim("XsTypeConverter.printInteger(" + s + ").toString()");
        break;
      case T_QNAME:
        result = mExprFactory.createVerbatim("XsTypeConverter.getQNameString(" + s +
                ".getNamespaceURI(), " + s + ".getLocalPart(), " + s + ".getPrefix())");
        break;
      case T_URI:
        result = mExprFactory.createVerbatim(s + ".toString()");
        break;
      case T_FLOAT_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printFloat(" + s + ".floatValue()).toString()");
        break;
      case T_DOUBLE_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printDouble(" + s + ".doubleValue()).toString()");
        break;
      case T_LONG_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printLong(" + s + ".longValue()).toString()");
        break;
      case T_INT_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printInt(" + s + ".intValue()).toString()");
        break;
      case T_SHORT_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printShort(" + s + ".shortValue()).toString()");
        break;
      case T_BYTE_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printByte(" + s + ".byteValue()).toString()");
        break;
      case T_BOOLEAN_CLASS:
        result = mExprFactory.createVerbatim("XsTypeConverter.printBoolean(" + s + ".booleanValue()).toString()");
        break;
      case T_BOOLEAN:
        result = mExprFactory.createVerbatim("XsTypeConverter.printBoolean(" + s + ").toString()");
        break;
      case T_BYTE:
        result = mExprFactory.createVerbatim("XsTypeConverter.printByte(" + s + ").toString()");
        break;
      case T_FLOAT:
        result = mExprFactory.createVerbatim("XsTypeConverter.printFloat(" + s + ").toString()");
        break;
      case T_DOUBLE:
        result = mExprFactory.createVerbatim("XsTypeConverter.printDouble(" + s + ").toString()");
        break;
      case T_LONG:
        result = mExprFactory.createVerbatim("XsTypeConverter.printLong(" + s + ").toString()");
        break;
      case T_INT:
        result = mExprFactory.createVerbatim("XsTypeConverter.printInt(" + s + ").toString()");
        break;
      case T_SHORT:
        result = mExprFactory.createVerbatim("XsTypeConverter.printShort(" + s + ").toString()");
        break;
      case T_GDURATION:
        result = mExprFactory.createVerbatim(s + ".toString()");
        break;
      case T_BYTE_ARRAY:
        if (mSchemaTypeCode == SchemaType.BTC_BASE_64_BINARY)
          result = mExprFactory.createVerbatim("XsTypeConverter.printBase64Binary(" + s + ").toString()");
        else if (mSchemaTypeCode == SchemaType.BTC_HEX_BINARY)
          result = mExprFactory.createVerbatim("XsTypeConverter.printHexBinary(" + s + ").toString()");
        break;
      default:
    }
    if (result == null)
      throw new IllegalStateException();
    else
      return result;
  }

  public String getObjectVersion(String var) {
    String result = null;
    if (mPrimitive) {
    switch (mTypeCode) {
      case T_BOOLEAN:
      result = "new Boolean(" + var + ")";
      break;
      case T_BYTE:
      result = "new Byte(" + var + ")";
      break;
      case T_FLOAT:
      result = "new Float(" + var + ")";
      break;
      case T_DOUBLE:
      result = "new Double(" + var + ")";
      break;
      case T_LONG:
      result = "new Long(" + var + ")";
      break;
      case T_INT:
      result = "new Integer(" + var + ")";
      break;
      case T_SHORT:
      result = "new Short(" + var + ")";
      break;
    }}
    else
      result = var;
    if (result == null)
      throw new IllegalStateException();
    else
      return result;
  }

  public String getEquals(String var1, String var2) {
    String result = null;
    if (mTypeCode == T_BYTE_ARRAY)
      result = "java.util.Arrays.equals(" + var1 + ", " + var2 + ")";
    else if (mPrimitive)
      result = var1 + " == " + var2;
    else
      result = var1 + ".equals(" + var2 + ")";
    return result;
  }

  public Expression getHashCode(String var) {
    Expression result = null;
    if (mPrimitive) {
    switch (mTypeCode) {
      case T_BOOLEAN:
      result = mExprFactory.createVerbatim(var + " ? 1 : 0");
      break;
      case T_BYTE:
      result = mExprFactory.createVerbatim("(int) " + var);
      break;
      case T_FLOAT:
      result = mExprFactory.createVerbatim("Float.floatToIntBits(" + var + ")");
      break;
      case T_DOUBLE:
      result = mExprFactory.createVerbatim("(int) (Double.doubleToLongBits(" + var + ") ^ (Double.doubleYoLongBits(" + var + ") >>> 32))");
      break;
      case T_LONG:
      result = mExprFactory.createVerbatim("(int) (" + var + " ^ (" + var + " >>> 32))");
      break;
      case T_INT:
      result = mExprFactory.createVerbatim(var);
      break;
      case T_SHORT:
      result = mExprFactory.createVerbatim("(int) " + var);
      break;
      }
    }
    else if (mTypeCode == T_BYTE_ARRAY)
      result = mExprFactory.createVerbatim(var + ".length > 0 ? (int)" + var + "[0] : 0");
    else
      result = mExprFactory.createVerbatim(var + ".hashCode()");
    if (result == null)
      throw new IllegalStateException();
    else
      return result;
  }

  public boolean isQName()
  { return mTypeCode == T_QNAME; }

  public boolean isArray()
  { return mArray; }

  public boolean isBinary()
  {
    return mSchemaTypeCode == SchemaType.BTC_BASE_64_BINARY ||
           mSchemaTypeCode == SchemaType.BTC_HEX_BINARY;
  }

  private int extractTypeCode(SchemaType sType) {
    boolean done = false;
    while (!done) {
      switch (sType.getSimpleVariety()) {
        case SchemaType.ATOMIC:
          done = true;
          break;
        case SchemaType.UNION:
          throw new IllegalArgumentException("Unions are not currently supported");
        case SchemaType.LIST:
          sType = sType.getListItemType();
          break;
        default:
          throw new IllegalStateException();
      }
    }
    return sType.getPrimitiveType().getBuiltinTypeCode();
  }
}
