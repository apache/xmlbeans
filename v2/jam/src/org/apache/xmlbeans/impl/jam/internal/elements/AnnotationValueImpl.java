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
package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;

/**
 * <p>Implementation of JAnnotationValue</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class AnnotationValueImpl implements JAnnotationValue {

  // ========================================================================
  // Variables

  private Object mValue = null;
  private String mName;
  private ElementContext mContext;
  private boolean mIsDefaultUsed = false;

  // ========================================================================
  // Constructors

  public AnnotationValueImpl(ElementContext ctx,
                              String simpleName,
                              Object value) {
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    if (simpleName == null) throw new IllegalArgumentException("null name");
    if (value == null) throw new IllegalArgumentException("null value");
    mContext = ctx;
    mName = simpleName;
    mValue = value;
  }

  // ========================================================================
  // EAnnotationMember implementation

  public void setValue(Object o) {
    mValue = o;
  }

  public void setValue(String value) {
    mValue = value;
  }

  public void setValue(JAnnotation value) {
    mValue = value;
  }

  public void setValue(boolean value) {
    mValue = new Boolean(value);
  }

  public void setValue(int value) {
    mValue = new Integer(value);
  }

  public void setValue(short value) {
    mValue = new Short(value);
  }

  public void setValue(long value) {
    mValue = new Long(value);
  }

  public void setValue(float value) {
    mValue = new Float(value);
  }

  public void setValue(double value) {
    mValue = new Double(value);
  }

  public void setValue(JClass clazz) {
    mValue = clazz;
    //FIXME QualifiedJClassRef.create(clazz.getQualifiedName(),mContext);

  }

  public void setValue(String[] value) {
    mValue = value;
  }

  public void setValue(JAnnotation[] value) {
    mValue = value;
  }

  public void setValue(boolean[] value) {
    mValue = value;
  }

  public void setValue(int[] value) {
    mValue = value;
  }

  public void setValue(short[] value) {
    mValue = value;
  }

  public void setValue(long[] value) {
    mValue = value;
  }

  public void setValue(float[] value) {
    mValue = value;
  }

  public void setValue(double[] value) {
    mValue = value;
  }

  public void setValue(JClass[] classes) {
    mValue = classes;
  }

  // ========================================================================
  // JAnnotationValue implementation  FIXME

  public boolean isDefaultValueUsed() {
    throw new IllegalStateException("nyi");
    //return mIsDefaultUsed;
  }

  public String getName() {
    return mName;
  }

  //docme
  public JClass getType() {
    return (mValue == null) ? null :
      mContext.getClassLoader().loadClass(mValue.getClass().getName());
  }

  public Object getValue() {
    return mValue;
  }

  public JAnnotation asAnnotation() {
    throw new IllegalStateException("NYI");
  }

  public JClass asClass() {
    throw new IllegalStateException("NYI");
  }

  public String asString() {
    if (mValue == null) return null;
    return mValue.toString();
  }

  public int asInt() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).intValue();
    try {
      return Integer.parseInt(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public boolean asBoolean() throws IllegalArgumentException {
    if (mValue == null) return false;
    return Boolean.valueOf(mValue.toString().trim()).booleanValue();
  }

  public long asLong() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).longValue();
    try {
      return Long.parseLong(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public short asShort() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).shortValue();
    try {
      return Short.parseShort(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public double asDouble() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).doubleValue();
    try {
      return Double.parseDouble(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public float asFloat() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).floatValue();
    try {
      return Float.parseFloat(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public byte asByte() throws NumberFormatException {
    if (mValue == null) return 0;
    if (mValue instanceof Number) return ((Number)mValue).byteValue();
    try {
      return Byte.parseByte(mValue.toString().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public char asChar() throws IllegalArgumentException {
    if (mValue == null) return 0;
    if (mValue instanceof Character) return ((Character)mValue).charValue();
    mValue = mValue.toString();
    return (((String)mValue).length() == 0) ? 0 : ((String)mValue).charAt(0);
  }

  public Object[] asArray() {
     throw new IllegalStateException("NYI");
  }

  public JClass[] asClassArray() {
    throw new IllegalStateException("NYI");
  }

  public JAnnotation[] asAnnotationArray() {
    throw new IllegalStateException("NYI");
  }

  public String[] asStringArray() {
    throw new IllegalStateException("NYI");
  }

  public int[] asIntArray() throws NumberFormatException {
       throw new IllegalStateException("NYI");
  }

  public boolean[] asBooleanArray() throws IllegalArgumentException {
      throw new IllegalStateException("NYI");
  }

  public short[] asShortArray() throws NumberFormatException {
        throw new IllegalStateException("NYI");
  }

  public long[] asLongArray() throws NumberFormatException {
       throw new IllegalStateException("NYI");
  }

  public double[] asDoubleArray() throws NumberFormatException {
        throw new IllegalStateException("NYI");
  }

  public float[] asFloatArray() throws NumberFormatException {
       throw new IllegalStateException("NYI");
  }

  public byte[] asByteArray() throws NumberFormatException {
       throw new IllegalStateException("NYI");
  }

  public char[] asCharArray() throws IllegalArgumentException {
        throw new IllegalStateException("NYI");
  }


}