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
 * <p>Represents an exposed field on a Java class.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JField extends JMember {

  /**
   * Returns the type of this field.
   */
  public JClass getType();

  /**
   * Return true if this field is final.
   */
  public boolean isFinal();

  /**
   * Return true if this field is static.
   */
  public boolean isStatic();

  /**
   * Return true if this field is volatile.
   */
  public boolean isVolatile();

  /**
   * Return true if this field is transient.
   */
  public boolean isTransient();


  /**
   * <p>Returns a qualied name for this method as specified by
   * <code>java.lang.reflect.Field.toString()</code>:</p>
   *
   * <p><i>Returns a string describing this Field. The format is the access
   * modifiers for the field, if any, followed by the field type, followed
   * by a space, followed by the fully-qualified name of the class declaring
   * the field, followed by a period, followed by the name of the field.
   * For example:</i></p>
   *
   * <p><i>public static final int java.lang.Thread.MIN_PRIORITY</i></p>
   * <p><i>private int java.io.FileDescriptor.fd</i></p>
   *
   * <p><i>The modifiers are placed in canonical order as specified by "The
   * Java Language Specification". This is public, protected or private
   * first, and then other modifiers in the following order: static, final,
   * transient, volatile.</i></p>
   */
  public String getQualifiedName();
}
