/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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
