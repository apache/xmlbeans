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
 * <p>Represents a metadata that is associated with a particular
 * JElement.  Note that JAnnoations are JElements, which means
 * that they themselves can have annotations, and can be treated as
 * nodes in a JAM hierarchy.</p>
 *
 * <p>Annotations can be simple or complex.  Values of simple
 * annotations can be retrieved via the various get...Value() methods.
 * Complex attributes can be navigated via the getAnnotations() method
 * which exposes nested attributes (which may in turn be either simple
 * or complex.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JAnnotation extends JElement {

  /**
   * Returns the name of this annotation.  Note that in the case of
   * javadoc-style annotations, this name will NOT include the leading
   * '@'.
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
   * Returns an array containing this annotation's members.  Returns an
   * empty array if the annotation has no members.
   */
  public JAnnotationMember[] getMembers();

  /**
   * Returns the member of this annotation which has the given name,
   * or null if no such member exists.
   *
   * @return The named member or null.
   * @throws IllegalArgumentException if the parameter is null.
   */
  public JAnnotationMember getMember(String named);

  /**
   * Returns a representation of this annotation's type declaration.
   */
  public JAnnotationDeclaration getDeclaration();


  // ========================================================================
  // These methods will all be deprecated soon

  /**
   * @deprecated
   */
  public String getStringValue();

  /**
   * @deprecated
   */
  public int getIntValue();

  /**
   * @deprecated
   */
  public boolean getBooleanValue();

  /**
   * @deprecated
   */
  public long getLongValue();

  /**
   * @deprecated
   */
  public short getShortValue();

  /**
   * @deprecated
   */
  public double getDoubleValue();

  /**
   * @deprecated
   */
  public byte getByteValue();
}