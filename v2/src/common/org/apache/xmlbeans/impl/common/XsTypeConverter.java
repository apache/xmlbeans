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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.XmlError;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

public final class XsTypeConverter
{
  private static final String POS_INF_LEX = "INF";
  private static final String NEG_INF_LEX = "-INF";
  private static final String NAN_LEX = "NaN";

  private static final char NAMESPACE_SEP = ':';
  private static final String EMPTY_PREFIX = "";

  // ======================== float ========================
  public static float lexFloat(String v, Collection errors)
  {
    try {
      //current jdk impl of parseFloat calls trim() on the string.
      //Any other space is illegal anyway, whether there are one or more spaces.
      //so no need to do a collapse pass through the string.
      return Float.parseFloat(v);
    } catch (NumberFormatException e) {
      if (v.equals(POS_INF_LEX)) return Float.POSITIVE_INFINITY;
      if (v.equals(NEG_INF_LEX)) return Float.NEGATIVE_INFINITY;
      if (v.equals(NAN_LEX)) return Float.NaN;

      //TODO: i18n
      String msg = "invalid float: " + v;
      errors.add(XmlError.forMessage(msg));

      return Float.NaN;
    }
  }

  public static String printFloat(float value)
  {
    if (value == Float.POSITIVE_INFINITY)
      return POS_INF_LEX;
    else if (value == Float.NEGATIVE_INFINITY)
      return NEG_INF_LEX;
    else if (value == Float.NaN)
      return NAN_LEX;
    else
      return Float.toString(value);
  }


  // ======================== double ========================
  public static double lexDouble(String v, Collection errors)
  {
    try {
      //current jdk impl of parseDouble calls trim() on the string.
      //Any other space is illegal anyway, whether there are one or more spaces.
      //so no need to do a collapse pass through the string.
      return Double.parseDouble(v);
    } catch (NumberFormatException e) {
      if (v.equals(POS_INF_LEX)) return Double.POSITIVE_INFINITY;
      if (v.equals(NEG_INF_LEX)) return Double.NEGATIVE_INFINITY;
      if (v.equals(NAN_LEX)) return Double.NaN;

      //TODO: i18n
      String msg = "invalid double: " + v;
      errors.add(XmlError.forMessage(msg));

      return Double.NaN;
    }
  }

  public static String printDouble(double value)
  {
    if (value == Double.POSITIVE_INFINITY)
      return POS_INF_LEX;
    else if (value == Double.NEGATIVE_INFINITY)
      return NEG_INF_LEX;
    else if (value == Double.NaN)
      return NAN_LEX;
    else
      return Double.toString(value);
  }


  // ======================== decimal ========================
  public static BigDecimal lexDecimal(String v, Collection errors)
  {
    try {
      //TODO: review this
      //NOTE: we trim unneeded zeros from the string because
      //java.math.BigDecimal considers them significant for its
      //equals() method, but the xml value
      //space does not consider them significant.
      //See http://www.w3.org/2001/05/xmlschema-errata#e2-44
      return new BigDecimal(trimTrailingZeros(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid long: " + v;
      errors.add(XmlError.forMessage(msg));
      return new BigDecimal(trimTrailingZeros(v));
    }
  }

  public static String printDecimal(BigDecimal value)
  {
    return value.toString();
  }

  // ======================== integer ========================
  public static BigInteger lexInteger(String v, Collection errors)
  {
    try {
      //TODO: consider special casing zero and one to return static values
      //from BigInteger to avoid object creation.
      return new BigInteger(trimInitialPlus(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid long: " + v;
      errors.add(XmlError.forMessage(msg));
      return BigInteger.ZERO;
    }
  }

  public static String printInteger(BigInteger value)
  {
    return value.toString();
  }

  // ======================== long ========================
  public static long lexLong(String v, Collection errors)
  {
    try {
      return Long.parseLong(trimInitialPlus(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid long: " + v;
      errors.add(XmlError.forMessage(msg));
      return 0L;
    }
  }

  public static String printLong(long value)
  {
    return Long.toString(value);
  }


  // ======================== short ========================
  public static short lexShort(String v, Collection errors)
  {
    try {
      return Short.parseShort(trimInitialPlus(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid short: " + v;
      errors.add(XmlError.forMessage(msg));
      return 0;
    }
  }

  public static String printShort(short value)
  {
    return Short.toString(value);
  }


  // ======================== int ========================
  public static int lexInt(String v, Collection errors)
  {
    try {
      return Integer.parseInt(trimInitialPlus(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid int: " + v;
      errors.add(XmlError.forMessage(msg));
      return 0;
    }
  }

  public static String printInt(int value)
  {
    return Integer.toString(value);
  }


  // ======================== byte ========================
  public static byte lexByte(String v, Collection errors)
  {
    try {
      return Byte.parseByte(trimInitialPlus(v));
    } catch (NumberFormatException e) {
      //TODO: i18n
      String msg = "invalid byte: " + v;
      errors.add(XmlError.forMessage(msg));
      return 0;
    }
  }

  public static String printByte(byte value)
  {
    return Byte.toString(value);
  }


  // ======================== boolean ========================
  public static boolean lexBoolean(String v, Collection errors)
  {
    if ("true".equals(v) || "1".equals(v)) {
      return true;
    } else if ("false".equals(v) || "0".equals(v)) {
      return false;
    } else {
      //TODO: i18n
      String msg = "invalid boolean: " + v;
      errors.add(XmlError.forMessage(msg));
      return false;
    }
  }

  public static String printBoolean(boolean value)
  {
    return (value ? "true" : "false");
  }


  // ======================== string ========================
  public static String lexString(String v, Collection errors)
  {
    return v;
  }

  public static String printString(String value)
  {
    return value;
  }


  // ======================== QName ========================
  public static QName lexQName(String xsd_qname, Collection errors,
                               NamespaceContext nscontext)
  {

    final int idx = indexOfNamespaceSeperator(xsd_qname, errors);
    String prefix;
    String localpart;
    if (idx < 0) {
      prefix = EMPTY_PREFIX;
      localpart = xsd_qname;
    } else {
      prefix = xsd_qname.substring(0, idx);
      localpart = xsd_qname.substring(1 + idx);
    }
    String namespaceUri = getUriFromContext(prefix, xsd_qname,
                                            nscontext, errors);
    return new QName(namespaceUri, localpart, prefix);
  }

  public static String printQName(QName qname, NamespaceContext nsContext,
                                  Collection errors)
  {
    final String uri = qname.getNamespaceURI();
    String prefix = nsContext.getPrefix(uri);
    if (prefix == null) {
      String msg = "NamespaceContext does not provide" +
        " prefix for namespaceURI " + uri;
      errors.add(XmlError.forMessage(msg));
    }
    return getQNameString(uri, qname.getLocalPart(), prefix);
  }

  public static String getQNameString(String uri,
                                      String localpart,
                                      String prefix)
  {
    if (uri != null &&
      prefix != null &&
      prefix.length() > 0) {
      return (prefix + NAMESPACE_SEP + localpart);
    } else {
      return localpart;
    }
  }


  private static String trimInitialPlus(String xml)
  {
    if (xml.charAt(0) == '+') {
      return xml.substring(1);
    } else {
      return xml;
    }
  }

  private static int indexOfNamespaceSeperator(String xml, Collection errors)
  {
    final int idx = xml.indexOf(NAMESPACE_SEP);

    if (idx == 0) {
      String msg = "an empty prefix is not allowed in an xsd:QName value";
      errors.add(XmlError.forMessage(msg));
    }
    return idx;
  }

  private static String getUriFromContext(String prefix,
                                          String original_xml,
                                          NamespaceContext nscontext,
                                          Collection errors)
  {
    String uri = nscontext.getNamespaceURI(prefix);
    if (uri == null) {
      String msg = "invalid qname: " + original_xml;
      errors.add(XmlError.forMessage(msg));
    }

    return uri;
  }


  private static String trimTrailingZeros(String xsd_decimal)
  {
    final int last_char_idx = xsd_decimal.length() - 1;
    if (xsd_decimal.charAt(last_char_idx) == '0') {
      final int last_point = xsd_decimal.lastIndexOf('.');
      if (last_point >= 0) {
        //find last trailing zero
        for (int idx = last_char_idx; idx > last_point; idx--) {
          if (xsd_decimal.charAt(idx) != '0') {
            return xsd_decimal.substring(0, idx + 1);
          }
        }
        //reaching here means the string matched xxx.0*
        return xsd_decimal.substring(0, last_point);
      }
    }
    return xsd_decimal;
  }

  //use only as needed
  public static String stringValue(CharSequence value)
  {
    return value.toString();
  }

}
