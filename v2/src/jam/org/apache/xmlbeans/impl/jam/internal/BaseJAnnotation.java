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

package org.apache.xmlbeans.impl.jam.internal;

import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.xmlbeans.impl.jam.*;

/**
 * Base implementation of org.apache.xmlbeans.impl.jam.Annotation
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class BaseJAnnotation extends BaseJElement
        implements JAnnotation, JAnnotationMember
 {

  // ========================================================================
  // Constants

  private static final String NAME_VALUE_SEPS = "\n\r";
  private static final boolean STRIP_QUOTES = false;

  // ========================================================================
  // Variables

  private JSourcePosition mSourcePosition = null;
  private String mName, mValue = null;
  private JElement mParent;

  // ========================================================================
  // Constructors

  public BaseJAnnotation(JElement parent,
                         String name,
                         String value) {
    this(parent, name, value, null);
  }

  public BaseJAnnotation(JElement parent,
                         String name,
                         String value,
                         JSourcePosition pos) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (parent == null) throw new IllegalArgumentException("null parent");
    mParent = parent;
    mName = name;
    mValue = value;  // ok to be null
    if (mValue != null) mValue = mValue.trim();
    if (STRIP_QUOTES){
      if (mValue.length() > 1) {
        if  (mValue.charAt(0) == '\"' &&
                mValue.charAt(mValue.length()-1) == '\"') {
          mValue = mValue.substring(1,mValue.length()-1);
        }
      }
    }
    mSourcePosition = pos;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return mParent;
  }

  public String getName() {
    return mName;
  }

  public Object getValue() {
    return null;
  }

  public boolean isDefaultValueUsed() {
    return false;
  }

  public String getSimpleName() {
    return mName;
  } //FIXME

  public String getQualifiedName() {
    return mName;
  } //FIXME

  // ========================================================================
  // JAnnotation implementation

  public JAnnotationMember[] getMembers() {
    return new JAnnotationMember[0];
  }

  public JAnnotationMember getMember(String named) {
    return null;
  }

  public JAnnotationDefinition getDefinition() {
    return null;
  }

  public Object getAnnotationObject() {
    return null;
  }

  public String getJavadocText() {
    return null;
  }

  public JSourcePosition getSourcePosition() {
    return mSourcePosition;
  }

  public String getStringValue() {
    return mValue;
  }

  public boolean getBooleanValue() {
    return Boolean.valueOf(getTrimmedStringValue().trim()).booleanValue();
  }

  public int getIntValue() {
    if (mValue == null) return 0;
    try {
      return Integer.parseInt(getTrimmedStringValue().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public long getLongValue() {
    try {
      return Long.parseLong(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public float getFloatValue() {
    try {
      return Float.parseFloat(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public double getDoubleValue() {
    try {
      return Double.parseDouble(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public short getShortValue() {
    try {
      return Short.parseShort(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public byte getByteValue() {
    try {
      return Byte.parseByte(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  // ========================================================================
  // JAnnotationMember implementation

  public JAnnotationMemberDefinition getMemberDefinition() {
    return null;
  }

  public JAnnotation getValueAsAnnotation() {
    return null;
  }

  public JClass getValueAsClass() {
    return null;
  }

  public String getValueAsString() {
    return getStringValue();
  }

  public int getValueAsInt() {
    return getIntValue();
  }

  public boolean getValueAsBoolean() {
    return getBooleanValue();
  }

  public long getValueAsLong() {
    return getLongValue();
  }

  public short getValueAsShort() {
    return getShortValue();
  }

  public double getValueAsDouble() {
    return getDoubleValue();
  }

  public float getValueAsFloat() throws NumberFormatException {
    return 0;
  }

  public byte getValueAsByte() {
    return getByteValue();
  }

  public char getValueAsChar() throws IllegalArgumentException {
    return 0;
  }

  public Object[] getValueAsArray() {
    return null;
  }

  public JClass[] getValueAsClassArray() {
    return null;
  }

  public JAnnotation[] getValueAsAnnotationArray() {
    return null;
  }

  public String[] getValueAsStringArray() {
    return null;
  }

  public int[] getValueAsIntArray() throws NumberFormatException {
    return null;
  }

  public boolean[] getValueAsBooleanArray() throws IllegalArgumentException {
    return null;
  }

  public short[] getValueAsShortArray() throws NumberFormatException {
    return null;
  }

  public long[] getValueAsLongArray() throws NumberFormatException {
    return null;
  }

  public double[] getValueAsDoubleArray() throws NumberFormatException {
    return null;
  }

  public float[] getValueAsFloatArray() throws NumberFormatException {
    return null;
  }

  public byte[] getValueAsByteArray() throws NumberFormatException {
    return null;
  }

  public char[] getValueAsCharArray() throws IllegalArgumentException {
    return null;
  }

  // ========================================================================
  // Protected methods

  protected String getTrimmedStringValue() {
    String v = getStringValue();
    if (v == null) return "";
    return v.trim();
  }

  // ========================================================================
  // BaseJElement impl

  // FIXME this logic should probably be made javadoc-specific
  /**
   * Taking the stringValue of this annotation as a
   * line-break-sepearated list of name-value pairs, creates a new
   * JAnnotation for each pair and adds it to the given collection.
   */
  protected void getLocalAnnotations(Collection out) {
    StringTokenizer st = new StringTokenizer(mValue, NAME_VALUE_SEPS);
    while (st.hasMoreTokens()) {
      String pair = st.nextToken();
      int eq = pair.indexOf('=');
      if (eq <= 0) continue; // if not there or is first character
      String name = pair.substring(0, eq).trim();
      String value = (eq < pair.length() - 1) ? pair.substring(eq + 1) : null;
      JAnnotation ann = new BaseJAnnotation(this, name, value);
      out.add(ann);
    }
  }

  /**
   * Annotations don't have comments, right?
   */
  protected void getLocalComments(Collection out) {
  }

}