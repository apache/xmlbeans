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

package org.apache.xmlbeans.impl.jam_old;

/**
 * <p>Represents an abstraction which can be member of a class.  Such
 * abstractions include: constructors, methods, fields, and classes
 * (in the case of inner classes).  JMember's share the following
 * attributes in common: they have some level of access protection
 * (public/protected/private) and are (usually) contained within a
 * class.</p>
 *
 * <p>Because classes themselves can be members of other classes,
 * JClass extends JMember.  In the case where a JClass represents a
 * top-level class (i.e. not an inner class),
 * JMember.getContainingClass() will always return null.</p>
 *
 * <p>Note that the various access protection levels are all mutually
 * exclusive.  For a given abstraction, only one of isPrivate(),
 * isPackagePrivate(), isProtected(), or isPublic() will return true.
 * Note that this information is also exposed via getModifiers(),
 * which returns a bit-field that is identical to that described in
 * java.lang.reflect.Modifier.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract interface JMember extends JElement {

  // ========================================================================
  // Public methods

  /**
   * <p>Returns a representation of the class which contains this
   * member.  Note that if this member is an inner class, this method
   * returns the class in which this class is declared (i.e. 'outer').
   * If this member is a top-level class, this method will always
   * return null.</p>
   */
  public JClass getContainingClass();

  /**
   * <p>Returns the modifiers specifier.  This is a bit field exactly
   * like those returned by java.lang.Class.getModifiers() and can be
   * manipulated using java.lang.reflect.Modifier in the same way.</p>
   */
  public int getModifiers();

  /**
   * Return true if this member is package private (i.e. the default
   * access protection level).
   */
  public boolean isPackagePrivate();

  /**
   * Return true if this member is private.  Equivalent to calling
   * Modifier.isPrivate(member.getModifiers()).
   */
  public boolean isPrivate();

  /**
   * Return true if this member is protected.  Equivalent to calling
   * Modifier.isProtected(member.getModifiers()).
   */
  public boolean isProtected();

  /**
   * Return true if this member is public.  Equivalent to calling
   * Modifier.isProtected(member.getModifiers()).
   */
  public boolean isPublic();


}
