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

  // ========================================================================
  // Constants

  public static final String SINGLE_MEMBER_NAME = "value";

  // ========================================================================
  // Public methods

  /**
   * Returns the name of this annotation.  Note that in the case of
   * javadoc-style annotations, this name will NOT include the leading
   * '@'.
   *
   * REVIEW this is a little weird - it's going to be the same as
   * getDeclaration().getSimpleName();  it really is type information,
   * which I thought we didn't want to expose here.  However,
   * I think name is still needed here simply because we may not always
   * have a definition (i.e. in the javadoc case), but we will still
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
   * or null if no such member exists.  If this tag is a 175 tag,
   * you can call getMember(SINGLE_MEMBER_NAME) to get the single member
   * of a single-member annotation, as described in the 175 specification.
   * If this tag is a javadoc tag, getMember(SINGLE_MEMBER_NAME) returns
   * the lone member of a simple javadoc tag (i.e., one of the form
   * '@mytag myvalue').
   *
   * @return The named member or null if none exists.
   * @throws IllegalArgumentException if the parameter is null.
   */
  public JAnnotationMember getMember(String named);

  /**
   * Returns a representation of this annotation's type definition.  This
   * typically returns null if the Annotation is does not represent a
   * JSR175 annotation.
   */
  public JAnnotationDefinition getDefinition();

  /**
   * <p>If this JAnnotation represents a JSR175 annotation, returns the
   * underlying java.lang.Annotation instance.  Returns null otherwise.</p>
   */
  public Object getAnnotationObject();

  /**
   * <p>If this JAnnotation represents a javadoc tag, returns the raw,
   * untrimmed contents of the tag.  Otherwise, returns null.  You
   * shouldn't use this method without a really good reason - you normally
   * should call one of the getMember() methods to get at the tag contents.
   * You can call getMember(SINGLE_MEMBER_NAME) to get a JAnnotationMember
   * representing the contents of a simple javadoc tag (e.g. @mytag myvalue).
   * </p>
   */
  public String getJavadocText();


  // ========================================================================
  // Deprecated - please get values from members instead

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public String getStringValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public int getIntValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public boolean getBooleanValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public long getLongValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public short getShortValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public double getDoubleValue();

  /**
   * @deprecated Please refer to the javadocs on getSingleMember() for
   * more information on the preferred alternative.
   */
  public byte getByteValue();
}