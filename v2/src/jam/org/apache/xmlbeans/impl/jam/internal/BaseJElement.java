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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xmlbeans.impl.jam.*;

/**
 * Base class for implementing interfaces which implement JElement.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseJElement implements JElement {

  // ========================================================================
  // Constants

  // help reduce object waste
  public static final JElement[] NO_NODE = new JElement[0];
  public static final JClass[] NO_CLASS = new JClass[0];
  public static final JField[] NO_FIELD = new JField[0];
  public static final JConstructor[] NO_CONSTRUCTOR = new JConstructor[0];
  public static final JMethod[] NO_METHOD = new JMethod[0];
  public static final JParameter[] NO_PARAMETER = new JParameter[0];
  public static final JPackage[] NO_PACKAGE = new JPackage[0];
  public static final JAnnotation[] NO_ANNOTATION = new JAnnotation[0];

  public static final JComment[] NO_COMMENT = new JComment[0];
  public static final JProperty[] NO_PROPERTY = new JProperty[0];

  private static final String ANNOTATION_SEPARATOR = "@";

  // ========================================================================
  // Variables

  private JAnnotation[] mAnns = null;
  private JComment[] mComments = null;
  private List mTempList = null;

  // ========================================================================
  // Constructors

  protected BaseJElement() {
  }

  private List getTempList() {
    return new ArrayList();
    /*
    if (mTempList == null) {
      mTempList = new ArrayList();
    } else {
      mTempList.clear();
    }
    return mTempList;
    */
  }

  // ========================================================================
  // JAnnotation implementation

  public final JAnnotation[] getAnnotations() {
    if (mAnns == null) {
      List list = getTempList();
      getLocalAnnotations(list);
      if (list.size() == 0) {
        mAnns = NO_ANNOTATION;
      } else {
        mAnns = new JAnnotation[list.size()];
        list.toArray(mAnns);
      }
    }
    return mAnns; // FIXME do we need to return a copy?
  }

  public final JAnnotation[] getAnnotations(String named) {
    List list = getTempList();
    gatherAnnotations(named, list);
    JAnnotation[] out = new JAnnotation[list.size()];
    list.toArray(out);
    return out;
  }

  public final JComment[] getComments() {
    if (mComments == null) {
      List list = getTempList();
      getLocalComments(list);
      /*      if (mContext.getExtraMarkupStore() != null) {
      mContext.getExtraMarkupStore().getComments(this,list);
      }*/
      if (list.size() == 0) {
        mComments = NO_COMMENT;
      } else {
        mComments = new JComment[list.size()];
        list.toArray(mComments);
      }
    }
    return mComments; // FIXME do we need to return a copy?
  }

  public final JAnnotation getAnnotation(String named) {
    JAnnotation[] anns = getAnnotations(named);
    return (anns.length == 0) ? null : anns[0];
  }

  // ========================================================================
  // Object implementation

  public String toString() {
    return getQualifiedName();
  }

  // ========================================================================
  // Methods to be implemented by subclasses

  /**
   * Extending class must implement this by adding all of the
   * annotations which apply to this object to the given 'out'
   * collection.  Implement as a no-op if annotations are not
   * applicable.
   */
  protected abstract void getLocalAnnotations(Collection out);

  /**
   * Extending class must implement this by adding all of the
   * annotations which apply to this object to the given 'out'
   * collection.  Implement as a no-op if comments are not applicable.
   */
  protected abstract void getLocalComments(Collection out);

  // ========================================================================
  // Private methods

  private void gatherAnnotations(String fullName, Collection out) {
    JAnnotation[] anns = getAnnotations();
    if (anns == NO_ANNOTATION) return;
    int dot = fullName.indexOf(ANNOTATION_SEPARATOR);
    String subname = (dot == -1) ? fullName : fullName.substring(0, dot);
    String postfix = (dot == -1 || dot == fullName.length() - 1) ?
            null : fullName.substring(dot + 1);
    // System.out.println("===== "+subname+"  "+postfix);
    for (int i = 0; i < anns.length; i++) {
      if (anns[i].getName().equals(subname)) {
        if (postfix == null) {
          out.add(anns[i]);
        } else {
          // REVIEW nasty cast.  Alternative is to expose this method
          // via a private interface, but that seems like overkill.
          ((BaseJElement) anns[i]).gatherAnnotations(postfix, out);
        }
      }
    }
  }


  // ========================================================================
  // Object implementation

  // REVIEW this needs more thought.

  public boolean equals(Object o) {
    return (o instanceof JElement) &&
            getQualifiedName().equals(((JElement) o).getQualifiedName());
  }

  public int hashCode() {
    return getQualifiedName().hashCode();
  }

}
