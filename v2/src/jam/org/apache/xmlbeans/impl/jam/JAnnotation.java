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
   * <p>Returns the JAnnotationMember which represents the member of this
   * annotation if this annotation qualifies as a 'single member
   * annotation.'</p>
   *
   * <p>This method should not be used lightly, as it is here primarily to
   * provide support simple javadoc tags of the form '@mytag value'.  If you
   * are using tags of this form, you probably need to do some thinking
   * about how you want to map your tag system into 175 annotation types.
   * getSingleMember() provides one avenue for such a mapping by equating
   * such simple javadoc tags as JSR175 single member annotations.</p>
   *
   * <p>The qualifications for being a 'single member annotation' are as
   * follows:</p>
   * <ul>
   *   <li>For JSR175 tags, as described in the spec, the annotation must be
   *       of a type which either has only one member, or has multiple members
   *       but exactly one member which has no default value (which is
   *       considered the single member).</li>
   *   <li>All javadoc tags implicitly qualify as single member tags,
   *       although not all of them should be treated as such.  For simple
   *       javadoc tags (@mytag value), this method provides thoe only
   *       means of accessing the tag value.  However, if the tag contains
   *       complex content (typically expressed as name=value pairs), this
   *       method returns a member which contains the entire raw text of
   *       the tag, whitespace and '=' signs included.  Typically, this is not
   *       useful - you should call getMembers() or getMember("name") to get
   *       at the structured data.</li>
   * </ul>
   *
   * <p>If this annotation does not qualify as a single member annotation,
   * as described above, this method will return null.</p.
   */
  public JAnnotationMember getSingleMember();

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
   * Returns a representation of this annotation's type declaration.  This
   * typically returns null if the Annotation is does not represent a
   * JSR175 annotation.
   */
  public JAnnotationDeclaration getDeclaration();

  /**
   * <p>If this JAnnotation represents a JSR175 annotation, returns the
   * underlying java.lang.Annotation instance.  Returns null otherwise.</p>
   */
  public Object getAnnotationObject();


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