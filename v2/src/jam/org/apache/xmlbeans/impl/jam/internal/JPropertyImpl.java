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

import org.apache.xmlbeans.impl.jam.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Implementation of JProperty.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JPropertyImpl implements JProperty {

  // ========================================================================
  // Variables

  private String mName;
  private JMethod mGetter, mSetter;
  private JClass mType;

  // ========================================================================
  // Factory


  // REVIEW should these be available via a getter on JClass?  Seems
  // ok to me, but there were some concerns that this might confusing.
  /**
   * Returns an array of properties found on the given class.
   */
  public static JProperty[] getProperties(JClass clazz) {
    Map name2prop = new HashMap();
    JMethod[] methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getSimpleName();
      //
      // process getters
      //
      if (name.startsWith("get") && name.length() > 3) {
        JClass type = methods[i].getReturnType();
        if (type == null) continue; // must have a type and have
        if (methods[i].getParameters().length > 0) continue; //no params
        name = name.substring(3);
        JPropertyImpl prop = (JPropertyImpl) name2prop.get(name);
        if (prop == null) {
          prop = new JPropertyImpl(name, methods[i], null, type);
          name2prop.put(name, prop);
        } else {
          if (type.equals(prop.getType())) {
            // if it's the same type, cool - just add the getter
            prop.mGetter = methods[i];
          } else {
            // Otherwise, getter/setter types are mismatched and we
            // don't have a property.  REVIEW ok?
            name2prop.remove(name);
          }
        }
      }
      //
      // process setters
      //
      if (name.startsWith("set") && name.length() > 3) {
        if (methods[i].getParameters().length != 1) continue; //1 param reqd
        JClass type = methods[i].getParameters()[0].getType();
        name = name.substring(3);
        JPropertyImpl prop = (JPropertyImpl) name2prop.get(name);
        if (prop == null) {
          prop = new JPropertyImpl(name, null, methods[i], type);
          name2prop.put(name, prop);
        } else {
          if (type.equals(prop.getType())) {
            // if it's the same type, cool - just add the getter
            prop.mSetter = methods[i];
          } else {
            // Otherwise, getter/setter types are mismatched and we
            // don't have a property.  REVIEW ok?
            name2prop.remove(name);
          }
        }
      }
    }
    JProperty[] out = new JProperty[name2prop.values().size()];
    name2prop.values().toArray(out);
    return out;
  }

  // ========================================================================
  // Constructor

  /**
   * <p>You'll usually want to use the getProperties() factory method
   * instead of constructing JProperties yourself.  This constructor
   * is exposed just in case the default rules in the factory method
   * for identifying properties are insufficient for some use
   * case.</p>
   */
  public JPropertyImpl(String name,
                       JMethod getter,
                       JMethod setter,
                       JClass type) {
    mName = name;
    mGetter = getter;
    mSetter = setter;
    mType = type;
  }

  // ========================================================================
  // Public methods

  /**
   * Returns a JClass which represents the type of this property.
   */
  public JClass getType() {
    return mType;
  }

  /**
   * Returns the simple name of this property.  For example, for a
   * property manifest by getFoo() and setFoo(), this will return
   * 'foo'.
   */
  public String getSimpleName() {
    return mName;
  }


  /**
   * Returns the simple name of this property.  For example, for a
   * property manifest by getFoo() and setFoo(), this will return
   * 'foo'.
   */
  public String getQualifiedName() {
    return getParent().getQualifiedName() + "." + getSimpleName(); //REVIEW
  }

  /**
   * Returns a JMethod which represents the setter for this property.
   * Returns null if this property is read-only.
   */
  public JMethod getSetter() {
    return mSetter;
  }

  /**
   * Returns a JMethod which represents the getter for this property.
   * Returns null if this property is write-only.
   */
  public JMethod getGetter() {
    return mGetter;
  }

  // ========================================================================
  // JElement implementation

  /**
   * Returns all of the annotations on the getter and/or the setter
   * methods.
   */
  public JAnnotation[] getAnnotations() {
    return combine((mGetter == null) ?
            BaseJElement.NO_ANNOTATION : mGetter.getAnnotations(),
            (mSetter == null) ?
            BaseJElement.NO_ANNOTATION : mSetter.getAnnotations());
  }

  /**
   * Returns annotations with the given name that are found on this
   * property's getter and/or setter.
   */
  public JAnnotation[] getAnnotations(String named) {
    return combine((mGetter == null) ?
            BaseJElement.NO_ANNOTATION : mGetter.getAnnotations(named),
            (mSetter == null) ?
            BaseJElement.NO_ANNOTATION : mSetter.getAnnotations(named));
  }

  /**
   * Returns the first annotation with the given name that is found on
   * this property's getter and/or setters.
   */
  public JAnnotation getAnnotation(String named) {
    // FIXME this could be a lot more efficient
    JAnnotation[] out = getAnnotations(named);
    if (out.length == 0) {
      return null;
    } else {
      return out[0];
    }
  }

  public JComment[] getComments() {
    return combine((mGetter == null) ?
            BaseJElement.NO_COMMENT : mGetter.getComments(),
            (mSetter == null) ?
            BaseJElement.NO_COMMENT : mSetter.getComments());
  }

  public JElement getParent() {
    return mGetter != null ? mGetter.getParent() : mSetter.getParent();
  }

  public JSourcePosition getSourcePosition() {
    return mGetter != null ?
            mGetter.getSourcePosition() : mSetter.getSourcePosition();
  }

  // ========================================================================
  // Object implementation

  public String toString() {
    return getQualifiedName();
  }

  // ========================================================================
  // Private methods

  /**
   * Returns an array that is the union of the two arrays of
   * anotations.
   */
  private JAnnotation[] combine(JAnnotation[] a, JAnnotation[] b) {
    if (a.length == 0) return b;
    if (b.length == 0) return a;
    JAnnotation[] out = new JAnnotation[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  /**
   * Returns an array that is the union of the two arrays of
   * anotations.
   */
  private JComment[] combine(JComment[] a, JComment[] b) {
    if (a.length == 0) return b;
    if (b.length == 0) return a;
    JComment[] out = new JComment[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }
}
