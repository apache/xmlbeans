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
package org.apache.xmlbeans.impl.jam.annotation;

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;

import java.util.Map;

/**
 * <p>This is basically just read-only HashMap with typed accessor/converters
 * provided for convenience.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ValueMap {

  // ========================================================================
  // Variables

  private Map mName2Value;

  // ========================================================================
  // Constructor

  public ValueMap(Map name2Value) {
    if (name2Value == null) throw new IllegalArgumentException("null map");
    mName2Value = name2Value;
  }

  // ========================================================================
  // ValueMap implementation

  public String[] getMemberNames() {
    String[] out = new String[mName2Value.keySet().size()];
    mName2Value.keySet().toArray(out);
    return out;
  }

  public JAnnotation getValueAsAnnotation(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v instanceof JAnnotation) return (JAnnotation)v;
    return null;
  }

  public String getValueAsString(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return null;
    return v.toString();
  }

  /**
   * <p>Returns the member's value as a boolean.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt(), except that IllegalArgumentException is
   * thrown instead of NumberFormatException.</p>
   */
  public boolean getValueAsBoolean(String memberName) throws IllegalArgumentException {
    Object v = mName2Value.get(memberName);
    if (v == null) return false;
    return Boolean.valueOf(v.toString().trim()).booleanValue();
  }


  /**
   * <p>Returns the member's value as an int.  If the value is not known to be
   * an int, (because it's a javadoc tag or because it's a 175 annotation
   * member of a type other than int) getValueAsString() is called.  If the result is
   * null, NumberFormatException is thrown.  Otherwise, the String is
   * converted to an int with Integer.valueOf(), which again may throw
   * NumberFormatException.</p>
   */
  public int getValueAsInt(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).intValue();
    try {
      return Integer.parseInt(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  /**
   * <p>Returns the member's value as a long.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public long getValueAsLong(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).longValue();
    try {
      return Long.parseLong(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }


  /**
   * <p>Returns the member's value as a short.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public short getValueAsShort(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).shortValue();
    try {
      return Short.parseShort(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  /**
   * <p>Returns the member's value as a double.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public double getValueAsDouble(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).doubleValue();
    try {
      return Double.parseDouble(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  /**
   * <p>Returns the member's value as a float.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public float getValueAsFloat(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).floatValue();
    try {
      return Float.parseFloat(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  /**
   * <p>Returns the member's value as a byte.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public byte getValueAsByte(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Number) return ((Number)v).byteValue();
    try {
      return Byte.parseByte(v.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  /**
   * <p>Returns the member's value as a char.  If necessary, type
   * conversion is performed by calling getStringValue().  If the result
   * is null or is a String that is not exactly of length 1,
   * IllegalArgumentException is thrown.</p>
   */
  public char getValueAsChar(String memberName) {
    Object v = mName2Value.get(memberName);
    if (v == null) return 0;
    if (v instanceof Character) return ((Character)v).charValue();
    v = v.toString();
    return (((String)v).length() == 0) ? 0 : ((String)v).charAt(0);
  }

  /**
   * <p>If the named member is known to be of an array type, returns the value
   * as an array of Objects.  If the array component type is primitive,
   * the array objects will be instances of an appropriate java.lang
   * wrapper (e.g., Integer).  Returns null if the member type is
   * not an array.</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public Object[] getValueAsArray(String memberName) {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>If the named member is known to be an array of classes, returns an
   * array of JClass representations of those classes.  If the memeber
   * is known to be an array of a simple non-array type, this method
   * will call getValueAsStringArray() and attempt to return a JClass
   * by treating each string in the returned array as a qualified classname.
   * Returns null otherwise.
   * </p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public JClass[] getValueAsClassArray(String memberName) {
    throw new IllegalStateException("NYI");

  }

  /**
   * <p>If the named member is known to be an array of annotations (i.e.
   * complex, nested types), this method returns an array containing
   * each complex value as a JAnnotation.  Returns null in all other cases.
   * </p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public JAnnotation[] getValueAsAnnotationArray(String memberName) {
    throw new IllegalStateException("NYI");
  }

    /**
   * <p>Returns the named member's value as an array of Strings.  If the named member is
   * not of an array type, or is an array of arrays or complex annotations,
   * this method returns null.  If it is an array of a simple, non-array type
   * other than String, conversion on each component will be attempted as
   * described under getStringValue().</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public String[] getValueAsStringArray(String memberName) {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of ints.  If the named member is
   * not of an array type, or is an array of arrays or complex annotations,
   * this method returns null.  If it is an array of a simple, non-array type
   * other than int, conversion on each component will be attempted as
   * described under getIntValue().</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public int[] getValueAsIntArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
  }

    /**
   * <p>Returns the named member's value as an array of booleans.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray(), except that IllegalArgumentException may be
   * thrown instead of NumberFormatException.</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public boolean[] getValueAsBooleanArray(String memberName) throws IllegalArgumentException {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of shorts.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public short[] getValueAsShortArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
    }

  /**
   * <p>Returns the named member's value as an array of longs.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public long[] getValueAsLongArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of doubles.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public double[] getValueAsDoubleArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of floats.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public float[] getValueAsFloatArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of bytes.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public byte[] getValueAsByteArray(String memberName) throws NumberFormatException {
    throw new IllegalStateException("NYI");
  }

  /**
   * <p>Returns the named member's value as an array of bytes.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray() and getValueAsChar()..
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public char[] getValueAsCharArray(String memberName) throws IllegalArgumentException {
    throw new IllegalStateException("NYI");
  }
}
