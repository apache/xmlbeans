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
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JAnnotationMember {

  /**
   * Returns the name of this annotation member.
   *
   * REVIEW this is a little weird - it's going to be the same as
   * getDeclaration().getSimpleName();  it really is type information,
   * which I thought we didn't want to expose here.  However,
   * I think name is still needed here simply because we may not always
   * have a declaration (i.e. in the javadoc case), but we will still
   * have a name.
   */
  public String getName();

  /**
   * Returns the value of this annotation as an Object.  If the value
   * is primitive, one of the
   */
  public Object getValue();

  /**
   * <p>Returns true if the member's value was not explicitly set in the
   * annotation instance but was instead taken from the member declaration
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
   * Returns the a representation of the declaration of this member in its
   * annotation type declaration.
   */
  public JAnnotationMemberDeclaration getDeclaration();

  /**
   * Returns the String value of the annotation.  Returns an empty string
   * by default.
   */
  public JAnnotation getValueAsAnnotation();

  /**
   * Returns the value of this member as a JClass.  Returns null if the
   * value cannot be understood as a class name or if the type of the member
   * is known to be something other than java.lang.Class.
   */
  public JClass getValueAsClass();

  /**
   * Returns the String value of the annotation.  Returns an empty string
   * by default.
   */
  public String getValueAsString();

  /**
   * Returns the value as an int.  Returns 0 by default if the value
   * cannot be understood as an int.
   */
  public int getValueAsInt();

  /**
   * Returns the value as a boolean.  Returns false by default if the
   * annotation value cannot be understood as a boolean.
   */
  public boolean getValueAsBoolean();

  /**
   * Returns the value as a long.  Returns 0 by default if the
   * annotation value cannot be understood as a long.
   */
  public long getValueAsLong();

  /**
   * Returns the value as a short.  Returns 0 by default if the
   * annotation value cannot be understood as a short.
   */
  public short getValueAsShort();

  /**
   * Returns the value as a double.  Returns 0 by default if the
   * annotation value cannot be understood as a double.
   */
  public double getValueAsDouble();

  /**
   * Returns the value as a byte.  Returns 0 by default if the
   * annotation value cannot be understood as a byte.
   */
  public byte getValueAsByte();

}
