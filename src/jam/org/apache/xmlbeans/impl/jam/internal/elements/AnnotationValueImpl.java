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
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;


/**
 * <p>Implementation of JAnnotationValue</p>
 *
 * @author Patrick Calahan <codehaus@bea.com>
 */
public class AnnotationValueImpl implements JAnnotationValue {

  // ========================================================================
  // Variables

  private Object mValue = null;
  private JClassRef mType = null;
  private String mName;
  private ElementContext mContext;

  // ========================================================================
  // Constructors

  public AnnotationValueImpl(ElementContext ctx,
                             String name,
                             Object value,
                             JClass type) {
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    if (name == null) throw new IllegalArgumentException("null name");
    if (value == null) throw new IllegalArgumentException("null value");
    if (type == null) throw new IllegalArgumentException("null type");
    if (value.getClass().isArray()) {
      mValue = ensureArrayWrapped(value);
    } else {
      mValue = value;
    }
    mContext = ctx;
    mName = name;
    mType = QualifiedJClassRef.create(type);
  }

  // ========================================================================
  // JAnnotationValue implementation

  public boolean isDefaultValueUsed() {
    throw new IllegalStateException("NYI");
    //return mIsDefaultUsed;
  }

  public String getName() { return mName; }

  public JClass getType() { return mType.getRefClass(); }


  public JAnnotation asAnnotation() {
    if (mValue instanceof JAnnotation) {
      return (JAnnotation)mValue;
    } else {
      return null; //REVIEW or throw?
    }
  }

  public JClass asClass() {
    if (mValue instanceof JClass) {
      return (JClass)mValue;
    } else {
      return null; //REVIEW or throw?
    }
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
    //FIXME this is not right
    if (mValue == null) return 0;
    if (mValue instanceof Character) return ((Character)mValue).charValue();
    mValue = mValue.toString();
    return (((String)mValue).length() == 0) ? 0 : ((String)mValue).charAt(0);
  }

  public JClass[] asClassArray() {
    if (mValue instanceof JClass[]) {
      return (JClass[])mValue;
    } else {
      return null;
    }
  }

  public JAnnotation[] asAnnotationArray() {
    if (mValue instanceof JAnnotation[]) {
      return (JAnnotation[])mValue;
    } else {
      return null;
    }
  }

  public String[] asStringArray() {
    if (!mValue.getClass().isArray()) return null;
    String[] out = new String[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element on "+
                                   getName());
        out[i] = "";
      } else {
        out[i] = ((Object[])mValue)[i].toString();
      }
    }
    return out;
  }

  public int[] asIntArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    int[] out = new int[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Integer.parseInt(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public boolean[] asBooleanArray() throws IllegalArgumentException {
    if (!mValue.getClass().isArray()) return null;
    boolean[] out = new boolean[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = false;
      } else {
        out[i] = Boolean.valueOf(((Object[])mValue)[i].toString()).booleanValue();
      }
    }
    return out;
  }

  public short[] asShortArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    short[] out = new short[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Short.parseShort(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public long[] asLongArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    long[] out = new long[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Long.parseLong(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public double[] asDoubleArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    double[] out = new double[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Double.parseDouble(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public float[] asFloatArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    float[] out = new float[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Float.parseFloat(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public byte[] asByteArray() throws NumberFormatException {
    if (!mValue.getClass().isArray()) return null;
    byte[] out = new byte[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        out[i] = Byte.parseByte(((Object[])mValue)[i].toString());
      }
    }
    return out;
  }

  public char[] asCharArray() throws IllegalArgumentException {
    if (!mValue.getClass().isArray()) return null;
    char[] out = new char[((Object[])mValue).length];
    for(int i=0; i<out.length; i++) {
      if (((Object[])mValue)[i] == null) {
        mContext.getLogger().error("Null annotation value array element "+
                                   i+" on "+getName());
        out[i] = 0;
      } else {
        //FIXME this is not right
        out[i] = (((Object[])mValue)[i].toString()).charAt(0);
      }
    }
    return out;
  }

  // ========================================================================
  // Private methods

  //ugh, where is autoboxing when you need it?
  private static final Object[] ensureArrayWrapped(Object o) {
    if (o instanceof Object[]) return (Object[])o;
    if (o instanceof int[]) {
      int dims = ((int[])o).length;
      Integer[] out = new Integer[dims];
      for(int i=0; i<dims; i++) out[i] = new Integer(((int[])o)[i]);
      return out;
    } else if (o instanceof boolean[]) {
      int dims = ((boolean[])o).length;
      Boolean[] out = new Boolean[dims];
      for(int i=0; i<dims; i++) out[i] = Boolean.valueOf(((boolean[])o)[i]);
      return out;
    } else if (o instanceof byte[]) {
      int dims = ((byte[])o).length;
      Byte[] out = new Byte[dims];
      for(int i=0; i<dims; i++) out[i] = new Byte(((byte[])o)[i]);
      return out;
    } else if (o instanceof char[]) {
      int dims = ((char[])o).length;
      Character[] out = new Character[dims];
      for(int i=0; i<dims; i++) out[i] = new Character(((char[])o)[i]);
      return out;
    } else if (o instanceof float[]) {
      int dims = ((float[])o).length;
      Float[] out = new Float[dims];
      for(int i=0; i<dims; i++) out[i] = new Float(((float[])o)[i]);
      return out;
    } else if (o instanceof double[]) {
      int dims = ((double[])o).length;
      Double[] out = new Double[dims];
      for(int i=0; i<dims; i++) out[i] = new Double(((double[])o)[i]);
      return out;
    } else if (o instanceof long[]) {
      int dims = ((long[])o).length;
      Long[] out = new Long[dims];
      for(int i=0; i<dims; i++) out[i] = new Long(((long[])o)[i]);
      return out;
    } else if (o instanceof short[]) {
      int dims = ((short[])o).length;
      Short[] out = new Short[dims];
      for(int i=0; i<dims; i++) out[i] = new Short(((short[])o)[i]);
      return out;
    } else {
      throw new IllegalStateException("Unknown array type "+o.getClass());
    }
   }

  public Object getValue() { return mValue; }
}