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

package org.apache.xmlbeans.impl.jam;



/**
 * <p>Represents a member value of a JAnnotation.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JAnnotationMember {

  /**
   * <p>Returns the value of this annotation as an Object.  If the value
   * is primitive, an instance of one of the java.lang wrappers (e.g. Integer)
   * will be returned.</p>
   *
   * <p>Note that for javadoc tags, this method always returns a String.</p>
   */
  public Object getValue();

  /**
   * <p>Returns true if the member's value was not explicitly set in the
   * annotation instance but was instead taken from the member definition's
   * default.</p>
   *
   * <p>Note that not all JAM implementations may be able to distinguish
   * the case where the value is explicitly declared to be the same value
   * as the member's default from the case where the value is not declared
   * and the value is implicitly default.  In this event, this method
   * will return true if and only if the effective value of the annotation
   * is the same as the default value (regardless of how that value was
   * declared).</p>
   */
  public boolean isDefaultValueUsed();

  /**
   * <p>Returns the a representation of the definition of this member in its
   * annotation type definition.  This will typically return null if
   * the AnnotationMember is not not part of a JSR175 annotation.</p>
   */
  public JAnnotationMemberDefinition getMemberDefinition();

  /**
   * <p>If this member is complex (i.e. an instance of another annotation
   * type), this method returns a representation of the annotation instance.
   * Returns null in all other cases.  This method always returns null if the
   * annotation is a javdoc tag, as such tags only support one level of
   * nesting.</p>
   */
  public JAnnotation getValueAsAnnotation();

  /**
   * <p>Returns the value of this member as a JClass.  Returns null if the
   * value cannot be understood as a class name or if the type of the member
   * is known to be something other than java.lang.Class.</p>
   */
  public JClass getValueAsClass();

  /**
   * <p>Returns the String value of the annotation.  If the value is
   * known to be a simple, non-array type other than String, it will be
   * converted in a resonable manner (with an appropriate toString() or
   * String.valueOf() method). If the value is known to be complex or is an
   * array, this method will return null.</p>
   *
   * <p>If no type information is available for the annotation member (i.e.
   * it's a javadoc tag), then the raw textual value of the member is
   * returned.</p>
   */
  public String getValueAsString();

  /**
   * <p>Returns the member's value as an int.  If the value is not known to be
   * an int, (because it's a javadoc tag or because it's a 175 annotation
   * member of a type other than int) getValueAsString() is called.  If the result is
   * null, NumberFormatException is thrown.  Otherwise, the String is
   * converted to an int with Integer.valueOf(), which again may throw
   * NumberFormatException.</p>
   */
  public int getValueAsInt() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a boolean.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt(), except that IllegalArgumentException is
   * thrown instead of NumberFormatException.</p>
   */
  public boolean getValueAsBoolean() throws IllegalArgumentException;

  /**
   * <p>Returns the member's value as a long.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public long getValueAsLong() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a short.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public short getValueAsShort() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a double.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public double getValueAsDouble() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a float.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public float getValueAsFloat() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a byte.  If necessary, type
   * conversion is performed in a similar manner as described for
   * getValueAsInt().</p>
   */
  public byte getValueAsByte() throws NumberFormatException;

  /**
   * <p>Returns the member's value as a char.  If necessary, type
   * conversion is performed by calling getStringValue().  If the result
   * is null or is a String that is not exactly of length 1,
   * IllegalArgumentException is thrown.</p>
   */
  public char getValueAsChar() throws IllegalArgumentException;

  /**
   * <p>If this member is known to be of an array type, returns the value
   * as an array of Objects.  If the array component type is primitive,
   * the array objects will be instances of an appropriate java.lang
   * wrapper (e.g., Integer).  Returns null if the member type is
   * not an array.</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public Object[] getValueAsArray();

  /**
   * <p>If this member is known to be an array of classes, returns an
   * array of JClass representations of those classes.  If the memeber
   * is known to be an array of a simple non-array type, this method
   * will call getValueAsStringArray() and attempt to return a JClass
   * by treating each string in the returned array as a qualified classname.
   * Returns null otherwise.
   * </p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public JClass[] getValueAsClassArray();

  /**
   * <p>If this member is known to be an array of annotations (i.e.
   * complex, nested types), this method returns an array containing
   * each complex value as a JAnnotation.  Returns null in all other cases.
   * </p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public JClass[] getValueAsAnnotationArray();

  /**
   * <p>Returns this member's value as an array of Strings.  If this member is
   * not of an array type, or is an array of arrays or complex annotations,
   * this method returns null.  If it is an array of a simple, non-array type
   * other than String, conversion on each component will be attempted as
   * described under getStringValue().</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public String[] getValueAsStringArray();

  /**
   * <p>Returns this member's value as an array of ints.  If this member is
   * not of an array type, or is an array of arrays or complex annotations,
   * this method returns null.  If it is an array of a simple, non-array type
   * other than int, conversion on each component will be attempted as
   * described under getIntValue().</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public int[] getValueAsIntArray() throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of booleans.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray(), except that IllegalArgumentException may be
   * thrown instead of NumberFormatException.</p>
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public boolean[] getValueAsBooleanArray() throws IllegalArgumentException;

  /**
   * <p>Returns this member's value as an array of shorts.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public short[] getValueAsShortArray() throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of longs.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public long[] getValueAsLongArray()  throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of doubles.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public double[] getValueAsDoubleArray()  throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of floats.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public float[] getValueAsFloatArray()  throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of bytes.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray().
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public byte[] getValueAsByteArray()  throws NumberFormatException;

  /**
   * <p>Returns this member's value as an array of bytes.  If necessary,
   * type conversion is performed in a similar manner as described for
   * getValueAsIntArray() and getValueAsChar()..
   *
   * <p>This method always returns null for javadoc tags.</p>
   */
  public char[] getValueAsCharArray()  throws IllegalArgumentException;

}
