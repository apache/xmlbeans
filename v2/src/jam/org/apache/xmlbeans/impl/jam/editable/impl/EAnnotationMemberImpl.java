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
package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EAnnotationMember;
import org.apache.xmlbeans.impl.jam.editable.EElementVisitor;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JAnnotationMemberDefinition;
import org.apache.xmlbeans.impl.jam.JClassLoader;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EAnnotationMemberImpl extends EElementImpl
        implements EAnnotationMember {

  // ========================================================================
  // Variables

  private Object mValue = null;
  private boolean mIsDefaultUsed = false;

  // ========================================================================
  // Constructors

  public EAnnotationMemberImpl(String simpleName, JClassLoader loader) {
    super(simpleName,loader);
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    throw new UnsupportedOperationException("NYI");//FIXME
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
    mValue = QualifiedJClassRef.create(clazz.getQualifiedName(),
                                       getClassLoader());
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
  // JAnnotationMember implementation  FIXME

  public boolean isDefaultValueUsed() {
    return mIsDefaultUsed;
  }

  public String getName() {
    return null;
  }

  public Object getValue() {
    return null;
  }

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
    return null;
  }

  public int getValueAsInt() throws NumberFormatException {
    return 0;
  }

  public boolean getValueAsBoolean() throws IllegalArgumentException {
    return false;
  }

  public long getValueAsLong() throws NumberFormatException {
    return 0;
  }

  public short getValueAsShort() throws NumberFormatException {
    return 0;
  }

  public double getValueAsDouble() throws NumberFormatException {
    return 0;
  }

  public float getValueAsFloat() throws NumberFormatException {
    return 0;
  }

  public byte getValueAsByte() throws NumberFormatException {
    return 0;
  }

  public char getValueAsChar() throws IllegalArgumentException {
    return 0;
  }

  public Object[] getValueAsArray() {
    return new Object[0];
  }

  public JClass[] getValueAsClassArray() {
    return new JClass[0];
  }

  public JAnnotation[] getValueAsAnnotationArray() {
    return new JAnnotation[0];
  }

  public String[] getValueAsStringArray() {
    return new String[0];
  }

  public int[] getValueAsIntArray() throws NumberFormatException {
    return new int[0];
  }

  public boolean[] getValueAsBooleanArray() throws IllegalArgumentException {
    return new boolean[0];
  }

  public short[] getValueAsShortArray() throws NumberFormatException {
    return new short[0];
  }

  public long[] getValueAsLongArray() throws NumberFormatException {
    return new long[0];
  }

  public double[] getValueAsDoubleArray() throws NumberFormatException {
    return new double[0];
  }

  public float[] getValueAsFloatArray() throws NumberFormatException {
    return new float[0];
  }

  public byte[] getValueAsByteArray() throws NumberFormatException {
    return new byte[0];
  }

  public char[] getValueAsCharArray() throws IllegalArgumentException {
    return new char[0];
  }


  // ========================================================================
  // EElement implementation

  public void accept(EElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(EElementVisitor visitor) {
    accept(visitor);
    if (mValue instanceof EAnnotationMember) {
      ((EAnnotationMember)mValue).acceptAndWalk(visitor); //REVIEW is ok???
    }
  }
}