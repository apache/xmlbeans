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

package org.apache.xmlbeans.impl.jam.internal;

import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.xmlbeans.impl.jam.*;

/**
 * Javadoc-backed implementation of org.apache.xmlbeans.impl.jam.Annotation
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class BaseJAnnotation extends BaseJElement implements JAnnotation {

  // ========================================================================
  // Constants

  private static final String NAME_VALUE_SEPS = "\n\r";
  private static final boolean STRIP_QUOTES = false;

  // ========================================================================
  // Variables

  private JSourcePosition mSourcePosition = null;
  private String mName, mValue = null;
  private JElement mParent;

  // ========================================================================
  // Constructors

  public BaseJAnnotation(JElement parent,
                         String name,
                         String value) {
    this(parent, name, value, null);
  }

  public BaseJAnnotation(JElement parent,
                         String name,
                         String value,
                         JSourcePosition pos) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (parent == null) throw new IllegalArgumentException("null parent");
    mParent = parent;
    mName = name;
    mValue = value;  // ok to be null
    if (mValue != null) mValue = mValue.trim();
    if (STRIP_QUOTES){
      if (mValue.length() > 1) {
        if  (mValue.charAt(0) == '\"' &&
                mValue.charAt(mValue.length()-1) == '\"') {
          mValue = mValue.substring(1,mValue.length()-1);
        }
      }
    }
    mSourcePosition = pos;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return mParent;
  }

  public String getName() {
    return mName;
  }

  public String getSimpleName() {
    return mName;
  } //FIXME

  public String getQualifiedName() {
    return mName;
  } //FIXME

  // ========================================================================
  // JAnnotation implementation

  public JAnnotationMember[] getMembers() {
    return new JAnnotationMember[0];
  }

  public JAnnotationMember getMember(String named) {
    return null;
  }

  public JAnnotationDeclaration getDeclaration() {
    return null;
  }

  public JSourcePosition getSourcePosition() {
    return mSourcePosition;
  }

  public String getStringValue() {
    return mValue;
  }

  public boolean getBooleanValue() {
    return Boolean.valueOf(getTrimmedStringValue().trim()).booleanValue();
  }

  public int getIntValue() {
    if (mValue == null) return 0;
    try {
      return Integer.parseInt(getTrimmedStringValue().trim());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public long getLongValue() {
    try {
      return Long.parseLong(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public float getFloatValue() {
    try {
      return Float.parseFloat(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public double getDoubleValue() {
    try {
      return Double.parseDouble(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public short getShortValue() {
    try {
      return Short.parseShort(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public byte getByteValue() {
    try {
      return Byte.parseByte(getTrimmedStringValue());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  // ========================================================================
  // Protected methods

  protected String getTrimmedStringValue() {
    String v = getStringValue();
    if (v == null) return "";
    return v.trim();
  }

  // ========================================================================
  // BaseJElement impl

  // FIXME this logic should probably be made javadoc-specific
  /**
   * Taking the stringValue of this annotation as a
   * line-break-sepearated list of name-value pairs, creates a new
   * JAnnotation for each pair and adds it to the given collection.
   */
  protected void getLocalAnnotations(Collection out) {
    StringTokenizer st = new StringTokenizer(mValue, NAME_VALUE_SEPS);
    while (st.hasMoreTokens()) {
      String pair = st.nextToken();
      int eq = pair.indexOf('=');
      if (eq <= 0) continue; // if not there or is first character
      String name = pair.substring(0, eq).trim();
      String value = (eq < pair.length() - 1) ? pair.substring(eq + 1) : null;
      JAnnotation ann = new BaseJAnnotation(this, name, value);
      out.add(ann);
    }
  }

  /**
   * Annotations don't have comments, right?
   */
  protected void getLocalComments(Collection out) {
  }

}